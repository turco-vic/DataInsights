package com.cti.provedor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteDTO {

    @NotBlank(message = "O código do cliente é obrigatório")
    @Size(max = 20, message = "O código do cliente deve ter no máximo 20 caracteres")
    @Pattern(regexp = "CTI\\d{3,}", message = "O código do cliente deve seguir o padrão CTI001")
    private String codigo;

    @NotBlank(message = "O nome do cliente é obrigatório")
    @Size(max = 150, message = "O nome do cliente deve ter no máximo 150 caracteres")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail não é válido")
    private String email;

    @Pattern(regexp = "Indústria|Comércio|Serviços|Saúde|Educação|Tecnologia",
            message = "O segmento deve ser Indústria, Comércio, Serviços, Saúde, Educação ou Tecnologia")
    private String segmento;

    @Pattern(regexp = "[ABC]", message = "O nível deve ser A, B ou C")
    private String nivel;

    @Positive(message = "O faturamento anual deve ser maior que zero")
    private BigDecimal faturamentoAnual;

    @PastOrPresent(message = "A data de contratação não pode estar no futuro")
    private LocalDate dataContratacao;

    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @Pattern(
            regexp = "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO",
            message = "A UF deve ser a sigla de um estado brasileiro (ex.: SP)")
    private String uf;

    private Long consultorId;
}
