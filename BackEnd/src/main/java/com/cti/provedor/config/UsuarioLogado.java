package com.cti.provedor.config;

/**
 * Quem fez a requisição, montado a partir do token e conferido no banco.
 * tipo: USUARIO ou CONSULTOR. papel: ADMIN, ANALISTA, LEITOR ou CONSULTOR.
 */
public record UsuarioLogado(Long id, String tipo, String nome, String email, String papel, boolean manterConectado) {
}
