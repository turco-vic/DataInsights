package com.cti.provedor.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.cti.provedor.model.Importacao;
import com.cti.provedor.model.LoteValidadoEvent;
import com.cti.provedor.repository.ImportacaoRepository;

import tools.jackson.databind.json.JsonMapper;

/**
 * Ponte entre o backend e o serviço Python (aula de Ciência de Dados). Depois que a planilha é gravada,
 * envia as linhas válidas em JSON para POST /etl; o Python limpa, calcula, gera as frases e grava o
 * resultado na tabela estatistica_lote. Se o Python não responder 200, a importação fica com ERRO.
 */
@Service
public class PythonEtlService {

    public static final String AGUARDANDO = "AGUARDANDO_ETL";
    public static final String ERRO = "ERRO";

    private static final Logger log = LoggerFactory.getLogger(PythonEtlService.class);
    private static final int TAMANHO_MENSAGEM = 480;

    private final String url;
    private final String chave;
    private final boolean ativo;
    private final ImportacaoRepository importacoes;
    private final JsonMapper json;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    /** Uma thread só: os lotes vão ao Python um de cada vez, sem segurar a resposta do upload. */
    private final ExecutorService fila = Executors.newSingleThreadExecutor(tarefa -> {
        Thread thread = new Thread(tarefa, "etl-python");
        thread.setDaemon(true);
        return thread;
    });

    public PythonEtlService(@Value("${app.etl.url:http://localhost:8000/etl}") String url,
            @Value("${app.etl.key:}") String chave, @Value("${app.etl.ativo:true}") boolean ativo,
            ImportacaoRepository importacoes, JsonMapper json) {
        this.url = url.trim();
        this.chave = chave.trim();
        this.ativo = ativo;
        this.importacoes = importacoes;
        this.json = json;
    }

    /** Roda só depois do commit: o Python precisa encontrar a importação já gravada no banco. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoValidar(LoteValidadoEvent evento) {
        if (!ativo) {
            log.info("ETL desativado (app.etl.ativo=false): lote {} não foi enviado ao Python", evento.loteId());
            return;
        }
        fila.submit(() -> executar(evento.loteId()));
    }

    private void executar(Long loteId) {
        try {
            Importacao importacao = importacoes.findById(loteId).orElse(null);
            if (importacao == null) {
                return;
            }
            List<Map<String, Object>> linhas = linhas(importacao);
            Map<String, Object> pedido = new LinkedHashMap<>();
            pedido.put("loteId", loteId);
            pedido.put("recebidas", importacao.getTotalLinhas());
            pedido.put("linhas", linhas);

            log.info("Enviando {} linhas do lote {} ao Python: POST {}", linhas.size(), loteId, url);
            HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMinutes(10))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(json.writeValueAsBytes(pedido)));
            if (!chave.isEmpty()) {
                requisicao.header("X-ETL-Key", chave);
            }
            HttpResponse<String> resposta = http.send(requisicao.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resposta.statusCode() != 200) {
                marcarErro(loteId, "O serviço Python respondeu " + resposta.statusCode() + ": " + resposta.body());
            }
        } catch (IOException e) {
            marcarErro(loteId, "Não foi possível falar com o serviço Python em " + url
                    + " (ele está ligado? uvicorn api:app --port 8000).");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            marcarErro(loteId, "Erro ao enviar o lote ao Python: " + e.getMessage());
        }
    }

    /** As linhas gravadas na importação, com os nomes das colunas da planilha (snake_case). */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> linhas(Importacao importacao) {
        List<Map<String, Object>> clientes = importacao.getClientesJson() == null ? List.of()
                : json.readValue(importacao.getClientesJson(), List.class);
        List<Map<String, Object>> linhas = new ArrayList<>();
        for (Map<String, Object> c : clientes) {
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("linha", c.get("linha"));
            linha.put("codigo_cliente", c.get("codigoCliente"));
            linha.put("nome_cliente", c.get("nomeCliente"));
            linha.put("consultor", c.get("consultor"));
            linha.put("segmento", c.get("segmento"));
            linha.put("nivel_cliente", c.get("nivelCliente"));
            linha.put("faturamento_anual", c.get("faturamentoAnual"));
            linha.put("servicos_contratados", c.get("servicosContratados") instanceof List<?> servicos
                    ? servicos.stream().map(String::valueOf).collect(Collectors.joining(";"))
                    : c.get("servicosContratados"));
            linha.put("data_contratacao", c.get("dataContratacao"));
            linha.put("cidade", c.get("cidade"));
            linha.put("uf", c.get("uf"));
            linhas.add(linha);
        }
        return linhas;
    }

    private void marcarErro(Long loteId, String mensagem) {
        log.error("ETL do lote {}: {}", loteId, mensagem);
        importacoes.atualizarEtl(loteId, ERRO,
                mensagem.length() > TAMANHO_MENSAGEM ? mensagem.substring(0, TAMANHO_MENSAGEM) : mensagem);
    }
}
