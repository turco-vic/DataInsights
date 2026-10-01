package com.cti.provedor.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.dto.InsightDTO;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.exception.RegraNegocioException;
import com.cti.provedor.model.Cliente;
import com.cti.provedor.model.Contrato;
import com.cti.provedor.model.Insight;
import com.cti.provedor.repository.ClienteRepository;
import com.cti.provedor.repository.ContratoRepository;
import com.cti.provedor.repository.InsightRepository;

@Service
public class InsightService {

    public static final String TIPO_OPORTUNIDADE = "Oportunidade";
    public static final String TIPO_REVISAR_NIVEL = "Revisar nível";
    public static final String TIPO_SEM_CONTRATO = "Sem contrato ativo";
    public static final String TIPO_SEM_CONSULTOR = "Sem consultor";

    private final InsightRepository repository;
    private final ClienteRepository clienteRepository;
    private final ContratoRepository contratoRepository;
    private final ClienteService clienteService;
    private final ContratoService contratoService;
    private final EventoService eventos;

    public InsightService(InsightRepository repository, ClienteRepository clienteRepository,
            ContratoRepository contratoRepository, ClienteService clienteService, ContratoService contratoService,
            EventoService eventos) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.contratoRepository = contratoRepository;
        this.clienteService = clienteService;
        this.contratoService = contratoService;
        this.eventos = eventos;
    }

    public List<Insight> listar(Long clienteId) {
        return clienteId == null ? repository.findAllByOrderByIdDesc() : repository.findByClienteIdOrderByIdDesc(clienteId);
    }

    public Insight buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Insight não encontrado"));
    }

    @Transactional
    public Insight criar(InsightDTO dto) {
        Insight insight = new Insight();
        preencher(insight, dto);
        return repository.save(insight);
    }

    @Transactional
    public Insight atualizar(Long id, InsightDTO dto) {
        Insight insight = buscarPorId(id);
        preencher(insight, dto);
        return repository.save(insight);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarPorId(id));
    }

    /**
     * Gera insights por regra para toda a carteira, sem repetir um tipo já existente no mesmo cliente:
     * nível A com no máximo 1 serviço ativo, nível C faturando acima da média do nível A,
     * cliente sem contrato ativo e cliente sem consultor. Devolve quantos foram criados.
     */
    @Transactional
    public int gerarAutomaticos() {
        List<Cliente> clientes = clienteRepository.findAll();
        BigDecimal mediaNivelA = media(clientes.stream()
                .filter(c -> "A".equals(c.getNivel()))
                .map(Cliente::getFaturamentoAnual)
                .filter(Objects::nonNull)
                .toList());

        int criados = 0;
        for (Cliente cliente : clientes) {
            long servicosAtivos = contratoRepository.countByClienteIdAndAtivoTrue(cliente.getId());

            if ("A".equals(cliente.getNivel()) && servicosAtivos == 1) {
                criados += criarSeNovo(cliente, TIPO_OPORTUNIDADE,
                        "Cliente nível A com apenas 1 serviço ativo: há espaço para oferecer outros serviços.");
            }
            if ("C".equals(cliente.getNivel()) && mediaNivelA != null && cliente.getFaturamentoAnual() != null
                    && cliente.getFaturamentoAnual().compareTo(mediaNivelA) > 0) {
                criados += criarSeNovo(cliente, TIPO_REVISAR_NIVEL, "Faturamento anual de "
                        + moeda(cliente.getFaturamentoAnual()) + " está acima da média dos clientes nível A ("
                        + moeda(mediaNivelA) + "). Vale revisar o nível.");
            }
            if (servicosAtivos == 0) {
                criados += criarSeNovo(cliente, TIPO_SEM_CONTRATO,
                        "Cliente sem nenhum contrato ativo: confirme se ainda é cliente ou se há risco de perda.");
            }
            if (cliente.getConsultor() == null) {
                criados += criarSeNovo(cliente, TIPO_SEM_CONSULTOR, "Cliente sem consultor responsável.");
            }
        }
        if (criados > 0) {
            eventos.registrar(EventoService.INSIGHTS_GERADOS, criados + " insight(s) gerado(s) automaticamente");
        }
        return criados;
    }

    private int criarSeNovo(Cliente cliente, String tipo, String descricao) {
        if (repository.existsByClienteIdAndTipo(cliente.getId(), tipo)) {
            return 0;
        }
        Insight insight = new Insight();
        insight.setCliente(cliente);
        insight.setTipo(tipo);
        insight.setDescricao(descricao);
        repository.save(insight);
        return 1;
    }

    private static BigDecimal media(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return null;
        }
        BigDecimal soma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(BigDecimal.valueOf(valores.size()), 2, RoundingMode.HALF_UP);
    }

    private static String moeda(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        formato.setMaximumFractionDigits(0);
        return formato.format(valor);
    }

    private void preencher(Insight insight, InsightDTO dto) {
        Cliente cliente = clienteService.buscarPorId(dto.getClienteId());
        Contrato contrato = null;
        if (dto.getContratoId() != null) {
            contrato = contratoService.buscarPorId(dto.getContratoId());
            if (!contrato.getCliente().getId().equals(cliente.getId())) {
                throw new RegraNegocioException("O contrato informado não pertence a este cliente");
            }
        }
        insight.setTipo(dto.getTipo().trim());
        insight.setDescricao(dto.getDescricao().trim());
        insight.setCliente(cliente);
        insight.setContrato(contrato);
    }
}
