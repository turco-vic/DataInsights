"""Banco: GRAVA no PostgreSQL (Azure) o resultado da análise e o status de cada planilha.

Adaptação ao projeto DataInsights: os clientes e contratos já são gravados pelo backend (Spring) na
importação, com e-mail, consultor e histórico. Gravar de novo aqui duplicaria esses dados, então o
Python grava só o que é dele: o JSON do dashboard (estatistica_lote) e a etapa do ETL (importacoes).
As tabelas são criadas pelo backend (rode o Spring uma vez antes)."""
from __future__ import annotations                                                  # recurso do Python para tipos mais flexíveis

import json                                                                         # json: transforma dicionário em texto JSON
import logging                                                                      # logging: escreve mensagens no terminal
from datetime import datetime, timezone                                             # datetime e timezone: data e hora em UTC

import pandas as pd                                                                 # pandas: tabelas de dados (DataFrame)
from sqlalchemy import create_engine, text                                          # SQLAlchemy: create_engine abre a conexão; text escreve SQL
from sqlalchemy.engine import Engine                                                # tipo Engine (só para indicar o tipo)

from config import database_url                                                     # config: pega o endereço do banco do .env

log = logging.getLogger("analytics")                                                # logger do programa


def criar_engine() -> Engine:                                                       # criar_engine: abre a "porta" de conexão com o Azure
    return create_engine(database_url(), pool_pre_ping=True)                        # pool_pre_ping testa a conexão antes de usar


def agora_utc() -> datetime:                                                        # agora_utc: hora atual em UTC
    """O Spring Boot grava TIMESTAMP sem fuso; fazemos o mesmo."""
    return datetime.now(timezone.utc).replace(tzinfo=None)                          # UTC sem fuso


# ------------------------------------------------------------------- status do lote
def reservar_lote(engine: Engine, lote_id: int, so_pendente: bool) -> bool:         # reservar_lote: marca PROCESSANDO para ninguém rodar duas vezes
    """Marca PROCESSANDO. Com so_pendente=True só pega lotes AGUARDANDO_ETL (evita dois ETLs no mesmo lote)."""
    filtro = "status_etl = 'AGUARDANDO_ETL'" if so_pendente else "(status_etl IS NULL OR status_etl <> 'PROCESSANDO')"  # escolhe a condição do UPDATE
    with engine.begin() as conn:                                                    # abre uma transação (confirma ao sair do bloco)
        r = conn.execute(                                                           # executa o UPDATE
            text(f"UPDATE importacoes SET status_etl = 'PROCESSANDO', mensagem_etl = 'ETL em andamento...' "  # troca o status para PROCESSANDO
                 f"WHERE id = :id AND {filtro}"), {"id": lote_id})                  # só se o lote estiver no status esperado
        return r.rowcount == 1                                                      # true se exatamente 1 lote foi alterado


def marcar_erro(engine: Engine, lote_id: int, mensagem: str) -> None:               # marcar_erro: grava ERRO no lote
    with engine.begin() as conn:                                                    # abre uma transação
        conn.execute(text("UPDATE importacoes SET status_etl = 'ERRO', mensagem_etl = :m WHERE id = :id"),  # SQL: status ERRO e a mensagem
                     {"id": lote_id, "m": mensagem[:480]})                          # mensagem limitada a 480 caracteres


# ------------------------------------------------------------------------ carga
def gravar_resultado(engine: Engine, lote_id: int, tratado: pd.DataFrame, payload: dict, rejeitadas_etl: int) -> None:  # gravar_resultado: grava o JSON e marca PROCESSADO
    """Grava tudo em uma única transação: ou o lote inteiro é processado, ou nada muda."""
    with engine.begin() as conn:                                                    # uma transação só: ou grava tudo ou nada
        conn.execute(text("DELETE FROM estatistica_lote WHERE lote_id = :l"), {"l": lote_id})  # reprocessar substitui o resultado anterior
        conn.execute(text("INSERT INTO estatistica_lote (lote_id, gerado_em, resultado) VALUES (:l, :g, :r)"),  # grava o JSON com média, mediana e gráficos
                     {"l": lote_id, "g": agora_utc(), "r": json.dumps(payload, ensure_ascii=False)})  # o JSON vai como texto

        mensagem = f"ETL concluído: {len(tratado)} clientes analisados"            # monta a mensagem final
        if rejeitadas_etl:                                                          # se o ETL recusou alguma linha
            mensagem += f", {rejeitadas_etl} linhas recusadas na limpeza"           # acrescenta quantas
        conn.execute(text("UPDATE importacoes SET status_etl = 'PROCESSADO', mensagem_etl = :m WHERE id = :id"),  # marca o lote como PROCESSADO
                     {"id": lote_id, "m": mensagem + "."})                          # com a mensagem de resumo
    log.info("Lote %s gravado: %s clientes analisados", lote_id, len(tratado))      # escreve no terminal o resumo
