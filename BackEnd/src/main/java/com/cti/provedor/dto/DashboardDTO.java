package com.cti.provedor.dto;

import java.math.BigDecimal;
import java.util.List;

/** Indicadores da carteira atual (todos os clientes do banco). */
public record DashboardDTO(
        long totalClientes,
        BigDecimal faturamentoTotal,
        BigDecimal faturamentoMedio,
        long contratosAtivos,
        long insights,
        List<Contagem> porNivel,
        List<Contagem> porSegmento,
        List<Contagem> servicos,
        List<Contagem> contratacoesPorMes,
        List<ClienteDestaque> maioresClientes,
        ImportacaoRespostaDTOs.Resumo ultimaImportacao) {

    public record Contagem(String nome, long quantidade) {
    }

    public record ClienteDestaque(Long id, String nome, String segmento, String consultor, String nivel,
            BigDecimal faturamentoAnual) {
    }
}
