package com.cti.provedor.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.dto.LoginDTO;
import com.cti.provedor.dto.LoginRespostaDTO;
import com.cti.provedor.dto.UsuarioDTO;
import com.cti.provedor.exception.AuthException;
import com.cti.provedor.exception.ConflitoException;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.exception.RegraNegocioException;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder, JwtService jwtService) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public LoginRespostaDTO login(LoginDTO dto) {
        Usuario usuario = repository.findByEmailIgnoreCase(dto.getEmail().trim())
                .orElseThrow(AuthException::new);
        if (usuario.getSenhaHash() == null || !encoder.matches(dto.getSenha(), usuario.getSenhaHash())) {
            throw new AuthException();
        }
        String token = jwtService.gerar(usuario.getId(), usuario.getNome(), usuario.getEmail(), JwtService.TIPO_USUARIO);
        return new LoginRespostaDTO(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), JwtService.TIPO_USUARIO);
    }

    @Transactional
    public Usuario criar(UsuarioDTO dto) {
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new RegraNegocioException("Senha é obrigatória");
        }
        String email = normalizarEmail(dto.getEmail());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("E-mail já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(encoder.encode(dto.getSenha()));
        return repository.save(usuario);
    }

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioDTO dto) {
        Usuario usuario = buscarPorId(id);
        String email = normalizarEmail(dto.getEmail());
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflitoException("E-mail já cadastrado");
        }

        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenhaHash(encoder.encode(dto.getSenha()));
        }
        return repository.save(usuario);
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(buscarPorId(id));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
