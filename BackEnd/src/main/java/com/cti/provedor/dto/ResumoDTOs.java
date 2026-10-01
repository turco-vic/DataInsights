package com.cti.provedor.dto;

/** Itens das listagens de consultores e serviços, com totais para as telas. */
public final class ResumoDTOs {

    private ResumoDTOs() {
    }

    public record Consultor(Long id, String nome, String matricula, String email, long totalClientes) {
    }

    public record Servico(Long id, String nome, String categoria, long totalContratos) {
    }

    /** Números exibidos na tela de login. */
    public record EstatisticasPublicas(long planilhasProcessadas, long clientesNaBase) {
    }
}
