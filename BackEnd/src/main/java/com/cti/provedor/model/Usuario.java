package com.cti.provedor.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Papel papel;

    private String empresa;
    private String cargo;

    // Preferências (null = valor padrão; as colunas são opcionais para não quebrar linhas antigas).
    private Boolean prefTratarDados;
    private Boolean prefGerarDashboard;
    private Boolean prefResumoSemanal;

    private LocalDateTime senhaAlteradaEm;
    private LocalDateTime ultimoAcesso;
    private LocalDateTime criadoEm;

    public Usuario() {
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (senhaAlteradaEm == null) {
            senhaAlteradaEm = criadoEm;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Papel getPapel() {
        return papel;
    }

    public void setPapel(Papel papel) {
        this.papel = papel;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public boolean isPrefTratarDados() {
        return prefTratarDados == null || prefTratarDados;
    }

    public void setPrefTratarDados(Boolean prefTratarDados) {
        this.prefTratarDados = prefTratarDados;
    }

    public boolean isPrefGerarDashboard() {
        return prefGerarDashboard == null || prefGerarDashboard;
    }

    public void setPrefGerarDashboard(Boolean prefGerarDashboard) {
        this.prefGerarDashboard = prefGerarDashboard;
    }

    public boolean isPrefResumoSemanal() {
        return prefResumoSemanal != null && prefResumoSemanal;
    }

    public void setPrefResumoSemanal(Boolean prefResumoSemanal) {
        this.prefResumoSemanal = prefResumoSemanal;
    }

    public LocalDateTime getSenhaAlteradaEm() {
        return senhaAlteradaEm;
    }

    public void setSenhaAlteradaEm(LocalDateTime senhaAlteradaEm) {
        this.senhaAlteradaEm = senhaAlteradaEm;
    }

    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
