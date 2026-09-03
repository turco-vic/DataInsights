package com.cti.provedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO {

    @NotBlank
    private String nome;

    @NotBlank
    @Email
    private String email;

    @Size(min = 6)
    private String senha;
}
