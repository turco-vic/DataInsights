package com.cti.provedor.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.config.UsuarioLogado;
import com.cti.provedor.dto.ClienteImportacaoDTO;
import com.cti.provedor.dto.ImportacaoDTO;
import com.cti.provedor.dto.ImportacaoRespostaDTOs;
import com.cti.provedor.dto.ImportacaoResultadoDTO;
import com.cti.provedor.dto.InconsistenciaDTO;
import com.cti.provedor.exception.PlanilhaInconsistenteException;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.exception.RegraNegocioException;
import com.cti.provedor.model.Importacao;
import com.cti.provedor.repository.ImportacaoRepository;

import tools.jackson.databind.json.JsonMapper;

/** Importa uma planilha validada e guarda o histórico dela (Planilhas enviadas / Relatório do dataset). */
@Service
public class ImportacaoService {

    /** Colunas do CSV, na ordem e com os nomes da planilha modelo (campo do JSON → coluna). */
    private static final Map<String, String> COLUNAS_CSV = new LinkedHashMap<>();
    static {
        COLUNAS_CSV.put("codigoCliente", "codigo_cliente");
        COLUNAS_CSV.put("nomeCliente", "nome_cliente");
        COLUNAS_CSV.put("email", "email");
        COLUNAS_CSV.put("consultor", "consultor");
        COLUNAS_CSV.put("segmento", "segmento");
        COLUNAS_CSV.put("nivelCliente", "nivel_cliente");
        COLUNAS_CSV.put("faturamentoAnual", "faturamento_anual");
        COLUNAS_CSV.put("servicosContratados", "servicos_contratados");
        COLUNAS_CSV.put("dataContratacao", "data_contratacao");
        COLUNAS_CSV.put("cidade", "cidade");
        COLUNAS_CSV.put("uf", "uf");
    }

    private final ImportacaoRepository repository;
    private final ClienteService clienteService;
    private final ClienteValidacaoService validacao;
    private final InsightService insightService;
    private final EventoService eventos;
    private final JsonMapper json;

    public ImportacaoService(ImportacaoRepository repository, ClienteService clienteService,
            ClienteValidacaoService validacao, InsightService insightService, EventoService eventos, JsonMapper json) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.validacao = validacao;
        this.insightService = insightService;
        this.eventos = eventos;
        this.json = json;
    }

    /**
     * Importa a planilha somente se ela estiver 100% consistente. Se o front apontou algum erro, ou se o
     * back-end encontrar alguma inconsistência, nada é gravado e todas as inconsistências são devolvidas.
     */
    @Transactional
    public ImportacaoRespostaDTOs.Detalhe registrar(ImportacaoDTO dto, UsuarioLogado logado) {
        String nomeArquivo = dto.getNomeArquivo().trim();
        List<ClienteImportacaoDTO> clientes;
        try {
            exigirPlanilhaCompleta(dto);
            clientes = validacao.validarEPreparar(dto.getClientes());
        } catch (PlanilhaInconsistenteException e) {
            eventos.registrar(EventoService.IMPORTACAO_RECUSADA, "%s por %s: %d inconsistência(s), nada importado"
                    .formatted(nomeArquivo, logado.nome(), e.getInconsistencias().size()));
            throw e;
        }

        ImportacaoResultadoDTO resultado = clienteService.gravarLote(clientes);

        Importacao importacao = new Importacao();
        importacao.setNomeArquivo(nomeArquivo);
        importacao.setAutorId(logado.id());
        importacao.setAutorNome(logado.nome());
        importacao.setEnviadaEm(LocalDateTime.now());
        importacao.setTotalLinhas(clientes.size());
        importacao.setLinhasComErro(0);
        importacao.setCriados(resultado.getCriados());
        importacao.setAtualizados(resultado.getAtualizados());
        importacao.setRejeitados(0);
        importacao.setStatus("processado");
        importacao.setErrosJson("[]");
        importacao.setRejeitadosJson("[]");
        importacao.setClientesJson(json.writeValueAsString(clientes));
        repository.save(importacao);

        insightService.gerarAutomaticos();
        eventos.registrar(EventoService.IMPORTACAO, "%s por %s: %d criado(s), %d atualizado(s)"
                .formatted(importacao.getNomeArquivo(), logado.nome(), importacao.getCriados(),
                        importacao.getAtualizados()));
        return detalhe(importacao);
    }

    /** Erros apontados pelo front ou linhas faltando também impedem a importação. */
    private static void exigirPlanilhaCompleta(ImportacaoDTO dto) {
        if (!dto.getErros().isEmpty()) {
            throw new PlanilhaInconsistenteException(dto.getErros().stream()
                    .map(e -> new InconsistenciaDTO(e.linha(), null, e.campo(), e.descricao()))
                    .toList());
        }
        if (dto.getTotalLinhas() > dto.getClientes().size()) {
            throw new RegraNegocioException("A planilha tem %d linha(s), mas só %d foram enviadas. Envie a planilha inteira."
                    .formatted(dto.getTotalLinhas(), dto.getClientes().size()));
        }
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

    /** Dataset limpo de uma importação em CSV (UTF-8, separado por vírgula), para a etapa de Ciência de Dados. */
    public String exportarCsv(Long id) {
        Importacao importacao = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Planilha não encontrada"));
        StringBuilder csv = new StringBuilder(String.join(",", COLUNAS_CSV.values())).append('\n');
        for (Object linha : lista(importacao.getClientesJson())) {
            Map<?, ?> cliente = (Map<?, ?>) linha;
            csv.append(COLUNAS_CSV.keySet().stream()
                    .map(campo -> celulaCsv(cliente.get(campo)))
                    .collect(Collectors.joining(",")))
                    .append('\n');
        }
        return csv.toString();
    }

    /** Nome do arquivo baixado: o da planilha, com extensão .csv. */
    public String nomeCsv(Long id) {
        String nome = repository.findById(id).map(Importacao::getNomeArquivo).orElse("dataset");
        return nome.replaceAll("\\.[^.]+$", "").replaceAll("[^\\p{L}\\p{N}._-]+", "_") + ".csv";
    }

    private static String celulaCsv(Object valor) {
        String texto;
        if (valor == null) {
            texto = "";
        } else if (valor instanceof List<?> itens) {
            texto = itens.stream().map(String::valueOf).collect(Collectors.joining(";"));
        } else if (valor instanceof Number numero) {
            texto = new BigDecimal(numero.toString()).stripTrailingZeros().toPlainString();
        } else {
            texto = valor.toString();
        }
        boolean precisaAspas = texto.contains(",") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r");
        return precisaAspas ? "\"" + texto.replace("\"", "\"\"") + "\"" : texto;
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
