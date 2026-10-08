package com.cti.provedor.dto;

import lombok.Getter;

/** Resultado de um lote gravado. Não existe recusa parcial: ou o lote inteiro entra, ou nada entra. */
@Getter
public class ImportacaoResultadoDTO {

    private final int recebidos;
    private int criados;
    private int atualizados;

    public ImportacaoResultadoDTO(int recebidos) {
        this.recebidos = recebidos;
    }

    public void somarCriado() {
        criados++;
    }

    public void somarAtualizado() {
        atualizados++;
    }
}
