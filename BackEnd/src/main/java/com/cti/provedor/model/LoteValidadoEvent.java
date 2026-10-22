package com.cti.provedor.model;

/** Publicado quando uma planilha é importada: depois do commit, o PythonEtlService envia o lote ao Python. */
public record LoteValidadoEvent(Long loteId) {
}
