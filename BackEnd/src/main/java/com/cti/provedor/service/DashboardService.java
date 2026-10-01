package com.cti.provedor.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cti.provedor.dto.DashboardDTO;
import com.cti.provedor.dto.DashboardDTO.ClienteDestaque;
import com.cti.provedor.dto.DashboardDTO.Contagem;
import com.cti.provedor.model.Cliente;
import com.cti.provedor.repository.ClienteRepository;
import com.cti.provedor.repository.ContratoRepository;
import com.cti.provedor.repository.ImportacaoRepository;
import com.cti.provedor.repository.InsightRepository;

/** Indicadores da carteira inteira, calculados sobre os clientes do banco. */
@Service
public class DashboardService {

    private static final String[] MESES = { "JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT",
            "NOV", "DEZ" };
    private static final int MAXIMO_MESES = 36;
    private static final int TOP = 8;

    private final ClienteRepository clienteRepository;
    private final ContratoRepository contratoRepository;
    private final InsightRepository insightRepository;
    private final ImportacaoRepository importacaoRepository;

    public DashboardService(ClienteRepository clienteRepository, ContratoRepository contratoRepository,
            InsightRepository insightRepository, ImportacaoRepository importacaoRepository) {
        this.clienteRepository = clienteRepository;
        this.contratoRepository = contratoRepository;
        this.insightRepository = insightRepository;
        this.importacaoRepository = importacaoRepository;
    }

    public DashboardDTO calcular() {
        List<Cliente> clientes = clienteRepository.findAll();

        List<BigDecimal> faturamentos = clientes.stream()
                .map(Cliente::getFaturamentoAnual).filter(Objects::nonNull).toList();
        BigDecimal total = faturamentos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal medio = faturamentos.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(faturamentos.size()), 2, RoundingMode.HALF_UP);

        List<Contagem> porNivel = List.of("A", "B", "C").stream()
                .map(n -> new Contagem(n, clientes.stream().filter(c -> n.equals(c.getNivel())).count()))
                .toList();

        List<Contagem> servicos = contratoRepository.rankingServicosAtivos().stream()
                .limit(TOP)
                .map(l -> new Contagem((String) l[0], (Long) l[1]))
                .toList();

        List<ClienteDestaque> maiores = clientes.stream()
                .filter(c -> c.getFaturamentoAnual() != null)
                .sorted(Comparator.comparing(Cliente::getFaturamentoAnual).reversed())
                .limit(TOP)
                .map(c -> new ClienteDestaque(c.getId(), c.getNome(), c.getSegmento(),
                        c.getConsultor() == null ? null : c.getConsultor().getNome(), c.getNivel(),
                        c.getFaturamentoAnual()))
                .toList();

        return new DashboardDTO(
                clientes.size(),
                total,
                medio,
                contratoRepository.countByAtivoTrue(),
                insightRepository.count(),
                porNivel,
                contar(clientes, c -> c.getSegmento() == null ? "Não informado" : c.getSegmento()),
                servicos,
                contratacoesPorMes(clientes),
                maiores,
                importacaoRepository.findFirstByOrderByEnviadaEmDesc().map(ImportacaoService::resumo).orElse(null));
    }

    private static List<Contagem> contar(List<Cliente> clientes, Function<Cliente, String> chave) {
        return clientes.stream()
                .collect(Collectors.groupingBy(chave, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new Contagem(e.getKey(), e.getValue()))
                .toList();
    }

    /** Meses contínuos entre a primeira e a última contratação, limitados aos 36 mais recentes. */
    private static List<Contagem> contratacoesPorMes(List<Cliente> clientes) {
        Map<YearMonth, Long> porMes = clientes.stream()
                .map(Cliente::getDataContratacao)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(YearMonth::from, Collectors.counting()));
        if (porMes.isEmpty()) {
            return List.of();
        }
        YearMonth fim = porMes.keySet().stream().max(Comparator.naturalOrder()).orElseThrow();
        YearMonth inicio = porMes.keySet().stream().min(Comparator.naturalOrder()).orElseThrow();
        if (inicio.isBefore(fim.minusMonths(MAXIMO_MESES - 1))) {
            inicio = fim.minusMonths(MAXIMO_MESES - 1);
        }

        Map<String, Long> resultado = new LinkedHashMap<>();
        for (YearMonth mes = inicio; !mes.isAfter(fim); mes = mes.plusMonths(1)) {
            String rotulo = MESES[mes.getMonthValue() - 1] + "/" + String.valueOf(mes.getYear()).substring(2);
            resultado.put(rotulo, porMes.getOrDefault(mes, 0L));
        }
        List<Contagem> lista = new ArrayList<>();
        resultado.forEach((rotulo, qtd) -> lista.add(new Contagem(rotulo, qtd)));
        return lista;
    }
}
