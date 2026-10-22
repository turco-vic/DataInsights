"""Testa a conexão com o PostgreSQL do Azure: python testar_conexao.py"""
from sqlalchemy import text  # import: text escreve SQL

from banco import criar_engine  # import: abre a conexão (usa o .env)

try:  # tenta conectar
    with criar_engine().connect() as conexao:  # abre a conexão com o banco
        versao = conexao.execute(text("SELECT version()")).scalar()  # pergunta a versão do PostgreSQL
        print("Conectou!", versao)  # deu certo: mostra a versão
        try:  # tenta contar as planilhas
            lotes = conexao.execute(text("SELECT COUNT(*) FROM importacoes")).scalar()  # conta quantas planilhas existem
            print("Planilhas no banco:", lotes)  # mostra quantas planilhas já foram enviadas
            conexao.execute(text("SELECT COUNT(*) FROM estatistica_lote")).scalar()  # confere a tabela das análises
        except Exception:  # a tabela ainda não existe
            print("As tabelas ainda não foram criadas. Rode o backend uma vez (mvnw spring-boot:run) e teste de novo.")  # explica o que fazer
except Exception as erro:  # se não conectou:
    print("Não conectou. Confira o .env e o firewall do Azure (seu IP precisa estar liberado).")  # dica de onde olhar
    print("Detalhe:", str(erro).splitlines()[0])  # mostra só a primeira linha do erro
