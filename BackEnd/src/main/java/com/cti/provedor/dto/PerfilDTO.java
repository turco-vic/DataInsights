package com.cti.provedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Dados que a própria pessoa edita em Configurações. Consultores só usam nome e e-mail. */
@Getter
@Setter
public class PerfilDTO {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotBlank
    @Email
    private String email;

    @Size(max = 150)
    private String empresa;

    @Size(max = 100)
    private String cargo;

    private Boolean prefTratarDados;
    private Boolean prefGerarDashboard;
    private Boolean prefResumoSemanal;
}
