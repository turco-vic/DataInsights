package com.cti.provedor.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

/** Histórico de uma planilha enviada: resumo, erros de validação e cópia das linhas importadas. */
@Entity
@Table(name = "importacoes")
public class Importacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeArquivo;

    private Long autorId;
    private String autorNome;

    @Column(nullable = false)
    private LocalDateTime enviadaEm;

    private int totalLinhas;
    private int linhasComErro;
    private int criados;
    private int atualizados;
    private int rejeitados;

    @Column(length = 20)
    private String status;

    /** Erros encontrados na validação da planilha (JSON). */
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String errosJson;

    /** Registros recusados pelo back-end na importação (JSON). */
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String rejeitadosJson;

    /** Linhas válidas enviadas, usadas no relatório do dataset (JSON). */
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    private String clientesJson;

    public Importacao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public Long getAutorId() {
        return autorId;
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }

    public String getAutorNome() {
        return autorNome;
    }

    public void setAutorNome(String autorNome) {
        this.autorNome = autorNome;
    }

    public LocalDateTime getEnviadaEm() {
        return enviadaEm;
    }

    public void setEnviadaEm(LocalDateTime enviadaEm) {
        this.enviadaEm = enviadaEm;
    }

    public int getTotalLinhas() {
        return totalLinhas;
    }

    public void setTotalLinhas(int totalLinhas) {
        this.totalLinhas = totalLinhas;
    }

    public int getLinhasComErro() {
        return linhasComErro;
    }

    public void setLinhasComErro(int linhasComErro) {
        this.linhasComErro = linhasComErro;
    }

    public int getCriados() {
        return criados;
    }

    public void setCriados(int criados) {
        this.criados = criados;
    }

    public int getAtualizados() {
        return atualizados;
    }

    public void setAtualizados(int atualizados) {
        this.atualizados = atualizados;
    }

    public int getRejeitados() {
        return rejeitados;
    }

    public void setRejeitados(int rejeitados) {
        this.rejeitados = rejeitados;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrosJson() {
        return errosJson;
    }

    public void setErrosJson(String errosJson) {
        this.errosJson = errosJson;
    }

    public String getRejeitadosJson() {
        return rejeitadosJson;
    }

    public void setRejeitadosJson(String rejeitadosJson) {
        this.rejeitadosJson = rejeitadosJson;
    }

    public String getClientesJson() {
        return clientesJson;
    }

    public void setClientesJson(String clientesJson) {
        this.clientesJson = clientesJson;
    }
}
