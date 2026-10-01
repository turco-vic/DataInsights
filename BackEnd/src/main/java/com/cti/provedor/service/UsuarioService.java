package com.cti.provedor.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.config.UsuarioLogado;
import com.cti.provedor.dto.CadastroDTO;
import com.cti.provedor.dto.LoginDTO;
import com.cti.provedor.dto.LoginRespostaDTO;
import com.cti.provedor.dto.UsuarioDTO;
import com.cti.provedor.exception.AuthException;
import com.cti.provedor.exception.ConflitoException;
import com.cti.provedor.exception.ProibidoException;
import com.cti.provedor.exception.RecursoNaoEncontradoException;
import com.cti.provedor.exception.RegraNegocioException;
import com.cti.provedor.model.Papel;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.RedefinicaoSenhaRepository;
import com.cti.provedor.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final RedefinicaoSenhaRepository redefinicaoRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final EventoService eventos;
    private final boolean cadastroAberto;
    private final Papel papelPadraoCadastro;

    public UsuarioService(UsuarioRepository repository, RedefinicaoSenhaRepository redefinicaoRepository,
            PasswordEncoder encoder, JwtService jwtService, EventoService eventos,
            @Value("${app.cadastro.aberto}") boolean cadastroAberto,
            @Value("${app.cadastro.papel-padrao}") Papel papelPadraoCadastro) {
        this.repository = repository;
        this.redefinicaoRepository = redefinicaoRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.eventos = eventos;
        this.cadastroAberto = cadastroAberto;
        this.papelPadraoCadastro = papelPadraoCadastro;
    }

    @Transactional
    public LoginRespostaDTO login(LoginDTO dto) {
        String email = normalizarEmail(dto.getEmail());
        Usuario usuario = repository.findByEmailIgnoreCase(email).orElse(null);
        if (usuario == null || usuario.getSenhaHash() == null || !encoder.matches(dto.getSenha(), usuario.getSenhaHash())) {
            eventos.registrar(EventoService.LOGIN_FALHOU, "Tentativa de login de usuário: " + email);
            throw new AuthException();
        }
        if (usuario.getPapel() == null) {
            usuario.setPapel(Papel.ADMIN);
        }
        usuario.setUltimoAcesso(LocalDateTime.now());
        repository.save(usuario);
        eventos.registrar(EventoService.LOGIN, "Usuário entrou: " + usuario.getEmail());
        return resposta(usuario, dto.isManterConectado());
    }

    /** Gera um token novo para quem já está logado (renovação da sessão). */
    public LoginRespostaDTO renovar(UsuarioLogado logado) {
        String token = jwtService.gerar(logado.id(), logado.nome(), logado.email(), logado.tipo(),
                logado.manterConectado());
        return new LoginRespostaDTO(token, logado.id(), logado.nome(), logado.email(), logado.tipo(), logado.papel(),
                logado.manterConectado());
    }

    /** Cadastro público. A primeira conta do sistema vira administradora. */
    @Transactional
    public Usuario cadastrar(CadastroDTO dto) {
        boolean primeiraConta = repository.count() == 0;
        if (!cadastroAberto && !primeiraConta) {
            throw new ProibidoException("O cadastro está fechado. Peça acesso a um administrador.");
        }
        String email = normalizarEmail(dto.getEmail());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("E-mail já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setEmpresa(vazioParaNulo(dto.getEmpresa()));
        usuario.setSenhaHash(encoder.encode(dto.getSenha()));
        usuario.setPapel(primeiraConta ? Papel.ADMIN : papelPadraoCadastro);
        Usuario salvo = repository.save(usuario);
        eventos.registrar(EventoService.CADASTRO, "Nova conta: " + email + " (" + salvo.getPapel() + ")");
        return salvo;
    }

    /** Criação feita por um administrador na tela de Usuários. */
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
        preencher(usuario, dto, email);
        usuario.setPapel(dto.getPapel() == null ? Papel.LEITOR : dto.getPapel());
        usuario.setSenhaHash(encoder.encode(dto.getSenha()));
        Usuario salvo = repository.save(usuario);
        eventos.registrar(EventoService.USUARIO_CRIADO, "Conta criada por administrador: " + email);
        return salvo;
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
        if (dto.getPapel() != null && dto.getPapel() != Papel.ADMIN && usuario.getPapel() == Papel.ADMIN) {
            exigirOutroAdmin();
        }

        preencher(usuario, dto, email);
        if (dto.getPapel() != null) {
            usuario.setPapel(dto.getPapel());
        }
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenhaHash(encoder.encode(dto.getSenha()));
            usuario.setSenhaAlteradaEm(LocalDateTime.now());
        }
        return repository.save(usuario);
    }

    @Transactional
    public void remover(Long id, UsuarioLogado logado) {
        Usuario usuario = buscarPorId(id);
        if (JwtService.TIPO_USUARIO.equals(logado.tipo()) && id.equals(logado.id())) {
            throw new RegraNegocioException("Você não pode excluir a própria conta");
        }
        if (usuario.getPapel() == Papel.ADMIN) {
            exigirOutroAdmin();
        }
        redefinicaoRepository.deleteByUsuarioId(id);
        repository.delete(usuario);
        eventos.registrar(EventoService.USUARIO_EXCLUIDO, "Conta excluída: " + usuario.getEmail());
    }

    private void exigirOutroAdmin() {
        if (repository.countByPapel(Papel.ADMIN) <= 1) {
            throw new ConflitoException("É preciso manter ao menos um administrador");
        }
    }

    private void preencher(Usuario usuario, UsuarioDTO dto, String email) {
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setEmpresa(vazioParaNulo(dto.getEmpresa()));
        usuario.setCargo(vazioParaNulo(dto.getCargo()));
    }

    private LoginRespostaDTO resposta(Usuario usuario, boolean manterConectado) {
        String token = jwtService.gerar(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                JwtService.TIPO_USUARIO, manterConectado);
        return new LoginRespostaDTO(token, usuario.getId(), usuario.getNome(), usuario.getEmail(),
                JwtService.TIPO_USUARIO, usuario.getPapel().name(), manterConectado);
    }

    static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    static String vazioParaNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
