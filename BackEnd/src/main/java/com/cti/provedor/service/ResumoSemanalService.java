package com.cti.provedor.service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cti.provedor.dto.DashboardDTO;
import com.cti.provedor.model.Importacao;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.ImportacaoRepository;
import com.cti.provedor.repository.UsuarioRepository;

/** Toda segunda-feira às 8h (horário de Brasília), envia o resumo da semana a quem ativou a preferência. */
@Service
public class ResumoSemanalService {

    private final UsuarioRepository usuarioRepository;
    private final ImportacaoRepository importacaoRepository;
    private final DashboardService dashboardService;
    private final EmailService email;
    private final String frontendUrl;

    public ResumoSemanalService(UsuarioRepository usuarioRepository, ImportacaoRepository importacaoRepository,
            DashboardService dashboardService, EmailService email, @Value("${app.frontend-url}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.importacaoRepository = importacaoRepository;
        this.dashboardService = dashboardService;
        this.email = email;
        this.frontendUrl = frontendUrl;
    }

    @Scheduled(cron = "0 0 8 * * MON", zone = "America/Sao_Paulo")
    public void enviarResumos() {
        List<Usuario> destinatarios = usuarioRepository.findByPrefResumoSemanalTrue();
        if (destinatarios.isEmpty()) {
            return;
        }
        String texto = montarTexto();
        for (Usuario usuario : destinatarios) {
            email.enviar(usuario.getEmail(), "DataInsights: resumo da semana", "Olá, " + usuario.getNome() + ".\n\n" + texto);
        }
    }

    String montarTexto() {
        DashboardDTO painel = dashboardService.calcular();
        List<Importacao> semana = importacaoRepository.findByEnviadaEmAfterOrderByEnviadaEmDesc(
                LocalDateTime.now().minusDays(7));
        int criados = semana.stream().mapToInt(Importacao::getCriados).sum();
        int atualizados = semana.stream().mapToInt(Importacao::getAtualizados).sum();

        NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        moeda.setMaximumFractionDigits(0);

        StringBuilder texto = new StringBuilder();
        texto.append("Nos últimos 7 dias:\n");
        texto.append("· ").append(semana.size()).append(" planilha(s) enviada(s)\n");
        texto.append("· ").append(criados).append(" cliente(s) novo(s) e ").append(atualizados)
                .append(" atualizado(s)\n\n");
        texto.append("Carteira hoje:\n");
        texto.append("· ").append(painel.totalClientes()).append(" cliente(s), faturamento anual somado de ")
                .append(moeda.format(painel.faturamentoTotal())).append("\n");
        texto.append("· ").append(painel.contratosAtivos()).append(" contrato(s) ativo(s)\n");
        texto.append("· ").append(painel.insights()).append(" insight(s) registrado(s)\n\n");
        texto.append("Abra o painel: ").append(frontendUrl).append("/app\n\n");
        texto.append("Para não receber mais, desative \"Resumo semanal por e-mail\" em Configurações.\n");
        return texto.toString();
    }
}
