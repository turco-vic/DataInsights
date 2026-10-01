package com.cti.provedor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsightDTO {

    @NotBlank
    @Size(max = 255)
    private String tipo;

    @NotBlank
    @Size(max = 500)
    private String descricao;

    @NotNull
    private Long clienteId;

    /** Opcional; quando informado, o contrato precisa ser do mesmo cliente. */
    private Long contratoId;
}
