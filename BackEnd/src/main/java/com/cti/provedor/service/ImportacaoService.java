package com.cti.provedor.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.config.UsuarioLogado;
import com.cti.provedor.dto.ClienteImportacaoDTO;
import com.cti.provedor.dto.ImportacaoDTO;
import com.cti.provedor.dto.ImportacaoRespostaDTOs;
import com.cti.provedor.dto.ImportacaoResultadoDTO;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.model.Importacao;
import com.cti.provedor.repository.ImportacaoRepository;

import tools.jackson.databind.json.JsonMapper;

/** Importa uma planilha validada e guarda o histórico dela (Planilhas enviadas / Relatório do dataset). */
@Service
public class ImportacaoService {

    private final ImportacaoRepository repository;
    private final ClienteService clienteService;
    private final InsightService insightService;
    private final EventoService eventos;
    private final JsonMapper json;

    public ImportacaoService(ImportacaoRepository repository, ClienteService clienteService,
            InsightService insightService, EventoService eventos, JsonMapper json) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.insightService = insightService;
        this.eventos = eventos;
        this.json = json;
    }

    @Transactional
    public ImportacaoRespostaDTOs.Detalhe registrar(ImportacaoDTO dto, UsuarioLogado logado) {
        ImportacaoResultadoDTO resultado = clienteService.importar(dto.getClientes());

        Set<String> recusados = resultado.getRejeitados().stream()
                .map(ImportacaoResultadoDTO.Rejeitado::codigoCliente)
                .collect(Collectors.toSet());
        List<ClienteImportacaoDTO> aceitos = dto.getClientes().stream()
                .filter(c -> !recusados.contains(c.getCodigoCliente()))
                .toList();

        Importacao importacao = new Importacao();
        importacao.setNomeArquivo(dto.getNomeArquivo().trim());
        importacao.setAutorId(logado.id());
        importacao.setAutorNome(logado.nome());
        importacao.setEnviadaEm(LocalDateTime.now());
        importacao.setTotalLinhas(Math.max(dto.getTotalLinhas(), dto.getClientes().size()));
        importacao.setLinhasComErro(importacao.getTotalLinhas() - dto.getClientes().size());
        importacao.setCriados(resultado.getCriados());
        importacao.setAtualizados(resultado.getAtualizados());
        importacao.setRejeitados(resultado.getRejeitados().size());
        importacao.setStatus(dto.getErros().isEmpty() && resultado.getRejeitados().isEmpty() ? "processado" : "erro");
        importacao.setErrosJson(json.writeValueAsString(dto.getErros()));
        importacao.setRejeitadosJson(json.writeValueAsString(resultado.getRejeitados()));
        importacao.setClientesJson(json.writeValueAsString(aceitos));
        repository.save(importacao);

        insightService.gerarAutomaticos();
        eventos.registrar(EventoService.IMPORTACAO, "%s por %s: %d criado(s), %d atualizado(s), %d recusado(s)"
                .formatted(importacao.getNomeArquivo(), logado.nome(), importacao.getCriados(),
                        importacao.getAtualizados(), importacao.getRejeitados()));
        return detalhe(importacao);
    }

    public List<ImportacaoRespostaDTOs.Resumo> listar() {
        return repository.findAllByOrderByEnviadaEmDesc().stream().map(ImportacaoService::resumo).toList();
    }

    public ImportacaoRespostaDTOs.Detalhe buscar(Long id) {
        return detalhe(repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Planilha não encontrada")));
    }

    /** Apaga só o histórico: os clientes importados continuam no banco. */
    @Transactional
    public void excluir(Long id) {
        Importacao importacao = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Planilha não encontrada"));
        repository.delete(importacao);
    }

    static ImportacaoRespostaDTOs.Resumo resumo(Importacao i) {
        return new ImportacaoRespostaDTOs.Resumo(i.getId(), i.getNomeArquivo(), i.getAutorNome(), i.getEnviadaEm(),
                i.getTotalLinhas(), i.getLinhasComErro(), i.getCriados(), i.getAtualizados(), i.getRejeitados(),
                i.getStatus());
    }

    private ImportacaoRespostaDTOs.Detalhe detalhe(Importacao i) {
        return new ImportacaoRespostaDTOs.Detalhe(resumo(i), lista(i.getErrosJson()), lista(i.getRejeitadosJson()),
                lista(i.getClientesJson()));
    }

    @SuppressWarnings("unchecked")
    private List<Object> lista(String conteudo) {
        return conteudo == null || conteudo.isBlank() ? List.of() : json.readValue(conteudo, List.class);
    }
}
