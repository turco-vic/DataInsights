package com.cti.provedor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContratoDTO {

    @NotNull
    private Long clienteId;

    @NotNull
    private Long servicoId;

    @NotNull
    private LocalDate dataInicio;

    private LocalDate dataFim;

    @PositiveOrZero
    private BigDecimal valor;

    /** Se não for informado, o contrato é criado como ativo. */
    private Boolean ativo;
}
