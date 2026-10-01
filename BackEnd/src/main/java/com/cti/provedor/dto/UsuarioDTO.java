package com.cti.provedor.dto;

import com.cti.provedor.model.Papel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotBlank
    @Email
    private String email;

    /** Obrigatória no cadastro; na atualização, se vier vazia a senha atual é mantida. */
    @Size(min = 6)
    private String senha;

    /** Se não for informado: LEITOR no cadastro, e o papel atual é mantido na atualização. */
    private Papel papel;

    @Size(max = 150)
    private String empresa;

    @Size(max = 100)
    private String cargo;
}
