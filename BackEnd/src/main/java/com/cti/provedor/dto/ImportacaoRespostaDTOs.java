package com.cti.provedor.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Respostas do histórico de importações. */
public final class ImportacaoRespostaDTOs {

    private ImportacaoRespostaDTOs() {
    }

    public record Resumo(Long id, String nomeArquivo, String autorNome, LocalDateTime enviadaEm, int totalLinhas,
            int linhasComErro, int criados, int atualizados, int rejeitados, String status) {
    }

    /** erros, rejeitados e clientes são devolvidos como foram gravados (listas de objetos JSON). */
    public record Detalhe(Resumo resumo, List<Object> erros, List<Object> rejeitados, List<Object> clientes) {
    }
}
