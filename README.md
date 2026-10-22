# DataInsights

> Transforme Dados em Decisões

Plataforma web moderna para análise de dados de clientes. O **DataInsights** automatiza o processamento de planilhas Excel e gera dashboards interativos com insights estratégicos em segundos.

## 📋 Sobre o Projeto

Projeto Integrador desenvolvido para a disciplina de Framework Front-End (SENAI). O sistema resolve a dificuldade de analisar planilhas complexas, oferecendo upload automático, padronização de dados e visualizações interativas para apoiar a tomada de decisão.

## ✨ Funcionalidades

- **Upload Simples** — envie planilhas `.xlsx`, `.xls` ou `.csv` (até 50 MB)
- **Tratamento de Dados** — padronização e limpeza automática no navegador (pode ser desligada em Configurações)
- **Validação** — campos obrigatórios vazios, códigos duplicados e níveis inválidos são apontados linha a linha, no front e de novo no back-end
- **Tudo ou nada** — só uma planilha 100% consistente entra no sistema; com qualquer inconsistência, nada é gravado
- **Dataset em CSV** — cada planilha importada pode ser baixada limpa e padronizada, pronta para a etapa de Ciência de Dados
- **Histórico de Planilhas** — cada envio fica guardado no servidor, com erros, recusas e relatório do dataset
- **Dashboard da Carteira** — KPIs e gráficos calculados sobre todos os clientes do banco
- **Cadastros** — clientes (com contratos, insights e atividade), consultores e serviços
- **Insights Automáticos** — oportunidades e alertas por cliente, gerados a cada importação
- **Usuários e Papéis** — administrador, analista e leitor; consultores com acesso de consulta
- **Conta** — perfil, preferências, troca de senha, recuperação de senha por e-mail e resumo semanal
- **Telemetria** — registro de logins, importações, exclusões e trocas de senha

## 🗺️ Fluxo de Telas

1. **Apresentação** — landing page, termos de uso e política de privacidade
2. **Acesso** — login (equipe ou consultor), cadastro, recuperação e redefinição de senha
3. **Análise** — dashboard, upload, relatório de validação, planilhas enviadas e relatório do dataset
4. **Cadastros** — clientes e detalhe do cliente, consultores, serviços e insights
5. **Administração** — usuários, telemetria e configurações

## 👥 Papéis

| Papel | Pode |
|-------|------|
| Administrador | tudo, inclusive gerenciar usuários e ver a telemetria |
| Analista | enviar planilhas e manter clientes, contratos, serviços, consultores e insights |
| Leitor | consultar dashboard, clientes e relatórios |
| Consultor | consultar (entra pela opção "Consultor" no login) |

A primeira conta cadastrada vira administradora. As seguintes começam como leitoras
(configurável) e são promovidas na tela de Usuários.

## 🛠️ Tecnologias

**Front-end**

- [Vue.js 3](https://vuejs.org/) — framework front-end
- [Vite](https://vitejs.dev/) — build tool e servidor de desenvolvimento (telas carregadas sob demanda)
- [Vue Router](https://router.vuejs.org/) — navegação e controle de acesso por papel
- [Pinia](https://pinia.vuejs.org/) — estado global: sessão (`authStore`), upload (`uploadStore`), histórico (`planilhasStore`) e avisos (`uiStore`)
- [Axios](https://axios-http.com/) — chamadas à API, com renovação automática da sessão
- [SheetJS / xlsx](https://sheetjs.com/) — leitura de Excel e CSV no navegador
- [Tailwind CSS](https://tailwindcss.com/) — estilização com classes utilitárias
- [ESLint](https://eslint.org/) e [Vitest](https://vitest.dev/) — qualidade e testes

**Back-end**

- [Spring Boot 4](https://spring.io/projects/spring-boot) — API REST em Java
- Spring Security + JWT — autenticação e permissões por papel
- Spring Data JPA + PostgreSQL (Azure) — persistência
- Spring Mail — recuperação de senha e resumo semanal

> **Nota sobre o `xlsx`:** instalado a partir do CDN oficial da SheetJS
> (`https://cdn.sheetjs.com/xlsx-0.20.2/xlsx-0.20.2.tgz`) e não do npm. A versão
> publicada no npm está parada na 0.18.5 e tem duas vulnerabilidades conhecidas
> (prototype pollution e ReDoS) sem correção disponível por lá.

## 📊 Tratamento dos Dados

A padronização segue a aba `dicionario_dados` da planilha modelo e acontece em
`src/stores/uploadStore.js`, na action `tratarLinha()`:

| Campo | Tratamento |
|-------|------------|
| `codigo_cliente` | trim; validação de duplicidade |
| `nome_cliente` | trim; capitalização |
| `consultor` | trim; capitalização (`ANA SOUZA` → `Ana Souza`) |
| `segmento` | unificação de variações (`IND.`, `Industria`, `INDUSTRIA` → `Indústria`) |
| `nivel_cliente` | uppercase; somente A, B ou C |
| `faturamento_anual` | conversão para número; precisa ser maior que zero |
| `servicos_contratados` | separação por `;` com remoção de espaços |
| `data_contratacao` | conversão para data válida |
| `cidade` | trim; capitalização |
| `uf` | uppercase; validação de 2 caracteres |

## 🛡️ Validação no Back-end

O front valida para ajudar quem envia a planilha; o back-end valida de novo para proteger a API
(alguém pode chamar a rota direto pelo Thunder Client ou pelo Postman). A regra combinada com a
professora é **tudo ou nada**: o sistema aponta todas as inconsistências, mas só importa uma planilha
100% consistente.

| Arquivo | Responsabilidade |
|---------|------------------|
| `dto/ClienteImportacaoDTO.java` | regras de uma linha com Jakarta Validation (`@NotBlank`, `@NotNull`, `@Positive`, `@Pattern`, `@Size`, `@Email`, `@PastOrPresent`), com mensagens em português |
| `service/ClienteValidacaoService.java` | padroniza (espaços, `IND.` → `Indústria`, `sp` → `SP`, `ANA SOUZA` → `Ana Souza`), valida cada linha, procura código e e-mail repetidos no lote e e-mail de outro cliente no banco; junta **todas** as inconsistências |
| `exception/GlobalExceptionHandler.java` | transforma os erros em JSON simples; erros inesperados viram uma mensagem curta e o detalhe fica só no log |

A padronização acontece **antes** das anotações do DTO, por isso as rotas de planilha não usam `@Valid`
na lista: o `ClienteValidacaoService` chama o `Validator` item por item depois de padronizar.

**Rotas**

| Rota | O que faz |
|------|-----------|
| `POST /api/clientes/validar` | valida e padroniza uma lista de clientes **sem gravar** e devolve a lista preparada |
| `POST /api/importacoes` | valida a planilha inteira; se estiver consistente, grava os clientes e o histórico |
| `GET /api/importacoes/{id}/csv` | baixa o dataset limpo de uma planilha importada (UTF-8, separado por vírgula) |

**Teste no Thunder Client** (com `Authorization: Bearer <token>` de um administrador ou analista):

```http
POST http://localhost:8080/api/clientes/validar
Content-Type: application/json

[{ "codigoCliente": "CTI001", "nomeCliente": "Metalúrgica Horizonte", "email": "contato@mh.com.br",
   "consultor": "ANA SOUZA", "segmento": "IND.", "nivelCliente": "a", "faturamentoAnual": 1850000,
   "servicosContratados": ["Internet Dedicada", "Firewall"], "dataContratacao": "2025-01-15",
   "cidade": "Campinas", "uf": "sp" }]
```

Resposta `200` com `"segmento": "Indústria"`, `"consultor": "Ana Souza"`, `"nivelCliente": "A"` e `"uf": "SP"`.
Com dados inválidos, a resposta é `400` e lista todas as inconsistências:

```json
{
  "status": 400,
  "mensagem": "A planilha tem 2 inconsistência(s). Nada foi importado: corrija todas e envie novamente.",
  "inconsistencias": [
    { "linha": 2, "codigoCliente": "CTI002", "campo": "nivel_cliente", "erro": "O nível deve ser A, B ou C" },
    { "linha": 3, "codigoCliente": "CTI002", "campo": "codigo_cliente", "erro": "O código CTI002 já aparece na linha 2" }
  ]
}
```

Sem o campo `linha` no JSON, o back-end conta como planilha: o primeiro cliente é a linha 2 (a linha 1 é o cabeçalho).

## 🐍 Ciência de Dados com Python (`analytics-python/`)

Depois que a planilha é importada, o backend envia as linhas válidas ao serviço Python, que limpa,
calcula e grava o resultado no mesmo PostgreSQL da Azure. A tela **Análise (Python)** mostra os gráficos.

```
Planilha → Pinia valida → Spring revalida e grava → PythonEtlService faz POST /etl
→ Python (pandas) limpa, calcula e gera as frases → grava estatistica_lote → /app/analise desenha os gráficos
```

| Arquivo | Responsabilidade |
|---------|------------------|
| `api.py` | API FastAPI: `GET /saude`, `POST /etl` (grava no banco) e `POST /testar` (calcula sem gravar) |
| `main.py` | `processar_lote`: reserva o lote, trata, calcula e grava; em caso de falha marca `ERRO` |
| `limpeza.py` | padroniza textos, segmentos e serviços (`"A;B"` vira lista) e converte números e datas |
| `analise.py` | média, mediana, desvio padrão, quartis, agrupamentos, correlação (scipy) |
| `insights.py` | transforma os números em frases |
| `banco.py` / `config.py` | conexão com o Azure (lê o `.env`) e gravação do resultado |
| `BackEnd/.../service/PythonEtlService.java` | depois do commit da importação, envia as linhas ao Python |

**Adaptação ao projeto:** os clientes e contratos já são gravados pelo backend na importação, então o
Python grava só o que é dele: o JSON das análises (`estatistica_lote`) e a etapa do ETL
(`importacoes.status_etl`: `AGUARDANDO_ETL` → `PROCESSANDO` → `PROCESSADO` ou `ERRO`).

**Rodar** (três terminais; ligue o Python antes de enviar a planilha):

```bash
# 1) Python
cd analytics-python
python -m venv .venv
.venv\Scripts\Activate.ps1          # Linux/Mac: source .venv/bin/activate
pip install -r requirements.txt
copy .env.example .env               # preencha DB_PASSWORD
python testar_conexao.py             # deve mostrar "Conectou!"
uvicorn api:app --port 8000          # http://localhost:8000/docs

# 2) Back-end:  DB_PASSWORD=... ./mvnw spring-boot:run
# 3) Front-end: npm run dev
```

**Testar:** em `http://localhost:8000/docs`, `POST /testar` com `amostras/clientes.json` deve devolver
2 clientes, média e mediana 1135000, desvio padrão 1011162.7. Pelo Spring: `GET /api/estatisticas?loteId=<id>`
devolve `{ lote, geradoEm, estatisticas }`; `POST /api/importacoes/<id>/etl` envia a planilha de novo ao Python.
No banco: `SELECT status_etl, mensagem_etl FROM importacoes ORDER BY id DESC;` e
`SELECT resultado::json -> 'resumo' ->> 'media' FROM estatistica_lote WHERE lote_id = <id>;`.
Com o Python desligado, a importação fica com `ERRO` "Não foi possível falar com o serviço Python".

## 🎨 Identidade Visual

Paleta baseada na marca CTI, seguindo a regra 60-30-10:

| Proporção | Uso | Cor |
|-----------|-----|-----|
| 60% | Fundo dominante | `#F8F9FA` (cinza claro) |
| 30% | Textos e menus | Azul-marinho |
| 10% | Destaque e botões | Azul (símbolo da marca) |

## 🚀 Como Executar

### Back-end (`BackEnd/`)

```bash
cd DataInsights/BackEnd

# Com o PostgreSQL da Azure (precisa da senha do banco)
DB_PASSWORD=... ./mvnw spring-boot:run

# Ou, para desenvolver sem tocar no banco real: H2 em memória (os dados somem ao parar)
./mvnw spring-boot:test-run

# Testes de integração (também usam H2)
./mvnw test
```

A API sobe em `http://localhost:8080`. Variáveis de ambiente:

| Variável | Para quê | Padrão |
|----------|----------|--------|
| `DB_PASSWORD` | senha do PostgreSQL | — |
| `JWT_SECRET` | chave dos tokens (**troque em produção**) | valor de exemplo |
| `ETL_URL` | endereço do serviço Python | `http://localhost:8000/etl` |
| `ETL_KEY` | chave enviada no cabeçalho `X-ETL-Key` (igual ao `ETL_API_KEY` do Python) | vazia |
| `FRONTEND_URL` | endereço usado nos links enviados por e-mail | `http://localhost:5173` |
| `CADASTRO_ABERTO` | permite o cadastro público na tela "Criar acesso" | `true` |
| `CADASTRO_PAPEL_PADRAO` | papel das contas criadas pelo cadastro público | `LEITOR` |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | servidor de e-mail; sem ele, os e-mails vão para o log | — |
| `MAIL_FROM` | remetente dos e-mails | `nao-responda@datainsights.local` |

### Front-end (`FrontEnd/`)

```bash
cd DataInsights/FrontEnd
npm install
npm run dev     # http://localhost:5173
npm run lint    # ESLint
npm test        # Vitest
npm run build   # versão de produção em dist/
```

Copie `.env.example` para `.env` para apontar outra API (`VITE_API_BASE_URL`) ou
mostrar o link de contato no rodapé (`VITE_CONTATO_EMAIL`).

> Todos os comandos `npm` deste projeto rodam dentro de `FrontEnd/`. Não existe
> `package.json` na raiz do repositório.

## 🧭 Arquitetura

O front-end **não acessa o banco diretamente**: valida a planilha no navegador e, se não houver
nenhum erro, envia todas as linhas para `POST /api/importacoes`. O Spring Boot padroniza e valida
o lote de novo; se estiver consistente, importa os clientes, guarda o histórico da planilha, gera
os insights automáticos e registra a telemetria. Se não estiver, devolve as inconsistências e não
grava nada (a recusa fica registrada na telemetria).
Dashboard, clientes e relatórios são lidos da API, com o token JWT de quem está logado.

## 👤 Autor

- Projeto acadêmico - SENAI
- Aluno: Enzo Turcovic
- Disciplina: Framework Front-End
- Professora: Ma. Tatiana Aparecida de Almeida
