package com.cti.provedor.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cti.provedor.dto.UsuarioDTO;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public Usuario criar(UsuarioDTO dto) {
        String senhaHash = encoder.encode(dto.getSenha());
        Usuario usuario = new Usuario(null, dto.getNome(), dto.getEmail(), senhaHash);
        return repository.save(usuario);
    }

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Usuario atualizar(Long id, UsuarioDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenhaHash(encoder.encode(dto.getSenha()));
        }

        return repository.save(usuario);
    }

    public void remover(Long id) {
        repository.deleteById(id);
    }
}
