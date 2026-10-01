package com.cti.provedor.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Envio de uma planilha já validada no front-end. */
@Getter
@Setter
public class ImportacaoDTO {

    @NotBlank
    @Size(max = 255)
    private String nomeArquivo;

    @PositiveOrZero
    private int totalLinhas;

    /** Erros de validação encontrados no front-end (as linhas com erro não vêm em clientes). */
    private List<ErroPlanilha> erros = new ArrayList<>();

    @NotNull
    private List<ClienteImportacaoDTO> clientes;

    public record ErroPlanilha(Integer linha, String campo, String tipo, String descricao) {
    }
}
