# DataInsights

> Transforme Dados em Decisões

Plataforma web moderna para análise de dados de clientes. O **DataInsights** automatiza o processamento de planilhas Excel e gera dashboards interativos com insights estratégicos em segundos.

## 📋 Sobre o Projeto

Projeto Integrador desenvolvido para a disciplina de Framework Front-End (SENAI). O sistema resolve a dificuldade de analisar planilhas complexas, oferecendo upload automático, padronização de dados e visualizações interativas para apoiar a tomada de decisão.

## ✨ Funcionalidades

- **Upload Simples** — envie planilhas `.xlsx`, `.xls` ou `.csv` com apenas um clique
- **Tratamento de Dados** — padronização e limpeza automática no próprio navegador
- **Validação** — campos obrigatórios vazios, códigos duplicados e níveis inválidos são apontados linha a linha
- **Gráficos Automáticos** — visualize os dados em tempo real
- **Insights Estratégicos** — KPIs e relatórios detalhados para a empresa

## 🗺️ Fluxo de Telas

1. **Apresentação** — landing page institucional do sistema
2. **Login** — autenticação segura para funcionários autorizados
3. **Upload** — envio e processamento da planilha Excel
4. **Dashboards** — análise visual, KPIs e relatórios detalhados

## 🛠️ Tecnologias

- [Vue.js 3](https://vuejs.org/) — framework front-end
- [Vite](https://vitejs.dev/) — build tool e servidor de desenvolvimento
- [Vue Router](https://router.vuejs.org/) — navegação entre telas
- [Pinia](https://pinia.vuejs.org/) — estado global: login (`authStore`), upload (`uploadStore`) e planilhas enviadas (`planilhasStore`)
- [SheetJS / xlsx](https://sheetjs.com/) — leitura de Excel e CSV no navegador
- [Tailwind CSS](https://tailwindcss.com/) — estilização com classes utilitárias
- HTML Semântico — foco em SEO e acessibilidade

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

```bash
# Clonar o repositório
git clone https://github.com/seu-usuario/DataInsights.git

# Entrar na pasta do front-end (o back-end Java fica em ../BackEnd)
cd DataInsights/FrontEnd

# Instalar as dependências
npm install

# Rodar em ambiente de desenvolvimento
npm run dev
```

Acesse `http://localhost:5173` no navegador.

> Todos os comandos `npm` deste projeto rodam dentro de `FrontEnd/`. Não existe
> `package.json` na raiz do repositório.

## 🧭 Etapa Atual e Próximos Passos

**Nesta etapa (somente front-end):** a planilha é lida, tratada e validada
inteiramente no navegador. Não há dados fictícios: Dashboard, Planilhas e Dataset
são montados a partir das planilhas enviadas, que o Pinia guarda no `localStorage`
(sobrevivem ao F5, mas ficam só neste navegador). O login também persiste no
`localStorage`; qualquer e-mail e senha preenchidos são aceitos até o backend existir.

**Próxima etapa — backend e nuvem:**

| Camada | Tecnologia | Situação |
|--------|------------|----------|
| Front-end | Vue 3 + Pinia | ✅ nesta etapa |
| HTTP | Axios / `fetch` | ⏳ próxima etapa |
| API | Java Spring Boot | ⏳ próxima etapa (scaffold em `BackEnd/`) |
| Persistência | PostgreSQL | ⏳ próxima etapa |
| Nuvem | Azure Database for PostgreSQL | ⏳ próxima etapa |

O front-end **não acessará o banco diretamente**: quem grava no PostgreSQL é o
Spring Boot. A troca fica nos stores: `authService.login` passa a chamar
`POST /api/usuarios/login` e `planilhasStore.salvar` passa a enviar os dados para a API.

## 👤 Autor

- Projeto acadêmico - SENAI
- Aluno: Enzo Turcovic
- Disciplina: Framework Front-End
- Professora: Ma. Tatiana Aparecida de Almeida
