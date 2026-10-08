package com.cti.provedor.dto;

/** Um problema encontrado na planilha: linha, cliente, coluna e mensagem. */
public record InconsistenciaDTO(Integer linha, String codigoCliente, String campo, String erro) {
}
