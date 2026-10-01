import { defineStore } from 'pinia';
// A biblioteca de planilhas é grande: só é baixada quando alguém processa um arquivo.
let XLSX = null;
import api, { mensagemErro } from '../services/api';
import { NIVEIS, SEGMENTOS, UFS } from '../utils/dominio';

export const TIPOS_ERRO = {
  COLUNA: 'Coluna ausente',
  VAZIO: 'Campo vazio',
  EMAIL: 'E-mail inválido',
  DUPLICADO: 'Registro duplicado',
  FORA_PADRAO: 'Dado fora do padrão',
};

export const TIPOS_AVISO = {
  ESPACO: 'Espaço em branco',
  PADRONIZADO: 'Texto padronizado',
};

const MAPA_SEGMENTOS = {
  'IND.': 'Indústria',
  INDUSTRIA: 'Indústria',
  'INDÚSTRIA': 'Indústria',
  'COM.': 'Comércio',
  COMERCIO: 'Comércio',
  'COMÉRCIO': 'Comércio',
  SERVICO: 'Serviços',
  'SERVIÇO': 'Serviços',
  SERVICOS: 'Serviços',
  'SERVIÇOS': 'Serviços',
  SAUDE: 'Saúde',
  'SAÚDE': 'Saúde',
  EDUCACAO: 'Educação',
  'EDUCAÇÃO': 'Educação',
  TECNOLOGIA: 'Tecnologia',
};
const SEGMENTOS_VALIDOS = SEGMENTOS;
const NIVEIS_VALIDOS = NIVEIS;
const EXTENSOES_ACEITAS = ['.xlsx', '.xls', '.csv'];
export const TAMANHO_MAXIMO_MB = 50;

const PADRAO_CODIGO = /^CTI\d{3,}$/;
const PADRAO_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

const CAMPOS_OBRIGATORIOS = {
  codigo_cliente: 'Código do cliente',
  nome_cliente: 'Nome do cliente',
  email: 'E-mail',
  consultor: 'Consultor',
  segmento: 'Segmento',
  nivel_cliente: 'Nível do cliente',
  faturamento_anual: 'Faturamento anual',
  servicos_contratados: 'Serviços contratados',
  data_contratacao: 'Data de contratação',
};
export const COLUNAS_MODELO = [...Object.keys(CAMPOS_OBRIGATORIOS), 'cidade', 'uf'];

const CONECTORES = ['de', 'da', 'do', 'das', 'dos', 'e'];

const vazio = (valor) => valor === null || valor === undefined || String(valor).trim() === '';

function capitalizar(texto) {
  if (texto !== texto.toUpperCase() && texto !== texto.toLowerCase()) return texto;
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

  let texto = String(valor ?? '').replace(/R\$|\s/g, '');
  if (!texto) return null;

  const decimal = texto.lastIndexOf(',') > texto.lastIndexOf('.') ? ',' : '.';
  texto = texto.split(decimal === ',' ? '.' : ',').join('');
  if (texto.split(decimal).length > 2) texto = texto.split(decimal).join('');
  texto = texto.replace(decimal, '.');

  return /^-?\d+(\.\d+)?$/.test(texto) ? Number(texto) : null;
}

function converterData(valor) {
  if (valor instanceof Date) return Number.isNaN(valor.getTime()) ? null : valor;

  if (typeof valor === 'number') {
    const partes = XLSX.SSF.parse_date_code(valor);
    return partes ? new Date(partes.y, partes.m - 1, partes.d) : null;
  }

  const texto = String(valor ?? '').trim();
  if (!texto) return null;

  const br = texto.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
  if (br) {
    const [, dia, mes, ano] = br.map(Number);
    const data = new Date(ano, mes - 1, dia);
    return data.getDate() === dia && data.getMonth() === mes - 1 ? data : null;
  }

  const data = new Date(texto);
  return Number.isNaN(data.getTime()) ? null : data;
}

function separarServicos(valor) {
  return String(valor ?? '')
    .split(';')
    .map((servico) => servico.trim())
    .filter(Boolean);
}

function dataIso(data) {
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${data.getFullYear()}-${mes}-${dia}`;
}

function normalizarLinha(linha, i) {
  const nova = { numero_linha: (linha.__rowNum__ ?? i + 1) + 1 };
  Object.entries(linha).forEach(([coluna, valor]) => {
    nova[coluna.trim().toLowerCase().replace(/\s+/g, '_')] = valor;
  });
  return nova;
}

function contarPorTipo(lista, tipos) {
  return Object.values(tipos).map((tipo) => ({
    tipo,
    quantidade: lista.filter((item) => item.tipo === tipo).length,
  }));
}

export const useUploadStore = defineStore('upload', {
  state: () => ({
    arquivo: null,
    erroArquivo: '',
    dataValidacao: null,
    dadosOriginais: [],
    dadosTratados: [],
    erros: [],
    avisos: [],
    carregando: false,
    importando: false,
    resultadoImportacao: null,
    erroImportacao: '',
    // Preferência "Tratar dados automaticamente": desligada, nada é corrigido e divergências viram erros.
    tratarAutomaticamente: true,
  }),

  getters: {
    nomeArquivo: (state) => state.arquivo?.name || '',
    temDados: (state) => state.dadosTratados.length > 0,
    totalRegistros: (state) => state.dadosTratados.length,
    totalErros: (state) => state.erros.length,
    totalAvisos: (state) => state.avisos.length,

    errosEstrutura: (state) => state.erros.filter((e) => e.linha === 1),

    numerosLinhasComErro: (state) => new Set(state.erros.map((e) => e.linha)),
    linhasValidas() {
      return this.dadosTratados.filter((l) => !this.numerosLinhasComErro.has(l.numero_linha));
    },
    linhasComErro() {
      return this.dadosTratados.filter((l) => this.numerosLinhasComErro.has(l.numero_linha));
    },
    registrosValidos() {
      return this.linhasValidas.length;
    },
    registrosComErro() {
      return this.linhasComErro.length;
    },
    percentualValidos() {
      return this.totalRegistros ? (this.registrosValidos / this.totalRegistros) * 100 : 0;
    },

    errosPorTipo: (state) => contarPorTipo(state.erros, TIPOS_ERRO),
    avisosPorTipo: (state) => contarPorTipo(state.avisos, TIPOS_AVISO),

    podeEnviar() {
      return this.errosEstrutura.length === 0 && this.registrosValidos > 0;
    },
  },

  actions: {
    selecionarArquivo(file) {
      this.limpar();
      this.arquivo = file;
    },

    validarArquivo() {
      if (!this.arquivo) {
        this.erroArquivo = 'Selecione uma planilha.';
        return false;
      }

      const nome = this.arquivo.name.toLowerCase();
      if (!EXTENSOES_ACEITAS.some((ext) => nome.endsWith(ext))) {
        this.erroArquivo = `Formato inválido. Use ${EXTENSOES_ACEITAS.join(', ')}.`;
        return false;
      }
      if (this.arquivo.size > TAMANHO_MAXIMO_MB * 1024 * 1024) {
        const mb = (this.arquivo.size / 1024 / 1024).toLocaleString('pt-BR', { maximumFractionDigits: 1 });
        this.erroArquivo = `O arquivo tem ${mb} MB. O limite é ${TAMANHO_MAXIMO_MB} MB.`;
        return false;
      }
      return true;
    },

    async processarPlanilha({ tratar = true } = {}) {
      this.tratarAutomaticamente = tratar;
      if (!this.validarArquivo()) return;

      this.carregando = true;
      try {
        XLSX ??= await import('xlsx');
        const buffer = await this.arquivo.arrayBuffer();
        // Em CSV a biblioteca leria "05/07/2024" no padrão americano (7 de maio). Lendo como texto,
        // datas e números passam pela conversão brasileira (DD/MM/AAAA e vírgula decimal).
        const csv = this.arquivo.name.toLowerCase().endsWith('.csv');
        const workbook = XLSX.read(buffer, { type: 'array', cellDates: true, raw: csv });
        const planilha = workbook.Sheets[workbook.SheetNames[0]];
        const linhas = XLSX.utils.sheet_to_json(planilha, { defval: '' });

        if (!linhas.length) {
          this.erroArquivo = 'A planilha está vazia ou não tem cabeçalho.';
          return;
        }

        this.dadosOriginais = linhas.map(normalizarLinha);
        this.validarColunas();
        this.dadosTratados = this.dadosOriginais.map((linha) => this.tratarLinha(linha));
        this.validarLinhas();
        this.dataValidacao = new Date();
        this.registrarValidacao();
      } catch (error) {
        console.error(error);
        this.erroArquivo = 'Não foi possível ler o arquivo. Ele pode estar corrompido.';
      } finally {
        this.carregando = false;
      }
    },

    registrarErro(linha, campo, tipo, descricao) {
      this.erros.push({ linha, campo, tipo, descricao });
    },

    registrarAviso(linha, campo, tipo, descricao) {
      this.avisos.push({ linha, campo, tipo, descricao });
    },

    validarColunas() {
      const colunas = Object.keys(this.dadosOriginais[0]);
      Object.entries(CAMPOS_OBRIGATORIOS).forEach(([campo, rotulo]) => {
        if (!colunas.includes(campo)) {
          this.registrarErro(1, campo, TIPOS_ERRO.COLUNA, `A coluna "${campo}" (${rotulo}) não existe no cabeçalho.`);
        }
      });
    },

    tratarLinha(linha) {
      const numero = linha.numero_linha;

      const texto = (campo, padronizar = (v) => v) => {
        const original = typeof linha[campo] === 'string' ? linha[campo] : String(linha[campo] ?? '');
        if (!this.tratarAutomaticamente) return original;
        const limpo = original.trim().replace(/\s+/g, ' ');
        if (limpo !== original) {
          this.registrarAviso(numero, campo, TIPOS_AVISO.ESPACO, `Espaços extras removidos: "${original}" → "${limpo}".`);
        }
        const final = padronizar(limpo);
        if (final !== limpo) {
          this.registrarAviso(numero, campo, TIPOS_AVISO.PADRONIZADO, `"${limpo}" padronizado para "${final}".`);
        }
        return final;
      };

      return {
        ...linha,
        codigo_cliente: texto('codigo_cliente', (v) => v.toUpperCase()),
        nome_cliente: texto('nome_cliente', capitalizar),
        email: texto('email', (v) => v.toLowerCase()),
        consultor: texto('consultor', capitalizar),
        segmento: texto('segmento', (v) => MAPA_SEGMENTOS[v.toUpperCase()] || v),
        nivel_cliente: texto('nivel_cliente', (v) => v.toUpperCase()),
        faturamento_anual: converterNumero(linha.faturamento_anual),
        servicos_contratados: separarServicos(linha.servicos_contratados),
        data_contratacao: converterData(linha.data_contratacao),
        cidade: texto('cidade', capitalizar),
        uf: texto('uf', (v) => v.toUpperCase()),
      };
    },

    validarLinhas() {
      const colunasAusentes = new Set(this.errosEstrutura.map((e) => e.campo));
      const hoje = new Date();
      hoje.setHours(23, 59, 59, 999);

      const vistos = { codigo_cliente: new Map(), email: new Map() };
      const conferirDuplicado = (campo, rotulo, valor, numero) => {
        if (!valor) return;
        const primeira = vistos[campo].get(valor.toLowerCase());
        if (primeira) {
          this.registrarErro(numero, campo, TIPOS_ERRO.DUPLICADO, `${rotulo} "${valor}" já aparece na linha ${primeira}.`);
        } else {
          vistos[campo].set(valor.toLowerCase(), numero);
        }
      };

      this.dadosTratados.forEach((cliente, i) => {
        const original = this.dadosOriginais[i];
        const numero = cliente.numero_linha;
        const erro = (campo, tipo, descricao) => this.registrarErro(numero, campo, tipo, descricao);

        Object.entries(CAMPOS_OBRIGATORIOS).forEach(([campo, rotulo]) => {
          if (!colunasAusentes.has(campo) && vazio(original[campo])) {
            erro(campo, TIPOS_ERRO.VAZIO, `${rotulo} não preenchido.`);
          }
        });

        if (cliente.codigo_cliente && !PADRAO_CODIGO.test(cliente.codigo_cliente)) {
          erro('codigo_cliente', TIPOS_ERRO.FORA_PADRAO, `Código "${cliente.codigo_cliente}" fora do padrão. Use CTI seguido de números (ex.: CTI001).`);
        }

        if (cliente.email && !PADRAO_EMAIL.test(cliente.email)) {
          erro('email', TIPOS_ERRO.EMAIL, `"${cliente.email}" não é um e-mail válido (ex.: contato@empresa.com.br).`);
        }

        if (cliente.segmento && !SEGMENTOS_VALIDOS.includes(cliente.segmento)) {
          erro('segmento', TIPOS_ERRO.FORA_PADRAO, `Segmento "${cliente.segmento}" não reconhecido. Use: ${SEGMENTOS_VALIDOS.join(', ')}.`);
        }

        if (cliente.nivel_cliente && !NIVEIS_VALIDOS.includes(cliente.nivel_cliente)) {
          erro('nivel_cliente', TIPOS_ERRO.FORA_PADRAO, `Nível "${cliente.nivel_cliente}" inválido. Use A, B ou C.`);
        }

        if (!vazio(original.faturamento_anual)) {
          if (cliente.faturamento_anual === null) {
            erro('faturamento_anual', TIPOS_ERRO.FORA_PADRAO, `Faturamento "${original.faturamento_anual}" não é um número.`);
          } else if (cliente.faturamento_anual < 0) {
            erro('faturamento_anual', TIPOS_ERRO.FORA_PADRAO, 'Faturamento anual negativo.');
          }
        }

        if (!vazio(original.servicos_contratados) && !cliente.servicos_contratados.length) {
          erro('servicos_contratados', TIPOS_ERRO.FORA_PADRAO, 'Nenhum serviço informado. Separe os serviços com ";".');
        }

        if (!vazio(original.data_contratacao)) {
          if (!cliente.data_contratacao) {
            erro('data_contratacao', TIPOS_ERRO.FORA_PADRAO, `Data "${original.data_contratacao}" inválida. Use DD/MM/AAAA.`);
          } else if (cliente.data_contratacao > hoje) {
            erro('data_contratacao', TIPOS_ERRO.FORA_PADRAO, `Data ${cliente.data_contratacao.toLocaleDateString('pt-BR')} está no futuro.`);
          }
        }

        if (cliente.uf && !UFS.includes(cliente.uf)) {
          erro('uf', TIPOS_ERRO.FORA_PADRAO, `UF "${cliente.uf}" não existe.`);
        }

        conferirDuplicado('codigo_cliente', 'Código', cliente.codigo_cliente, numero);
        conferirDuplicado('email', 'E-mail', cliente.email, numero);
      });
    },

    /** Registra a validação na telemetria. Falhas aqui não atrapalham o upload. */
    registrarValidacao() {
      api.post('/api/telemetrias', {
        evento: 'planilha_validada',
        mensagem: `${this.nomeArquivo}: ${this.totalRegistros} linha(s), ${this.totalErros} erro(s), ${this.totalAvisos} correção(ões)`,
      }).catch(() => {});
    },

    async importarValidos() {
      this.erroImportacao = '';
      this.resultadoImportacao = null;

      if (!this.podeEnviar) {
        this.erroImportacao = 'Não há registros válidos para importar.';
        return false;
      }

      const clientes = this.linhasValidas.map((c) => ({
        codigoCliente: c.codigo_cliente,
        nomeCliente: c.nome_cliente,
        email: c.email,
        consultor: c.consultor,
        segmento: c.segmento,
        nivelCliente: c.nivel_cliente,
        faturamentoAnual: c.faturamento_anual,
        servicosContratados: c.servicos_contratados,
        dataContratacao: dataIso(c.data_contratacao),
        cidade: c.cidade || null,
        uf: c.uf || null,
      }));

      this.importando = true;
      try {
        // O back-end importa os clientes e guarda o histórico (Planilhas enviadas / Relatório do dataset).
        const resposta = await api.post('/api/importacoes', {
          nomeArquivo: this.nomeArquivo,
          totalLinhas: this.totalRegistros,
          erros: this.erros,
          clientes,
        });
        this.resultadoImportacao = resposta.data;
        return true;
      } catch (error) {
        console.error(error);
        this.erroImportacao = mensagemErro(
          error,
          `O servidor recusou os dados (erro ${error.response?.status}).`,
        );
        return false;
      } finally {
        this.importando = false;
      }
    },

    limpar() {
      this.arquivo = null;
      this.erroArquivo = '';
      this.dataValidacao = null;
      this.dadosOriginais = [];
      this.dadosTratados = [];
      this.erros = [];
      this.avisos = [];
      this.importando = false;
      this.resultadoImportacao = null;
      this.erroImportacao = '';
    },
  },
});
