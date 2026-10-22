package com.cti.provedor.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

/**
 * Resultado da análise de uma planilha (aula de Ciência de Dados). Quem grava é o serviço Python
 * (analytics-python/banco.py); o backend só cria a tabela e devolve o JSON para o front.
 */
@Entity
@Table(name = "estatistica_lote")
public class EstatisticaLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id da importação (tabela importacoes) que gerou este resultado. */
    @Column(nullable = false, unique = true)
    private Long loteId;

    private LocalDateTime geradoEm;

    /** JSON do dashboard: resumo, agrupamentos, correlação e frases dos insights. */
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String resultado;

    public EstatisticaLote() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public void setGeradoEm(LocalDateTime geradoEm) {
        this.geradoEm = geradoEm;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}
