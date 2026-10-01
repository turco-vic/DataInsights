package com.cti.provedor.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    public static final String TIPO_USUARIO = "USUARIO";
    public static final String TIPO_CONSULTOR = "CONSULTOR";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.expiration-longa-ms}")
    private long expirationLongaMs;

    /** manterConectado = true gera um token de longa duração ("Manter conectado"). */
    public String gerar(Long id, String nome, String email, String tipo, boolean manterConectado) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + (manterConectado ? expirationLongaMs : expirationMs));

        return Jwts.builder()
                .subject(email)
                .claim("id", id)
                .claim("nome", nome)
                .claim("tipo", tipo)
                .claim("manter", manterConectado)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chave())
                .compact();
    }

    /** Devolve as claims do token, ou null se ele for inválido ou estiver expirado. */
    public Claims validar(String token) {
        try {
            return Jwts.parser().verifyWith(chave()).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private SecretKey chave() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
