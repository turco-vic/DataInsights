package com.cti.provedor.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.dto.SenhaDTOs;
import com.cti.provedor.exception.RegraNegocioException;
import com.cti.provedor.model.RedefinicaoSenha;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.RedefinicaoSenhaRepository;
import com.cti.provedor.repository.UsuarioRepository;

/** "Esqueci minha senha": envia um link de uso único, válido por 30 minutos. */
@Service
public class RecuperacaoSenhaService {

    private static final int VALIDADE_MINUTOS = 30;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final RedefinicaoSenhaRepository repository;
    private final PasswordEncoder encoder;
    private final EmailService email;
    private final EventoService eventos;
    private final String frontendUrl;

    public RecuperacaoSenhaService(UsuarioRepository usuarioRepository, RedefinicaoSenhaRepository repository,
            PasswordEncoder encoder, EmailService email, EventoService eventos,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.repository = repository;
        this.encoder = encoder;
        this.email = email;
        this.eventos = eventos;
        this.frontendUrl = frontendUrl;
    }

    /** Não revela se o e-mail existe: a resposta é sempre a mesma. */
    @Transactional
    public void solicitar(SenhaDTOs.EsqueciSenha dto) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dto.email().trim()).orElse(null);
        if (usuario == null) {
            return;
        }

        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RedefinicaoSenha pedido = new RedefinicaoSenha();
        pedido.setUsuario(usuario);
        pedido.setTokenHash(hash(token));
        pedido.setExpiraEm(LocalDateTime.now().plusMinutes(VALIDADE_MINUTOS));
        repository.save(pedido);

        String link = frontendUrl + "/redefinir-senha?token=" + token;
        email.enviar(usuario.getEmail(), "DataInsights: redefinição de senha", """
                Olá, %s.

                Recebemos um pedido para redefinir a sua senha. Use o link abaixo em até %d minutos:

                %s

                Se não foi você, ignore este e-mail: a sua senha continua a mesma.
                """.formatted(usuario.getNome(), VALIDADE_MINUTOS, link));
    }

    @Transactional
    public void redefinir(SenhaDTOs.RedefinirSenha dto) {
        RedefinicaoSenha pedido = repository.findByTokenHash(hash(dto.token()))
                .filter(p -> p.getUsadoEm() == null && p.getExpiraEm().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RegraNegocioException("Link inválido ou expirado. Peça um novo."));

        Usuario usuario = pedido.getUsuario();
        usuario.setSenhaHash(encoder.encode(dto.novaSenha()));
        usuario.setSenhaAlteradaEm(LocalDateTime.now());
        usuarioRepository.save(usuario);

        pedido.setUsadoEm(LocalDateTime.now());
        repository.save(pedido);
        eventos.registrar(EventoService.SENHA_REDEFINIDA, "Senha redefinida por link: " + usuario.getEmail());
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
