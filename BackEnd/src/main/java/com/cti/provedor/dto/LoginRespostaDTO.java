package com.cti.provedor.dto;

public record LoginRespostaDTO(String token, Long id, String nome, String email, String tipo) {
}
