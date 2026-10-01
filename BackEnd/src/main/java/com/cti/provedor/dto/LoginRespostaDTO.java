package com.cti.provedor.dto;

/** papel: ADMIN, ANALISTA, LEITOR ou CONSULTOR. */
public record LoginRespostaDTO(String token, Long id, String nome, String email, String tipo, String papel,
        boolean manterConectado) {
}
