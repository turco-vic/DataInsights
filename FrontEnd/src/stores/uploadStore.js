import { defineStore } from 'pinia';
import * as XLSX from 'xlsx';

// Store da tela de upload: guarda o arquivo escolhido, lê a planilha no próprio
// navegador (sem backend), padroniza cada linha e acumula os erros encontrados.
// O tratamento segue a aba "dicionario_dados" da planilha modelo da aula.
// Quando o backend Java existir, uma action enviarParaBackend() manda
// dadosTratados para o Spring Boot — a leitura e o tratamento continuam aqui.

// Fora do store: constantes não precisam ser recriadas a cada linha.
// As chaves são o valor da planilha já com trim() e toUpperCase() aplicados.
const MAPA_SEGMENTOS = {
  'IND.': 'Indústria',
  INDUSTRIA: 'Indústria',
  'INDÚSTRIA': 'Indústria',
  'COM.': 'Comércio',
  COMERCIO: 'Comércio',
  'COMÉRCIO': 'Comércio',
  SERVICOS: 'Serviços',
  'SERVIÇOS': 'Serviços',
  SAUDE: 'Saúde',
  'SAÚDE': 'Saúde',
  EDUCACAO: 'Educação',
  'EDUCAÇÃO': 'Educação',
  TECNOLOGIA: 'Tecnologia',
};

const NIVEIS_VALIDOS = ['A', 'B', 'C'];
const EXTENSOES_ACEITAS = ['.xlsx', '.xls', '.csv'];

// Campos de texto que o dicionário de dados marca como obrigatórios.
const CAMPOS_OBRIGATORIOS = [
  ['codigo_cliente', 'código do cliente'],
  ['nome_cliente', 'nome do cliente'],
  ['consultor', 'consultor'],
  ['segmento', 'segmento'],
];

// Ligações que permanecem em minúsculo na capitalização de nomes próprios.
const CONECTORES = ['de', 'da', 'do', 'das', 'dos', 'e'];

function capitalizar(texto) {
  return texto
    .toLowerCase()
    .split(' ')
    .map((palavra, i) =>
      i > 0 && CONECTORES.includes(palavra) ? palavra : palavra.charAt(0).toUpperCase() + palavra.slice(1),
    )
    .join(' ');
}

function converterNumero(valor) {
  if (typeof valor === 'number') return Number.isFinite(valor) ? valor : null;

  const texto = String(valor ?? '').trim();
  if (!texto) return null;

  // "1.850.000,50" é pt-BR (vírgula decimal); "1850000.50" é padrão internacional.
  const normalizado = texto.includes(',') ? texto.replace(/\./g, '').replace(',', '.') : texto;
  const numero = Number(normalizado.replace(/[^\d.-]/g, ''));
  return Number.isFinite(numero) ? numero : null;
}

function converterData(valor) {
  // Com cellDates a planilha já entrega um Date pronto.
  if (valor instanceof Date) return Number.isNaN(valor.getTime()) ? null : valor;

  // Número solto é o serial do Excel (dias contados a partir de 1900).
  if (typeof valor === 'number') {
    const partes = XLSX.SSF.parse_date_code(valor);
    return partes ? new Date(partes.y, partes.m - 1, partes.d) : null;
  }

  const texto = String(valor ?? '').trim();
  if (!texto) return null;

  // CSV costuma trazer dd/mm/aaaa, que o Date() nativo interpreta ao contrário.
  const br = texto.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (br) return new Date(Number(br[3]), Number(br[2]) - 1, Number(br[1]));

  const data = new Date(texto);
  return Number.isNaN(data.getTime()) ? null : data;
}

function separarServicos(valor) {
  return String(valor ?? '')
    .split(';')
    .map((servico) => servico.trim())
    .filter(Boolean);
}

export const useUploadStore = defineStore('upload', {
  state: () => ({
    arquivo: null, // o File vindo do input, ainda não lido
    dadosOriginais: [], // linhas exatamente como saíram da planilha
    dadosTratados: [], // as mesmas linhas já padronizadas
    erros: [], // mensagens de validação, uma por problema
    carregando: false, // controla o texto "Lendo arquivo…" na tela
  }),

  getters: {
    totalClientes: (state) => state.dadosTratados.length,
    totalErros: (state) => state.erros.length,
    clientesNivelA: (state) => state.dadosTratados.filter((c) => c.nivel_cliente === 'A').length,
    temDados: (state) => state.dadosTratados.length > 0,

    // Quantos clientes por segmento, já com os nomes unificados pelo tratamento.
    porSegmento: (state) =>
      state.dadosTratados.reduce((acc, cliente) => {
        const chave = cliente.segmento || 'Não informado';
        acc[chave] = (acc[chave] || 0) + 1;
        return acc;
      }, {}),

    // Soma só o que virou número de verdade; linha sem faturamento não entra.
    faturamentoTotal: (state) =>
      state.dadosTratados.reduce((total, c) => total + (c.faturamento_anual || 0), 0),

    // Quantas vezes cada serviço aparece, do mais contratado para o menos.
    servicosMaisContratados: (state) => {
      const contagem = state.dadosTratados.reduce((acc, cliente) => {
        cliente.servicos_contratados.forEach((servico) => {
          acc[servico] = (acc[servico] || 0) + 1;
        });
        return acc;
      }, {});
      return Object.entries(contagem).sort((a, b) => b[1] - a[1]);
    },
  },

  actions: {
    selecionarArquivo(file) {
      this.arquivo = file;
      this.erros = [];
      this.dadosOriginais = [];
      this.dadosTratados = [];
    },

    validarArquivo() {
      if (!this.arquivo) {
        this.erros.push('Selecione uma planilha.');
        return false;
      }

      const nome = this.arquivo.name.toLowerCase();
      const valido = EXTENSOES_ACEITAS.some((ext) => nome.endsWith(ext));
      if (!valido) this.erros.push(`Formato inválido. Use ${EXTENSOES_ACEITAS.join(', ')}.`);
      return valido;
    },

    async processarPlanilha() {
      if (!this.validarArquivo()) return;

      this.carregando = true;
      try {
        const buffer = await this.arquivo.arrayBuffer();
        // cellDates faz o SheetJS devolver Date em vez do número de série do Excel.
        const workbook = XLSX.read(buffer, { type: 'array', cellDates: true });
        const planilha = workbook.Sheets[workbook.SheetNames[0]]; // sempre a primeira aba
        const linhas = XLSX.utils.sheet_to_json(planilha, { defval: '' });

        if (!linhas.length) this.erros.push('A planilha está vazia ou não tem cabeçalho.');

        this.dadosOriginais = linhas;
        this.dadosTratados = linhas.map((linha) => this.tratarLinha(linha));
        this.validarLinhas();
      } catch {
        this.erros.push('Não foi possível ler o arquivo. Ele pode estar corrompido.');
      } finally {
        // No finally para que uma falha de leitura não deixe a tela travada em "Lendo…".
        this.carregando = false;
      }
    },

    // Padroniza uma linha conforme a aba "dicionario_dados":
    // String(campo || '') protege contra célula vazia, trim tira espaço sobrando
    // e o mapa unifica IND./Industria/INDUSTRIA num único "Indústria".
    tratarLinha(linha) {
      const segmento = String(linha.segmento || '').trim().toUpperCase();

      return {
        ...linha, // preserva as colunas que não tratamos
        codigo_cliente: String(linha.codigo_cliente || '').trim(),
        nome_cliente: capitalizar(String(linha.nome_cliente || '').trim()),
        consultor: capitalizar(String(linha.consultor || '').trim()),
        segmento: MAPA_SEGMENTOS[segmento] || segmento,
        nivel_cliente: String(linha.nivel_cliente || '').trim().toUpperCase(),
        faturamento_anual: converterNumero(linha.faturamento_anual),
        servicos_contratados: separarServicos(linha.servicos_contratados),
        data_contratacao: converterData(linha.data_contratacao),
        cidade: capitalizar(String(linha.cidade || '').trim()),
        uf: String(linha.uf || '').trim().toUpperCase(),
      };
    },

    validarLinhas() {
      const codigosVistos = new Set();

      this.dadosTratados.forEach((cliente, i) => {
        const linha = i + 2; // +2: a linha 1 é o cabeçalho e o índice começa em 0

        CAMPOS_OBRIGATORIOS.forEach(([campo, rotulo]) => {
          if (!cliente[campo]) this.erros.push(`Linha ${linha}: ${rotulo} vazio.`);
        });

        if (cliente.codigo_cliente) {
          if (codigosVistos.has(cliente.codigo_cliente)) {
            this.erros.push(`Linha ${linha}: código ${cliente.codigo_cliente} duplicado.`);
          } else {
            codigosVistos.add(cliente.codigo_cliente);
          }
        }

        if (!NIVEIS_VALIDOS.includes(cliente.nivel_cliente)) {
          this.erros.push(`Linha ${linha}: nível "${cliente.nivel_cliente || '—'}" inválido.`);
        }

        if (cliente.faturamento_anual === null) {
          this.erros.push(`Linha ${linha}: faturamento anual vazio ou não numérico.`);
        } else if (cliente.faturamento_anual < 0) {
          this.erros.push(`Linha ${linha}: faturamento anual negativo.`);
        }

        if (!cliente.servicos_contratados.length) {
          this.erros.push(`Linha ${linha}: nenhum serviço contratado informado.`);
        }

        if (!cliente.data_contratacao) {
          this.erros.push(`Linha ${linha}: data de contratação inválida ou vazia.`);
        }

        // cidade e uf são opcionais: só valida o formato quando vierem preenchidos.
        if (cliente.uf && cliente.uf.length !== 2) {
          this.erros.push(`Linha ${linha}: UF "${cliente.uf}" deve ter 2 caracteres.`);
        }
      });
    },

    limpar() {
      this.arquivo = null;
      this.dadosOriginais = [];
      this.dadosTratados = [];
      this.erros = [];
    },
  },
});
