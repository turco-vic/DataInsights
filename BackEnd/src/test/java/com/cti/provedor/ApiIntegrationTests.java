package com.cti.provedor;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.cti.provedor.model.EstatisticaLote;
import com.cti.provedor.model.Papel;
import com.cti.provedor.model.RedefinicaoSenha;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.EstatisticaLoteRepository;
import com.cti.provedor.repository.RedefinicaoSenhaRepository;
import com.cti.provedor.repository.UsuarioRepository;
import com.cti.provedor.service.ResumoSemanalService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RedefinicaoSenhaRepository redefinicaoRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ResumoSemanalService resumoSemanalService;

    @Autowired
    private EstatisticaLoteRepository estatisticaRepository;

    /** Token de um administrador criado para cada teste. */
    private String token;

    @BeforeEach
    void autenticarComoAdmin() throws Exception {
        token = tokenDe(criarUsuario(Papel.ADMIN), "segredo1");
    }

    @Test
    void rotasProtegidasExigemToken() throws Exception {
        mvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").exists());
        mvc.perform(get("/api/clientes").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
        mvc.perform(auth(get("/api/clientes"))).andExpect(status().isOk());
        mvc.perform(get("/api/publico/estatisticas")).andExpect(status().isOk())
                .andExpect(jsonPath("$.planilhasProcessadas").exists());
    }

    @Test
    void papeisLimitamOQueCadaUmPodeFazer() throws Exception {
        String email = unico() + "@teste.com";
        mvc.perform(json(post("/api/auth/cadastro"), """
                {"nome": "Leo", "email": "%s", "senha": "segredo1", "empresa": "ACME"}""".formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.papel").value("LEITOR"))
                .andExpect(jsonPath("$.empresa").value("ACME"));
        String leitor = login(email, "segredo1", false);

        mvc.perform(get("/api/clientes").header("Authorization", "Bearer " + leitor)).andExpect(status().isOk());
        mvc.perform(json(post("/api/servicos"), "{\"nome\": \"X\"}").header("Authorization", "Bearer " + leitor))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").exists());
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + leitor))
                .andExpect(status().isForbidden());

        // Promovido a analista, passa a poder escrever (o papel é lido do banco a cada requisição).
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        mvc.perform(json(auth(put("/api/usuarios/" + usuario.getId())), """
                {"nome": "Leo", "email": "%s", "papel": "ANALISTA"}""".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("ANALISTA"));
        mvc.perform(json(post("/api/servicos"), "{\"nome\": \"Servico %s\"}".formatted(unico()))
                .header("Authorization", "Bearer " + leitor))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + leitor))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuario() throws Exception {
        String email = unico() + "@teste.com";
        String corpo = """
                {"nome": "Ana", "email": "%s", "senha": "segredo1", "papel": "ANALISTA"}"""
                .formatted(email.toUpperCase());

        Integer id = criar("/api/usuarios", corpo);
        mvc.perform(auth(get("/api/usuarios/" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.papel").value("ANALISTA"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        mvc.perform(json(auth(post("/api/usuarios")), corpo)).andExpect(status().isConflict());
        mvc.perform(json(auth(post("/api/usuarios")), """
                {"nome": "", "email": "invalido", "senha": "1"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.nome").exists());

        // Atualiza sem senha: a senha antiga continua valendo.
        mvc.perform(json(auth(put("/api/usuarios/" + id)), """
                {"nome": "Ana Maria", "email": "%s"}""".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Maria"))
                .andExpect(jsonPath("$.papel").value("ANALISTA"));
        login(email, "segredo1", false);
        mvc.perform(json(post("/api/usuarios/login"), """
                {"email": "%s", "senha": "errada"}""".formatted(email)))
                .andExpect(status().isUnauthorized());

        mvc.perform(auth(delete("/api/usuarios/" + id))).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/usuarios/" + id))).andExpect(status().isNotFound());
    }

    @Test
    void naoDaParaExcluirAPropriaContaNemFicarSemAdmin() throws Exception {
        Usuario admin = criarUsuario(Papel.ADMIN);
        String tokenAdmin = tokenDe(admin, "segredo1");
        mvc.perform(delete("/api/usuarios/" + admin.getId()).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isBadRequest());

        // Com ele como único administrador, rebaixá-lo é bloqueado.
        List<Usuario> outrosAdmins = usuarioRepository.findAll().stream()
                .filter(u -> u.getPapel() == Papel.ADMIN && !u.getId().equals(admin.getId()))
                .toList();
        outrosAdmins.forEach(u -> u.setPapel(Papel.LEITOR));
        usuarioRepository.saveAll(outrosAdmins);
        try {
            mvc.perform(json(put("/api/usuarios/" + admin.getId()), """
                    {"nome": "x", "email": "%s", "papel": "LEITOR"}""".formatted(admin.getEmail()))
                    .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isConflict());
        } finally {
            outrosAdmins.forEach(u -> u.setPapel(Papel.ADMIN));
            usuarioRepository.saveAll(outrosAdmins);
        }
    }

    @Test
    void perfilESenha() throws Exception {
        mvc.perform(auth(get("/api/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("ADMIN"))
                .andExpect(jsonPath("$.prefTratarDados").value(true))
                .andExpect(jsonPath("$.prefResumoSemanal").value(false));

        String email = JsonPath.read(mvc.perform(auth(get("/api/me"))).andReturn().getResponse().getContentAsString(),
                "$.email");
        mvc.perform(json(auth(put("/api/me")), """
                {"nome": "Novo Nome", "email": "%s", "cargo": "Controladoria",
                 "prefResumoSemanal": true, "prefTratarDados": false}""".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"))
                .andExpect(jsonPath("$.cargo").value("Controladoria"))
                .andExpect(jsonPath("$.prefResumoSemanal").value(true))
                .andExpect(jsonPath("$.prefTratarDados").value(false));

        mvc.perform(json(auth(put("/api/me/senha")), """
                {"senhaAtual": "errada", "novaSenha": "outra123"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(json(auth(put("/api/me/senha")), """
                {"senhaAtual": "segredo1", "novaSenha": "outra123"}"""))
                .andExpect(status().isNoContent());
        login(email, "outra123", false);
    }

    @Test
    void manterConectadoERenovacao() throws Exception {
        Usuario usuario = criarUsuario(Papel.LEITOR);
        String curto = login(usuario.getEmail(), "segredo1", false);
        String longo = login(usuario.getEmail(), "segredo1", true);
        long horasCurto = horasAteExpirar(curto);
        long horasLongo = horasAteExpirar(longo);
        assertTrue(horasCurto <= 1, "sessão normal dura 1 hora");
        assertTrue(horasLongo >= 24 * 6, "manter conectado dura 7 dias");

        mvc.perform(post("/api/auth/renovar").header("Authorization", "Bearer " + longo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.manterConectado").value(true))
                .andExpect(jsonPath("$.papel").value("LEITOR"));
    }

    @Test
    void recuperacaoDeSenha() throws Exception {
        Usuario usuario = criarUsuario(Papel.LEITOR);
        mvc.perform(json(post("/api/auth/esqueci-senha"), "{\"email\": \"%s\"}".formatted(usuario.getEmail())))
                .andExpect(status().isNoContent());
        mvc.perform(json(post("/api/auth/esqueci-senha"), "{\"email\": \"ninguem@nada.com\"}"))
                .andExpect(status().isNoContent());

        String tokenLink = Base64.getUrlEncoder().withoutPadding().encodeToString(unico().getBytes());
        RedefinicaoSenha pedido = new RedefinicaoSenha();
        pedido.setUsuario(usuario);
        pedido.setTokenHash(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(tokenLink.getBytes(StandardCharsets.UTF_8))));
        pedido.setExpiraEm(LocalDateTime.now().plusMinutes(30));
        redefinicaoRepository.save(pedido);

        String corpo = "{\"token\": \"%s\", \"novaSenha\": \"nova1234\"}".formatted(tokenLink);
        mvc.perform(json(post("/api/auth/redefinir-senha"), corpo)).andExpect(status().isNoContent());
        login(usuario.getEmail(), "nova1234", false);
        // O link é de uso único.
        mvc.perform(json(post("/api/auth/redefinir-senha"), corpo)).andExpect(status().isBadRequest());
    }

    @Test
    void consultor() throws Exception {
        String email = unico() + "@cti.com";
        Integer id = criar("/api/consultores", """
                {"nome": "Bruno", "matricula": "M1", "email": "%s", "senha": "segredo1"}""".formatted(email));

        mvc.perform(auth(get("/api/consultores/" + id)))
                .andExpect(jsonPath("$.senha").doesNotExist());
        String tokenConsultor = JsonPath.read(mvc.perform(json(post("/api/consultores/login"), """
                {"email": "%s", "senha": "segredo1"}""".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("CONSULTOR"))
                .andReturn().getResponse().getContentAsString(), "$.token");
        mvc.perform(get("/api/me").header("Authorization", "Bearer " + tokenConsultor))
                .andExpect(jsonPath("$.tipo").value("CONSULTOR"));
        mvc.perform(json(post("/api/servicos"), "{\"nome\": \"Y\"}").header("Authorization", "Bearer " + tokenConsultor))
                .andExpect(status().isForbidden());

        mvc.perform(json(auth(put("/api/consultores/" + id)), """
                {"nome": "Bruno Lima", "email": "%s"}""".formatted(email)))
                .andExpect(status().isOk());
        // Consultor sem e-mail é permitido (não entra na plataforma), mas senha sem e-mail não.
        criar("/api/consultores", "{\"nome\": \"Sem Login\"}");
        mvc.perform(json(auth(post("/api/consultores")), "{\"nome\": \"Outro\", \"senha\": \"segredo1\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(auth(get("/api/consultores")))
                .andExpect(jsonPath("$[0].totalClientes").exists());

        mvc.perform(auth(delete("/api/consultores/999999"))).andExpect(status().isNotFound());
        mvc.perform(auth(delete("/api/consultores/" + id))).andExpect(status().isNoContent());
    }

    @Test
    void clienteComContratosInsightsETelemetria() throws Exception {
        Integer consultorId = criar("/api/consultores", """
                {"nome": "Carla", "email": "%s", "senha": "segredo1"}""".formatted(unico() + "@cti.com"));
        String nomeServico = "Servico " + unico();
        Integer servicoId = criar("/api/servicos", """
                {"nome": "%s", "categoria": "Cloud"}""".formatted(nomeServico));
        mvc.perform(json(auth(post("/api/servicos")), "{\"nome\": \"%s\"}".formatted(nomeServico.toUpperCase())))
                .andExpect(status().isConflict());

        String codigo = novoCodigo();
        String cliente = """
                {"codigo": "%s", "nome": "Empresa X", "email": "%s", "segmento": "Tecnologia",
                 "nivel": "A", "faturamentoAnual": 1000, "uf": "SP", "consultorId": %d}"""
                .formatted(codigo, unico() + "@x.com", consultorId);
        Integer clienteId = criar("/api/clientes", cliente);
        mvc.perform(json(auth(post("/api/clientes")), cliente)).andExpect(status().isConflict());
        mvc.perform(auth(get("/api/clientes/" + clienteId)))
                .andExpect(jsonPath("$.consultor.id").value(consultorId))
                .andExpect(jsonPath("$.contratos").doesNotExist());
        mvc.perform(auth(get("/api/clientes?busca=" + codigo.toLowerCase())))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(auth(get("/api/clientes?busca=" + codigo + "&nivel=B")))
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(auth(get("/api/clientes?consultorId=" + consultorId)))
                .andExpect(jsonPath("$", hasSize(1)));

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

        // Nível A com 1 serviço ativo gera o insight automático de oportunidade.
        mvc.perform(auth(post("/api/insights/gerar"))).andExpect(status().isOk());
        mvc.perform(auth(get("/api/insights?clienteId=" + clienteId)))
                .andExpect(jsonPath("$[*].tipo", hasItem("Oportunidade")));
        mvc.perform(auth(get("/api/contratos?clienteId=" + clienteId))).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(auth(get("/api/telemetrias?clienteId=" + clienteId)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dataHora").exists());
        mvc.perform(auth(get("/api/servicos")))
                .andExpect(jsonPath("$[?(@.id == %d)].totalContratos".formatted(servicoId), hasItem(1)));

        // Consultor e serviço em uso não podem ser removidos.
        mvc.perform(auth(delete("/api/consultores/" + consultorId))).andExpect(status().isConflict());
        mvc.perform(auth(delete("/api/servicos/" + servicoId))).andExpect(status().isConflict());

        // Remover o cliente leva junto contratos, insights e telemetrias.
        mvc.perform(auth(delete("/api/clientes/" + clienteId))).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/insights/" + insightId))).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/contratos/" + contratoId))).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/telemetrias?clienteId=" + clienteId))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(auth(get("/api/telemetrias?evento=cliente_excluido")))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));

        mvc.perform(auth(delete("/api/servicos/" + servicoId))).andExpect(status().isNoContent());
        mvc.perform(auth(delete("/api/consultores/" + consultorId))).andExpect(status().isNoContent());
    }

    @Test
    void importacaoConsistenteGuardaHistoricoEAlimentaODashboard() throws Exception {
        String valido = novoCodigo();
        String outro = novoCodigo();
        // Textos fora do padrão: o back-end padroniza antes de validar (aula de validação no Service).
        String resposta = mvc.perform(json(auth(post("/api/importacoes")), """
                {"nomeArquivo": "carteira.xlsx", "totalLinhas": 2, "erros": [],
                 "clientes": [
                   {"linha": 2, "codigoCliente": " %s ", "nomeCliente": "IMPORTADA LTDA", "email": "%s",
                    "consultor": "  DIEGO   LIMA ", "segmento": "saude", "nivelCliente": "a", "faturamentoAnual": 10,
                    "servicosContratados": [" Backup "], "dataContratacao": "2025-05-01", "cidade": "rio de janeiro", "uf": "rj"},
                   {"linha": 3, "codigoCliente": "%s", "nomeCliente": "Outra, S.A.", "email": "%s", "consultor": "Diego Lima",
                    "segmento": "IND.", "nivelCliente": "B", "faturamentoAnual": 20,
                    "servicosContratados": ["Backup", "Cloud"], "dataContratacao": "2025-06-01", "uf": ""}
                 ]}"""
                .formatted(valido.toLowerCase(), unico() + "@IMP.com", outro, unico() + "@imp.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resumo.criados").value(2))
                .andExpect(jsonPath("$.resumo.rejeitados").value(0))
                .andExpect(jsonPath("$.resumo.linhasComErro").value(0))
                .andExpect(jsonPath("$.resumo.status").value("processado"))
                .andExpect(jsonPath("$.resumo.statusEtl").value("AGUARDANDO_ETL"))
                .andExpect(jsonPath("$.resumo.autorNome").exists())
                .andExpect(jsonPath("$.clientes", hasSize(2)))
                .andExpect(jsonPath("$.clientes[0].codigoCliente").value(valido))
                .andExpect(jsonPath("$.clientes[0].nomeCliente").value("Importada Ltda"))
                .andExpect(jsonPath("$.clientes[0].consultor").value("Diego Lima"))
                .andExpect(jsonPath("$.clientes[0].segmento").value("Saúde"))
                .andExpect(jsonPath("$.clientes[0].nivelCliente").value("A"))
                .andExpect(jsonPath("$.clientes[0].cidade").value("Rio de Janeiro"))
                .andExpect(jsonPath("$.clientes[0].uf").value("RJ"))
                .andExpect(jsonPath("$.clientes[1].segmento").value("Indústria"))
                .andExpect(jsonPath("$.erros", hasSize(0)))
                .andExpect(jsonPath("$.rejeitados", hasSize(0)))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(resposta, "$.resumo.id");

        mvc.perform(auth(get("/api/importacoes"))).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(auth(get("/api/importacoes/" + id)))
                .andExpect(jsonPath("$.clientes[0].codigoCliente").value(valido))
                .andExpect(jsonPath("$.clientes[0].dataContratacao").value("2025-05-01"));

        // Dataset limpo em CSV para a etapa de Ciência de Dados.
        mvc.perform(auth(get("/api/importacoes/" + id + "/csv")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string("Content-Disposition", containsString("carteira.csv")))
                .andExpect(content().string(startsWith("codigo_cliente,nome_cliente,email,consultor,segmento,")))
                .andExpect(content().string(containsString(valido + ",Importada Ltda,")))
                .andExpect(content().string(containsString(",Saúde,A,10,Backup,2025-05-01,Rio de Janeiro,RJ")))
                .andExpect(content().string(containsString(outro + ",\"Outra, S.A.\",")))
                .andExpect(content().string(containsString(",Indústria,B,20,Backup;Cloud,2025-06-01,,")));

        // Etapa do Python: sem resultado ainda, estatisticas vem null; depois do ETL, vem o JSON gravado.
        mvc.perform(auth(get("/api/estatisticas?loteId=" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lote.id").value(id))
                .andExpect(jsonPath("$.lote.statusEtl").value("AGUARDANDO_ETL"))
                .andExpect(jsonPath("$.estatisticas").doesNotExist());
        EstatisticaLote estatistica = new EstatisticaLote();
        estatistica.setLoteId(id.longValue());
        estatistica.setGeradoEm(LocalDateTime.now());
        estatistica.setResultado("{\"resumo\": {\"media\": 15.0}, \"insights\": [\"frase\"]}");
        estatisticaRepository.save(estatistica);
        mvc.perform(auth(get("/api/estatisticas")))
                .andExpect(jsonPath("$.lote.id").value(id))
                .andExpect(jsonPath("$.estatisticas.resumo.media").value(15.0))
                .andExpect(jsonPath("$.estatisticas.insights[0]").value("frase"));
        mvc.perform(auth(post("/api/importacoes/" + id + "/etl")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusEtl").value("AGUARDANDO_ETL"));

        mvc.perform(auth(get("/api/dashboard")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClientes", greaterThan(1)))
                .andExpect(jsonPath("$.contratosAtivos", greaterThan(1)))
                .andExpect(jsonPath("$.porNivel", hasSize(3)))
                .andExpect(jsonPath("$.servicos[*].nome", hasItem("Backup")))
                .andExpect(jsonPath("$.ultimaImportacao.id").value(id));

        // Nível A com um único serviço: a importação já gera o insight de oportunidade.
        mvc.perform(auth(get("/api/insights")))
                .andExpect(jsonPath("$[?(@.cliente.codigo == '%s')].tipo".formatted(valido), hasItem("Oportunidade")));
        mvc.perform(auth(get("/api/telemetrias?evento=importacao")))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));

        mvc.perform(auth(delete("/api/importacoes/" + id))).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/importacoes/" + id))).andExpect(status().isNotFound());
        assertTrue(estatisticaRepository.findByLoteId(id.longValue()).isEmpty());
        mvc.perform(auth(get("/api/clientes?busca=" + valido))).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void resumoSemanalMontaOTextoSemServidorDeEmail() {
        Usuario usuario = criarUsuario(Papel.LEITOR);
        usuario.setPrefResumoSemanal(true);
        usuarioRepository.save(usuario);
        // Sem SPRING_MAIL_HOST o e-mail vai para o log; o importante é não falhar.
        resumoSemanalService.enviarResumos();
    }

    @Test
    void importacaoAntigaContinuaFuncionando() throws Exception {
        mvc.perform(json(auth(post("/api/clientes/importar")), """
                [{"codigoCliente": "%s", "nomeCliente": "Importada", "email": "%s", "consultor": "Diego",
                  "segmento": "Saúde", "nivelCliente": "B", "faturamentoAnual": 10,
                  "servicosContratados": ["Backup"], "dataContratacao": "2025-05-01", "uf": "RJ"}]"""
                .formatted(novoCodigo(), unico() + "@imp.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criados").value(1));
    }

    @Test
    void planilhaComQualquerInconsistenciaNaoImportaNada() throws Exception {
        String valido = novoCodigo();
        String repetido = novoCodigo();
        String emailRepetido = unico() + "@imp.com";
        String lote = """
                {"nomeArquivo": "com-erros.xlsx", "totalLinhas": 3, "erros": [],
                 "clientes": [
                   {"linha": 2, "codigoCliente": "%s", "nomeCliente": "Válida", "email": "%s", "consultor": "Diego",
                    "segmento": "Saúde", "nivelCliente": "A", "faturamentoAnual": 10,
                    "servicosContratados": ["Backup"], "dataContratacao": "2025-05-01"},
                   {"linha": 3, "codigoCliente": "%s", "nomeCliente": "Outra", "email": "%s", "consultor": "Diego",
                    "segmento": "Saúde", "nivelCliente": "B", "faturamentoAnual": 20,
                    "servicosContratados": ["Backup"], "dataContratacao": "2025-06-01"},
                   {"linha": 4, "codigoCliente": "%s", "nomeCliente": "Repetida", "email": "%s", "consultor": "Diego",
                    "segmento": "Saúde", "nivelCliente": "B", "faturamentoAnual": 0,
                    "servicosContratados": ["Backup"], "dataContratacao": "2025-06-01"}
                 ]}"""
                .formatted(valido, unico() + "@imp.com", repetido, emailRepetido, repetido, emailRepetido);

        mvc.perform(json(auth(post("/api/importacoes")), lote))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("Nada foi importado")))
                .andExpect(jsonPath("$.inconsistencias", hasSize(3)))
                .andExpect(jsonPath("$.inconsistencias[*].linha", hasItems(4)))
                .andExpect(jsonPath("$.inconsistencias[*].campo",
                        hasItems("codigo_cliente", "email", "faturamento_anual")))
                .andExpect(jsonPath("$.inconsistencias[*].erro",
                        hasItem("O faturamento anual deve ser maior que zero")));

        // Nem a linha válida entrou, e a planilha não foi para o histórico.
        mvc.perform(auth(get("/api/clientes?busca=" + valido))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(auth(get("/api/importacoes")))
                .andExpect(jsonPath("$[*].nomeArquivo", not(hasItem("com-erros.xlsx"))));
        mvc.perform(auth(get("/api/telemetrias?evento=importacao_recusada")))
                .andExpect(jsonPath("$[*].mensagem", hasItem(containsString("com-erros.xlsx"))));

        // Erros apontados pelo front também bloqueiam, mesmo com os clientes enviados corretos.
        mvc.perform(json(auth(post("/api/importacoes")), """
                {"nomeArquivo": "front.xlsx", "totalLinhas": 1,
                 "erros": [{"linha": 2, "campo": "email", "tipo": "E-mail inválido", "descricao": "E-mail inválido"}],
                 "clientes": [{"codigoCliente": "%s", "nomeCliente": "A", "email": "%s", "consultor": "Diego",
                   "segmento": "Saúde", "nivelCliente": "A", "faturamentoAnual": 10,
                   "servicosContratados": ["Backup"], "dataContratacao": "2025-05-01"}]}"""
                .formatted(valido, unico() + "@imp.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.inconsistencias", hasSize(1)));

        // Linhas faltando (front mandou só parte da planilha) também bloqueiam.
        mvc.perform(json(auth(post("/api/importacoes")), """
                {"nomeArquivo": "parcial.xlsx", "totalLinhas": 5, "erros": [],
                 "clientes": [{"codigoCliente": "%s", "nomeCliente": "A", "email": "%s", "consultor": "Diego",
                   "segmento": "Saúde", "nivelCliente": "A", "faturamentoAnual": 10,
                   "servicosContratados": ["Backup"], "dataContratacao": "2025-05-01"}]}"""
                .formatted(valido, unico() + "@imp.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("Envie a planilha inteira")));
        mvc.perform(auth(get("/api/clientes?busca=" + valido))).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void validarPadronizaSemGravar() throws Exception {
        String codigo = novoCodigo();
        // Teste 1 da aula: JSON válido, com segmento "IND." para o Service padronizar.
        mvc.perform(json(auth(post("/api/clientes/validar")), """
                [{"codigoCliente": "%s", "nomeCliente": "Metalúrgica Horizonte", "email": "%s",
                  "consultor": "ANA SOUZA", "segmento": "IND.", "nivelCliente": "a", "faturamentoAnual": 1850000,
                  "servicosContratados": ["Internet Dedicada", "Firewall"], "dataContratacao": "2025-01-15",
                  "cidade": "Campinas", "uf": "sp"}]"""
                .formatted(codigo, unico() + "@mh.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].linha").value(2))
                .andExpect(jsonPath("$[0].segmento").value("Indústria"))
                .andExpect(jsonPath("$[0].consultor").value("Ana Souza"))
                .andExpect(jsonPath("$[0].nivelCliente").value("A"))
                .andExpect(jsonPath("$[0].uf").value("SP"));
        mvc.perform(auth(get("/api/clientes?busca=" + codigo))).andExpect(jsonPath("$", hasSize(0)));

        // Serviços também podem vir como texto separado por ";", como na planilha.
        mvc.perform(json(auth(post("/api/clientes/validar")), """
                [{"codigoCliente": "%s", "nomeCliente": "Metalúrgica Horizonte", "email": "%s",
                  "consultor": "Ana Souza", "segmento": "IND.", "nivelCliente": "A", "faturamentoAnual": 1850000,
                  "servicosContratados": "Internet Dedicada; Firewall;", "dataContratacao": "2025-01-15",
                  "cidade": "Campinas", "uf": "SP"}]"""
                .formatted(novoCodigo(), unico() + "@mh.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].servicosContratados", hasSize(2)))
                .andExpect(jsonPath("$[0].servicosContratados[1]").value("Firewall"));

        // Teste 2 da aula: vários erros na mesma linha voltam todos juntos.
        mvc.perform(json(auth(post("/api/clientes/validar")), """
                [{"codigoCliente": "CTI002", "nomeCliente": "", "consultor": "Carlos Lima", "segmento": "Comercio",
                  "nivelCliente": "D", "faturamentoAnual": -10, "servicosContratados": [],
                  "dataContratacao": "2025-01-30", "cidade": "Hortolândia", "uf": "SPP"}]"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.inconsistencias[*].campo", hasItems("nome_cliente", "email", "nivel_cliente",
                        "faturamento_anual", "servicos_contratados", "uf")))
                .andExpect(jsonPath("$.inconsistencias[*].erro", hasItem("O nome do cliente é obrigatório")));

        mvc.perform(json(auth(post("/api/clientes/validar")), "[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A planilha não tem nenhum cliente para validar."));
    }

    @Test
    void errosInesperadosViramJsonSimples() throws Exception {
        mvc.perform(auth(get("/api/rota-que-nao-existe")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Rota não encontrada"));
        mvc.perform(auth(put("/api/importacoes")))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.mensagem").value("Método HTTP não permitido nesta rota"));
    }

    private Usuario criarUsuario(Papel papel) {
        Usuario usuario = new Usuario();
        usuario.setNome(papel.name().toLowerCase() + " de teste");
        usuario.setEmail(papel.name().toLowerCase() + "-" + unico() + "@teste.com");
        usuario.setSenhaHash(encoder.encode("segredo1"));
        usuario.setPapel(papel);
        return usuarioRepository.save(usuario);
    }

    private String tokenDe(Usuario usuario, String senha) throws Exception {
        return login(usuario.getEmail(), senha, false);
    }

    private String login(String email, String senha, boolean manter) throws Exception {
        return JsonPath.read(mvc.perform(json(post("/api/usuarios/login"), """
                {"email": "%s", "senha": "%s", "manterConectado": %s}""".formatted(email, senha, manter)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.token");
    }

    private long horasAteExpirar(String jwt) {
        String payload = new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]), StandardCharsets.UTF_8);
        long exp = Long.parseLong(payload.replaceAll(".*\"exp\":(\\d+).*", "$1"));
        return (exp - System.currentTimeMillis() / 1000) / 3600;
    }

    private Integer criar(String caminho, String corpo) throws Exception {
        return JsonPath.read(mvc.perform(json(auth(post(caminho)), corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req) {
        return req.header("Authorization", "Bearer " + token);
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String corpo) {
        return req.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private String novoCodigo() {
        return "CTI" + (100000 + (int) (Math.random() * 800000));
    }

    private String unico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
