package com.cti.provedor.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cti.provedor.model.Cliente;
import com.cti.provedor.model.Telemetria;
import com.cti.provedor.repository.TelemetriaRepository;

/** Registra na telemetria os eventos que acontecem no servidor (login, importação, exclusões...). */
@Service
public class EventoService {

    public static final String LOGIN = "login";
    public static final String LOGIN_FALHOU = "login_falhou";
    public static final String CADASTRO = "cadastro";
    public static final String IMPORTACAO = "importacao";
    public static final String IMPORTACAO_RECUSADA = "importacao_recusada";
    public static final String CLIENTE_EXCLUIDO = "cliente_excluido";
    public static final String USUARIO_CRIADO = "usuario_criado";
    public static final String USUARIO_EXCLUIDO = "usuario_excluido";
    public static final String SENHA_ALTERADA = "senha_alterada";
    public static final String SENHA_REDEFINIDA = "senha_redefinida";
    public static final String INSIGHTS_GERADOS = "insights_gerados";

    private static final int TAMANHO_MAXIMO = 255;

    private final TelemetriaRepository repository;

    public EventoService(TelemetriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String evento, String mensagem) {
        registrar(evento, mensagem, null);
    }

    /** Grava em transação própria, para o evento ficar registrado mesmo quando a operação falha. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String evento, String mensagem, Cliente cliente) {
        Telemetria telemetria = new Telemetria();
        telemetria.setEvento(evento);
        telemetria.setDataHora(LocalDateTime.now());
        telemetria.setMensagem(mensagem == null || mensagem.length() <= TAMANHO_MAXIMO
                ? mensagem
                : mensagem.substring(0, TAMANHO_MAXIMO - 1) + "…");
        telemetria.setCliente(cliente);
        repository.save(telemetria);
    }
}
