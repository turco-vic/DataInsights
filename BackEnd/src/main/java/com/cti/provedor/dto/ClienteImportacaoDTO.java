package com.cti.provedor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteImportacaoDTO {

    @NotBlank
    @Pattern(regexp = "CTI\\d{3,}", message = "deve seguir o padrão CTI001")
    private String codigoCliente;

    @NotBlank
    @Size(max = 150)
    private String nomeCliente;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String consultor;

    @NotBlank
    @Pattern(regexp = "Indústria|Comércio|Serviços|Saúde|Educação|Tecnologia", message = "não é um segmento reconhecido")
    private String segmento;

    @NotBlank
    @Pattern(regexp = "[ABC]", message = "deve ser A, B ou C")
    private String nivelCliente;

    @NotNull
    @PositiveOrZero
    private BigDecimal faturamentoAnual;

    @NotEmpty
    private List<@NotBlank String> servicosContratados;

    @NotNull
    @PastOrPresent
    private LocalDate dataContratacao;

    private String cidade;

    @Pattern(
            regexp = "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO",
            message = "não é uma UF válida")
    private String uf;
}
