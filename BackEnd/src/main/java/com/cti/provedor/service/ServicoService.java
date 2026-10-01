package com.cti.provedor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.dto.ServicoDTO;
import com.cti.provedor.exception.ConflitoException;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.model.Servico;
import com.cti.provedor.repository.ContratoRepository;
import com.cti.provedor.repository.ServicoRepository;

@Service
public class ServicoService {

    private final ServicoRepository repository;
    private final ContratoRepository contratoRepository;

    public ServicoService(ServicoRepository repository, ContratoRepository contratoRepository) {
        this.repository = repository;
        this.contratoRepository = contratoRepository;
    }

    public List<Servico> listar() {
        return repository.findAll();
    }

    public Servico buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));
    }

    @Transactional
    public Servico criar(ServicoDTO dto) {
        String nome = dto.getNome().trim();
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Serviço já cadastrado");
        }
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setCategoria(dto.getCategoria());
        return repository.save(servico);
    }

    @Transactional
    public Servico atualizar(Long id, ServicoDTO dto) {
        Servico servico = buscarPorId(id);
        String nome = dto.getNome().trim();
        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Serviço já cadastrado");
        }
        servico.setNome(nome);
        servico.setCategoria(dto.getCategoria());
        return repository.save(servico);
    }

    @Transactional
    public void excluir(Long id) {
        Servico servico = buscarPorId(id);
        long contratos = contratoRepository.countByServicoId(id);
        if (contratos > 0) {
            throw new ConflitoException("Serviço possui " + contratos + " contrato(s) vinculado(s)");
        }
        repository.delete(servico);
    }
}
