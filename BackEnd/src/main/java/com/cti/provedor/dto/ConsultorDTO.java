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

    /** Opcional; obrigatório só para o consultor entrar na plataforma. */
    @Email
    private String email;

    /** Opcional. Na atualização, se vier vazia a senha atual é mantida (desde que haja e-mail). */
    @Size(min = 6)
    private String senha;
}
