package com.cti.provedor.dto;

import java.time.LocalDateTime;
import java.util.Map;

/** Planilha + JSON calculado pelo Python (null enquanto o ETL não terminou). */
public record EstatisticaRespostaDTO(ImportacaoRespostaDTOs.Resumo lote, LocalDateTime geradoEm,
        Map<?, ?> estatisticas) {
}
