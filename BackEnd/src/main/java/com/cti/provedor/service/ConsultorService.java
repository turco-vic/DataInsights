package com.cti.provedor.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.model.Consultor;
import com.cti.provedor.repository.ConsultorRepository;

@Service
public class ConsultorService {

    private final ConsultorRepository repository;
    private final PasswordEncoder encoder;

    public ConsultorService(ConsultorRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional
    public Consultor criar(Consultor consultor) {
        validar(consultor);
        if (repository.findByEmail(consultor.getEmail()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado");
        }
        consultor.setId(null);
        consultor.setSenha(encoder.encode(consultor.getSenha()));
        return repository.save(consultor);
    }

    public Consultor login(String email, String senha) {
        Consultor consultor = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Consultor não encontrado"));
        if (senha == null || consultor.getSenha() == null || !encoder.matches(senha, consultor.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }
        return consultor;
    }

    public List<Consultor> listar() {
        return repository.findAll();
    }

    public Consultor buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultor não encontrado"));
    }

    @Transactional
    public Consultor atualizar(Long id, Consultor dados) {
        Consultor consultor = buscarPorId(id);
        validar(dados);
        repository.findByEmail(dados.getEmail())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new RuntimeException("E-mail já cadastrado");
                });
        consultor.setNome(dados.getNome());
        consultor.setMatricula(dados.getMatricula());
        consultor.setEmail(dados.getEmail());
        consultor.setSenha(encoder.encode(dados.getSenha()));
        return repository.save(consultor);
    }

    @Transactional
    public void excluir(Long id) {
        Consultor consultor = buscarPorId(id);
        repository.deleteById(consultor.getId());
    }

    private void validar(Consultor consultor) {
        if (consultor.getNome() == null || consultor.getNome().isBlank()) {
            throw new RuntimeException("Nome é obrigatório");
        }
        if (consultor.getEmail() == null || consultor.getEmail().isBlank()) {
            throw new RuntimeException("E-mail é obrigatório");
        }
        if (consultor.getSenha() == null || consultor.getSenha().isBlank()) {
            throw new RuntimeException("Senha é obrigatória");
        }
    }
}
