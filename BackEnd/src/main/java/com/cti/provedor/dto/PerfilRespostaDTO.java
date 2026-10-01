package com.cti.provedor.dto;

import java.time.LocalDateTime;

public record PerfilRespostaDTO(
        Long id,
        String tipo,
        String papel,
        String nome,
        String email,
        String empresa,
        String cargo,
        boolean prefTratarDados,
        boolean prefGerarDashboard,
        boolean prefResumoSemanal,
        LocalDateTime senhaAlteradaEm,
        LocalDateTime ultimoAcesso) {
}
