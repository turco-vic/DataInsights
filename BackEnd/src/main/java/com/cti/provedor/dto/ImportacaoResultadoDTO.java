package com.cti.provedor.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class ImportacaoResultadoDTO {

    private final int recebidos;
    private int criados;
    private int atualizados;
    private final List<Rejeitado> rejeitados = new ArrayList<>();

    public ImportacaoResultadoDTO(int recebidos) {
        this.recebidos = recebidos;
    }

    public void somarCriado() {
        criados++;
    }

    public void somarAtualizado() {
        atualizados++;
    }

    public void rejeitar(String codigoCliente, String motivo) {
        rejeitados.add(new Rejeitado(codigoCliente, motivo));
    }

    public record Rejeitado(String codigoCliente, String motivo) {
    }
}
