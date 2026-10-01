package com.cti.provedor.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.config.UsuarioLogado;
import com.cti.provedor.dto.PerfilDTO;
import com.cti.provedor.dto.PerfilRespostaDTO;
import com.cti.provedor.dto.SenhaDTOs;
import com.cti.provedor.exception.ConflitoException;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.exception.RegraNegocioException;
import com.cti.provedor.model.Consultor;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.ConsultorRepository;
import com.cti.provedor.repository.UsuarioRepository;

/** Perfil e senha da própria pessoa logada (usuário ou consultor). */
@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final ConsultorRepository consultorRepository;
    private final PasswordEncoder encoder;
    private final EventoService eventos;

    public PerfilService(UsuarioRepository usuarioRepository, ConsultorRepository consultorRepository,
            PasswordEncoder encoder, EventoService eventos) {
        this.usuarioRepository = usuarioRepository;
        this.consultorRepository = consultorRepository;
        this.encoder = encoder;
        this.eventos = eventos;
    }

    public PerfilRespostaDTO obter(UsuarioLogado logado) {
        if (ehConsultor(logado)) {
            return resposta(consultor(logado));
        }
        return resposta(usuario(logado));
    }

    @Transactional
    public PerfilRespostaDTO atualizar(UsuarioLogado logado, PerfilDTO dto) {
        String email = UsuarioService.normalizarEmail(dto.getEmail());

        if (ehConsultor(logado)) {
            Consultor consultor = consultor(logado);
            if (consultorRepository.existsByEmailIgnoreCaseAndIdNot(email, consultor.getId())) {
                throw new ConflitoException("E-mail já cadastrado");
            }
            consultor.setNome(dto.getNome().trim());
            consultor.setEmail(email);
            return resposta(consultorRepository.save(consultor));
        }

        Usuario usuario = usuario(logado);
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, usuario.getId())) {
            throw new ConflitoException("E-mail já cadastrado");
        }
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setEmpresa(UsuarioService.vazioParaNulo(dto.getEmpresa()));
        usuario.setCargo(UsuarioService.vazioParaNulo(dto.getCargo()));
        if (dto.getPrefTratarDados() != null) {
            usuario.setPrefTratarDados(dto.getPrefTratarDados());
        }
        if (dto.getPrefGerarDashboard() != null) {
            usuario.setPrefGerarDashboard(dto.getPrefGerarDashboard());
        }
        if (dto.getPrefResumoSemanal() != null) {
            usuario.setPrefResumoSemanal(dto.getPrefResumoSemanal());
        }
        return resposta(usuarioRepository.save(usuario));
    }

    @Transactional
    public void alterarSenha(UsuarioLogado logado, SenhaDTOs.AlterarSenha dto) {
        if (ehConsultor(logado)) {
            Consultor consultor = consultor(logado);
            conferirSenhaAtual(dto.senhaAtual(), consultor.getSenha());
            consultor.setSenha(encoder.encode(dto.novaSenha()));
            consultorRepository.save(consultor);
        } else {
            Usuario usuario = usuario(logado);
            conferirSenhaAtual(dto.senhaAtual(), usuario.getSenhaHash());
            usuario.setSenhaHash(encoder.encode(dto.novaSenha()));
            usuario.setSenhaAlteradaEm(LocalDateTime.now());
            usuarioRepository.save(usuario);
        }
        eventos.registrar(EventoService.SENHA_ALTERADA, "Senha alterada: " + logado.email());
    }

    private void conferirSenhaAtual(String informada, String hash) {
        if (hash == null || !encoder.matches(informada, hash)) {
            throw new RegraNegocioException("A senha atual não confere");
        }
    }

    private boolean ehConsultor(UsuarioLogado logado) {
        return JwtService.TIPO_CONSULTOR.equals(logado.tipo());
    }

    private Usuario usuario(UsuarioLogado logado) {
        return usuarioRepository.findById(logado.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private Consultor consultor(UsuarioLogado logado) {
        return consultorRepository.findById(logado.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consultor não encontrado"));
    }

    private PerfilRespostaDTO resposta(Usuario u) {
        return new PerfilRespostaDTO(u.getId(), JwtService.TIPO_USUARIO, u.getPapel().name(), u.getNome(),
                u.getEmail(), u.getEmpresa(), u.getCargo(), u.isPrefTratarDados(),
                u.isPrefGerarDashboard(), u.isPrefResumoSemanal(), u.getSenhaAlteradaEm(), u.getUltimoAcesso());
    }

    private PerfilRespostaDTO resposta(Consultor c) {
        return new PerfilRespostaDTO(c.getId(), JwtService.TIPO_CONSULTOR, "CONSULTOR", c.getNome(), c.getEmail(),
                null, null, true, true, false, null, null);
    }
}
