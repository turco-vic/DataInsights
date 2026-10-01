package com.cti.provedor;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTests {

    @Autowired
    private MockMvc mvc;

    private String token;

    @BeforeEach
    void autenticar() throws Exception {
        String email = unico() + "@teste.com";
        mvc.perform(json(post("/api/usuarios"), """
                {"nome": "Admin", "email": "%s", "senha": "segredo1"}""".formatted(email)))
                .andExpect(status().isCreated());
        token = JsonPath.read(mvc.perform(json(post("/api/usuarios/login"), """
                {"email": "%s", "senha": "segredo1"}""".formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.token");
    }

    @Test
    void rotasProtegidasExigemToken() throws Exception {
        mvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/clientes").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/clientes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void usuario() throws Exception {
        String email = unico() + "@teste.com";
        String corpo = """
                {"nome": "Ana", "email": "%s", "senha": "segredo1"}""".formatted(email.toUpperCase());

        Integer id = criar("/api/usuarios", corpo);
        mvc.perform(auth(get("/api/usuarios/" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        mvc.perform(json(post("/api/usuarios"), corpo)).andExpect(status().isConflict());
        mvc.perform(json(post("/api/usuarios"), """
                {"nome": "", "email": "invalido", "senha": "1"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.nome").exists());

        // Atualiza sem senha: a senha antiga continua valendo.
        mvc.perform(json(auth(put("/api/usuarios/" + id)), """
                {"nome": "Ana Maria", "email": "%s"}""".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Maria"));
        mvc.perform(json(post("/api/usuarios/login"), """
                {"email": "%s", "senha": "segredo1"}""".formatted(email)))
                .andExpect(status().isOk());
        mvc.perform(json(post("/api/usuarios/login"), """
                {"email": "%s", "senha": "errada"}""".formatted(email)))
                .andExpect(status().isUnauthorized());

        mvc.perform(auth(delete("/api/usuarios/" + id))).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/usuarios/" + id))).andExpect(status().isNotFound());
    }

    @Test
    void consultor() throws Exception {
        String email = unico() + "@cti.com";
        Integer id = criar("/api/consultores", """
                {"nome": "Bruno", "matricula": "M1", "email": "%s", "senha": "segredo1"}""".formatted(email));

        mvc.perform(auth(get("/api/consultores/" + id)))
                .andExpect(jsonPath("$.senha").doesNotExist());
        mvc.perform(json(post("/api/consultores/login"), """
                {"email": "%s", "senha": "segredo1"}""".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CONSULTOR"))
                .andExpect(jsonPath("$.token").exists());

        mvc.perform(json(auth(put("/api/consultores/" + id)), """
                {"nome": "Bruno Lima", "email": "%s"}""".formatted(email)))
                .andExpect(status().isOk());
        mvc.perform(json(auth(post("/api/consultores")), """
                {"nome": "Outro", "email": "%s"}""".formatted(unico() + "@cti.com")))
                .andExpect(status().isBadRequest());

        mvc.perform(auth(delete("/api/consultores/999999"))).andExpect(status().isNotFound());
        mvc.perform(auth(delete("/api/consultores/" + id))).andExpect(status().isNoContent());
    }

    @Test
    void clienteComContratosInsightsETelemetria() throws Exception {
        Integer consultorId = criar("/api/consultores", """
                {"nome": "Carla", "email": "%s", "senha": "segredo1"}""".formatted(unico() + "@cti.com"));
        Integer servicoId = criar("/api/servicos", """
                {"nome": "Servico %s", "categoria": "Cloud"}""".formatted(unico()));
        mvc.perform(json(auth(post("/api/servicos")), """
                {"nome": "SERVICO %s"}""".formatted(nomeServico(servicoId).substring(8))))
                .andExpect(status().isConflict());

        String codigo = "CTI" + (100000 + (int) (Math.random() * 800000));
        String cliente = """
                {"codigo": "%s", "nome": "Empresa X", "email": "%s", "segmento": "Tecnologia",
                 "nivel": "A", "faturamentoAnual": 1000, "uf": "SP", "consultorId": %d}"""
                .formatted(codigo, unico() + "@x.com", consultorId);
        Integer clienteId = criar("/api/clientes", cliente);
        mvc.perform(json(auth(post("/api/clientes")), cliente)).andExpect(status().isConflict());
        mvc.perform(auth(get("/api/clientes/" + clienteId)))
                .andExpect(jsonPath("$.consultor.id").value(consultorId))
                .andExpect(jsonPath("$.contratos").doesNotExist());

        Integer contratoId = criar("/api/contratos", """
                {"clienteId": %d, "servicoId": %d, "dataInicio": "2026-01-01", "valor": 500}"""
                .formatted(clienteId, servicoId));
        mvc.perform(auth(get("/api/contratos/" + contratoId)))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.servico.id").value(servicoId));
        mvc.perform(json(auth(put("/api/contratos/" + contratoId)), """
                {"clienteId": %d, "servicoId": %d, "dataInicio": "2026-01-01", "dataFim": "2025-01-01"}"""
                .formatted(clienteId, servicoId)))
                .andExpect(status().isBadRequest());

        Integer insightId = criar("/api/insights", """
                {"tipo": "Upsell", "descricao": "Cliente pronto para upgrade", "clienteId": %d, "contratoId": %d}"""
                .formatted(clienteId, contratoId));
        criar("/api/telemetrias", """
                {"evento": "login", "clienteId": %d}""".formatted(clienteId));

        mvc.perform(auth(get("/api/contratos?clienteId=" + clienteId))).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(auth(get("/api/insights?clienteId=" + clienteId))).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(auth(get("/api/telemetrias?clienteId=" + clienteId)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dataHora").exists());

        // Consultor e serviço em uso não podem ser removidos.
        mvc.perform(auth(delete("/api/consultores/" + consultorId))).andExpect(status().isConflict());
        mvc.perform(auth(delete("/api/servicos/" + servicoId))).andExpect(status().isConflict());

        // Remover o cliente leva junto contratos, insights e telemetrias.
        mvc.perform(auth(delete("/api/clientes/" + clienteId))).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/insights/" + insightId))).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/contratos/" + contratoId))).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/telemetrias?clienteId=" + clienteId))).andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(auth(delete("/api/servicos/" + servicoId))).andExpect(status().isNoContent());
        mvc.perform(auth(delete("/api/consultores/" + consultorId))).andExpect(status().isNoContent());
    }

    @Test
    void importacaoContinuaFuncionando() throws Exception {
        String codigo = "CTI" + (100000 + (int) (Math.random() * 800000));
        mvc.perform(json(auth(post("/api/clientes/importar")), """
                [{"codigoCliente": "%s", "nomeCliente": "Importada", "email": "%s", "consultor": "Diego",
                  "segmento": "Saúde", "nivelCliente": "B", "faturamentoAnual": 10,
                  "servicosContratados": ["Backup"], "dataContratacao": "2025-05-01", "uf": "RJ"}]"""
                .formatted(codigo, unico() + "@imp.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criados").value(1));
    }

    private Integer criar(String caminho, String corpo) throws Exception {
        return JsonPath.read(mvc.perform(json(auth(post(caminho)), corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    private String nomeServico(Integer id) throws Exception {
        return JsonPath.read(mvc.perform(auth(get("/api/servicos/" + id)))
                .andReturn().getResponse().getContentAsString(), "$.nome");
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req) {
        return req.header("Authorization", "Bearer " + token);
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String corpo) {
        return req.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private String unico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
