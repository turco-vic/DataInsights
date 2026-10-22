"""Configuração de conexão com o banco. Lê as variáveis do arquivo .env (pasta analytics-python)."""
import os                                                      # import: os = lê variáveis de ambiente
from pathlib import Path                                       # import: Path = trabalha com caminhos de arquivos

from dotenv import load_dotenv                                 # import: load_dotenv lê o arquivo .env
from sqlalchemy.engine import URL                              # import: URL monta o endereço do banco

load_dotenv(Path(__file__).resolve().parent / ".env")          # lê o arquivo .env que está na pasta analytics-python


def database_url() -> URL:                                     # função: devolve o endereço de conexão com o banco
    """Monta o endereço do banco com as variáveis DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD e DB_SSLMODE.

    É o mesmo banco do backend (Azure Database for PostgreSQL): use DB_SSLMODE=require.
    """
    return URL.create(                                         # monta o endereço do PostgreSQL
        "postgresql+psycopg",                                  # driver: psycopg (conversa com o PostgreSQL)
        username=os.getenv("DB_USER", "eturcodevadmin"),       # usuário (do .env)
        password=os.getenv("DB_PASSWORD", ""),                 # senha (do .env)
        host=os.getenv("DB_HOST", "eturcodev.postgres.database.azure.com"),  # servidor do Azure
        port=int(os.getenv("DB_PORT", "5432")),                # porta padrão do PostgreSQL
        database=os.getenv("DB_NAME", "postgres"),             # nome do banco
        query={"sslmode": os.getenv("DB_SSLMODE", "require")},  # sslmode=require: conexão criptografada (o Azure exige)
    )                                                          # fecha o bloco
