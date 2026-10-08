package com.cti.provedor.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroDTO(int status, String mensagem, Map<String, String> campos,
        List<InconsistenciaDTO> inconsistencias, LocalDateTime timestamp) {

    public ErroDTO(int status, String mensagem, Map<String, String> campos) {
        this(status, mensagem, campos, null, LocalDateTime.now());
    }

    public ErroDTO(int status, String mensagem, List<InconsistenciaDTO> inconsistencias) {
        this(status, mensagem, null, inconsistencias, LocalDateTime.now());
    }
}
