package com.cti.provedor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.dto.InsightDTO;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.exception.RegraNegocioException;
import com.cti.provedor.model.Cliente;
import com.cti.provedor.model.Contrato;
import com.cti.provedor.model.Insight;
import com.cti.provedor.repository.InsightRepository;

@Service
public class InsightService {

    private final InsightRepository repository;
    private final ClienteService clienteService;
    private final ContratoService contratoService;

    public InsightService(InsightRepository repository, ClienteService clienteService,
            ContratoService contratoService) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.contratoService = contratoService;
    }

    public List<Insight> listar(Long clienteId) {
        return clienteId == null ? repository.findAll() : repository.findByClienteId(clienteId);
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
