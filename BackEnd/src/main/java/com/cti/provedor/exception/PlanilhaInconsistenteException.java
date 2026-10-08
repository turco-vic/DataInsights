package com.cti.provedor.exception;

import java.util.List;

import com.cti.provedor.dto.InconsistenciaDTO;

import lombok.Getter;

/** A planilha tem pelo menos uma inconsistência: nada é importado e todas são devolvidas ao front. */
@Getter
public class PlanilhaInconsistenteException extends RuntimeException {

    private final List<InconsistenciaDTO> inconsistencias;

    public PlanilhaInconsistenteException(List<InconsistenciaDTO> inconsistencias) {
        super("A planilha tem %d inconsistência(s). Nada foi importado: corrija todas e envie novamente."
                .formatted(inconsistencias.size()));
        this.inconsistencias = List.copyOf(inconsistencias);
    }
}
