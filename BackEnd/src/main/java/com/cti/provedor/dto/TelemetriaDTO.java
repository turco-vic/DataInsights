package com.cti.provedor.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelemetriaDTO {

    @NotBlank
    @Size(max = 255)
    private String evento;

    /** Se não for informada, usa o momento do registro. */
    private LocalDateTime dataHora;

    @Size(max = 255)
    private String mensagem;

    private Long clienteId;
}
