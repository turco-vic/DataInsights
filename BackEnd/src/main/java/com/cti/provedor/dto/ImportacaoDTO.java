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

    /** Erros apontados pela validação do front-end. Se houver algum, a planilha é recusada inteira. */
    private List<ErroPlanilha> erros = new ArrayList<>();

    /** Todas as linhas da planilha. Sem @Valid de propósito: o ClienteValidacaoService padroniza antes de validar. */
    @NotNull
    private List<ClienteImportacaoDTO> clientes;

    public record ErroPlanilha(Integer linha, String campo, String tipo, String descricao) {
    }
}
