package com.cti.provedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String senha;

    /** true gera uma sessão longa ("Manter conectado"). */
    private boolean manterConectado;
}
