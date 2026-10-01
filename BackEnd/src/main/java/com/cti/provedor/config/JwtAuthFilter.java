package com.cti.provedor.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cti.provedor.repository.ConsultorRepository;
import com.cti.provedor.repository.UsuarioRepository;
import com.cti.provedor.service.JwtService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lê o token Bearer e confere no banco se a conta ainda existe. O papel vem do banco, e não do token,
 * para que promoções, rebaixamentos e exclusões valham na hora.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final ConsultorRepository consultorRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository,
            ConsultorRepository consultorRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.consultorRepository = consultorRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIXO)) {
            Claims claims = jwtService.validar(header.substring(PREFIXO.length()));
            UsuarioLogado logado = claims == null ? null : carregar(claims);
            if (logado != null) {
                var papeis = List.of(new SimpleGrantedAuthority("ROLE_" + logado.papel()));
                var autenticacao = new UsernamePasswordAuthenticationToken(logado, null, papeis);
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            }
        }
        chain.doFilter(request, response);
    }

    private UsuarioLogado carregar(Claims claims) {
        Object id = claims.get("id");
        if (!(id instanceof Number numero)) {
            return null;
        }
        String tipo = claims.get("tipo", String.class);
        boolean manter = Boolean.TRUE.equals(claims.get("manter", Boolean.class));

        if (JwtService.TIPO_CONSULTOR.equals(tipo)) {
            return consultorRepository.findById(numero.longValue())
                    .map(c -> new UsuarioLogado(c.getId(), tipo, c.getNome(), c.getEmail(), "CONSULTOR", manter))
                    .orElse(null);
        }
        return usuarioRepository.findById(numero.longValue())
                .filter(u -> u.getPapel() != null)
                .map(u -> new UsuarioLogado(u.getId(), JwtService.TIPO_USUARIO, u.getNome(), u.getEmail(),
                        u.getPapel().name(), manter))
                .orElse(null);
    }
}
