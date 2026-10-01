package com.cti.provedor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicoDTO {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @Size(max = 100)
    private String categoria;
}
