# DataInsights

> Transforme Dados em Decisões

Plataforma web moderna para análise de dados de clientes. O **DataInsights** automatiza o processamento de planilhas Excel e gera dashboards interativos com insights estratégicos em segundos.

## 📋 Sobre o Projeto

Projeto Integrador desenvolvido para a disciplina de Framework Front-End (SENAI). O sistema resolve a dificuldade de analisar planilhas complexas, oferecendo upload automático, padronização de dados e visualizações interativas para apoiar a tomada de decisão.

## ✨ Funcionalidades

- **Upload Simples** — envie planilhas `.xlsx`, `.xls` ou `.csv` (até 50 MB)
- **Tratamento de Dados** — padronização e limpeza automática no navegador (pode ser desligada em Configurações)
- **Validação** — campos obrigatórios vazios, códigos duplicados e níveis inválidos são apontados linha a linha
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
| `faturamento_anual` | conversão para número; validação de vazio e negativo |
| `servicos_contratados` | separação por `;` com remoção de espaços |
| `data_contratacao` | conversão para data válida |
| `cidade` | trim; capitalização |
| `uf` | uppercase; validação de 2 caracteres |

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

O front-end **não acessa o banco diretamente**: valida a planilha no navegador e envia
as linhas válidas para `POST /api/importacoes`. O Spring Boot importa os clientes,
guarda o histórico da planilha, gera os insights automáticos e registra a telemetria.
Dashboard, clientes e relatórios são lidos da API, com o token JWT de quem está logado.

## 👤 Autor

- Projeto acadêmico - SENAI
- Aluno: Enzo Turcovic
- Disciplina: Framework Front-End
- Professora: Ma. Tatiana Aparecida de Almeida
