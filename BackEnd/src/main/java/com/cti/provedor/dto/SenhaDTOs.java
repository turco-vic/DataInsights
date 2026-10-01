package com.cti.provedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpos das requisições de troca e recuperação de senha. */
public final class SenhaDTOs {

    private SenhaDTOs() {
    }

    public record AlterarSenha(@NotBlank String senhaAtual, @NotBlank @Size(min = 6) String novaSenha) {
    }

    public record EsqueciSenha(@NotBlank @Email String email) {
    }

    public record RedefinirSenha(@NotBlank String token, @NotBlank @Size(min = 6) String novaSenha) {
    }
}
