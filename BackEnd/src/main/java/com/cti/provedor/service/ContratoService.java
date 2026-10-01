package com.cti.provedor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.dto.ContratoDTO;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.exception.RegraNegocioException;
import com.cti.provedor.model.Contrato;
import com.cti.provedor.repository.ContratoRepository;
import com.cti.provedor.repository.InsightRepository;

@Service
public class ContratoService {

    private final ContratoRepository repository;
    private final InsightRepository insightRepository;
    private final ClienteService clienteService;
    private final ServicoService servicoService;

    public ContratoService(ContratoRepository repository, InsightRepository insightRepository,
            ClienteService clienteService, ServicoService servicoService) {
        this.repository = repository;
        this.insightRepository = insightRepository;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
    }

    public List<Contrato> listar(Long clienteId) {
        return clienteId == null ? repository.findAll() : repository.findByClienteId(clienteId);
    }

    public Contrato buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contrato não encontrado"));
    }

    @Transactional
    public Contrato criar(ContratoDTO dto) {
        Contrato contrato = new Contrato();
        preencher(contrato, dto);
        return repository.save(contrato);
    }

    @Transactional
    public Contrato atualizar(Long id, ContratoDTO dto) {
        Contrato contrato = buscarPorId(id);
        preencher(contrato, dto);
        return repository.save(contrato);
    }

    /** Remove o contrato junto com os insights ligados a ele. */
    @Transactional
    public void excluir(Long id) {
        Contrato contrato = buscarPorId(id);
        insightRepository.deleteByContratoId(id);
        repository.delete(contrato);
    }

    private void preencher(Contrato contrato, ContratoDTO dto) {
        if (dto.getDataFim() != null && dto.getDataFim().isBefore(dto.getDataInicio())) {
            throw new RegraNegocioException("Data de fim não pode ser anterior à data de início");
        }
        contrato.setCliente(clienteService.buscarPorId(dto.getClienteId()));
        contrato.setServico(servicoService.buscarPorId(dto.getServicoId()));
        contrato.setDataInicio(dto.getDataInicio());
        contrato.setDataFim(dto.getDataFim());
        contrato.setValor(dto.getValor());
        contrato.setAtivo(dto.getAtivo() == null ? Boolean.TRUE : dto.getAtivo());
    }
}
