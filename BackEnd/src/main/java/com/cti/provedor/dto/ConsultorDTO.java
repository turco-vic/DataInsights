package com.cti.provedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsultorDTO {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @Size(max = 50)
    private String matricula;

    @NotBlank
    @Email
    private String email;

    /** Obrigatória no cadastro; na atualização, se vier vazia a senha atual é mantida. */
    @Size(min = 6)
    private String senha;
}
