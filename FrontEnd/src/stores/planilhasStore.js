import { defineStore } from 'pinia';
import api, { mensagemErro } from '../services/api';
import { TIPOS_ERRO } from './uploadStore';

const ATIVA_KEY = 'di_importacao_ativa';

const OBRIGATORIOS = [
  'codigo_cliente', 'nome_cliente', 'email', 'consultor', 'segmento',
  'nivel_cliente', 'faturamento_anual', 'servicos_contratados', 'data_contratacao',
];

/** "2025-05-01" → Date local (sem deslocar o dia por causa do fuso). */
function dataLocal(iso) {
  if (!iso) return null;
  const [ano, mes, dia] = String(iso).slice(0, 10).split('-').map(Number);
  return new Date(ano, mes - 1, dia);
}

/** Converte o detalhe devolvido por /api/importacoes/{id} no formato usado pelo relatório do dataset. */
export function paraPlanilha(detalhe) {
  const r = detalhe.resumo;
  return {
    id: String(r.id),
    nome: r.nomeArquivo,
    autor: r.autorNome || '—',
    enviadaEm: r.enviadaEm,
    totalLinhas: r.totalLinhas,
    linhasComErro: r.linhasComErro,
    criados: r.criados,
    atualizados: r.atualizados,
    status: r.status,
    erros: detalhe.erros || [],
    rejeitados: detalhe.rejeitados || [],
    clientes: (detalhe.clientes || []).map((c) => ({
      codigo_cliente: c.codigoCliente,
      nome_cliente: c.nomeCliente,
      email: c.email,
      consultor: c.consultor,
      segmento: c.segmento,
      nivel_cliente: c.nivelCliente,
      faturamento_anual: c.faturamentoAnual == null ? null : Number(c.faturamentoAnual),
      servicos_contratados: c.servicosContratados || [],
      data_contratacao: dataLocal(c.dataContratacao),
      cidade: c.cidade || '',
      uf: c.uf || '',
    })),
  };
}

function contar(itens) {
  const contagem = itens.reduce((acc, item) => {
    acc[item] = (acc[item] || 0) + 1;
    return acc;
  }, {});
  return Object.entries(contagem).sort((a, b) => b[1] - a[1]);
}

const preenchido = (v) => (Array.isArray(v) ? v.length > 0 : v !== null && v !== undefined && v !== '');

function tipoDaColuna(valores) {
  const amostra = valores.filter(preenchido);
  if (!amostra.length) return 'Vazia';
  if (amostra.every((v) => v instanceof Date)) return 'Data';
  if (amostra.every((v) => typeof v === 'number')) return 'Número';
  if (amostra.every(Array.isArray)) return 'Lista';
  return new Set(amostra).size <= 10 ? 'Categoria' : 'Texto';
}

function exemplo(v) {
  if (v instanceof Date) return v.toLocaleDateString('pt-BR');
  if (Array.isArray(v)) return v.join('; ');
  return String(v);
}

/** Histórico de planilhas enviadas, lido da API. */
export const usePlanilhasStore = defineStore('planilhas', {
  state: () => ({
    lista: [],
    carregada: false,
    carregando: false,
    erro: '',
    ativaId: localStorage.getItem(ATIVA_KEY),
    detalhe: null,
    carregandoDetalhe: false,
    erroDetalhe: '',
  }),

  getters: {
    temPlanilhas: (state) => state.lista.length > 0,
    ativaResumo: (state) => state.lista.find((p) => String(p.id) === state.ativaId) || state.lista[0] || null,
    ativa() {
      return this.detalhe && this.ativaResumo && this.detalhe.id === String(this.ativaResumo.id) ? this.detalhe : null;
    },
    clientes() {
      return this.ativa?.clientes || [];
    },

    porNivel() {
      return ['A', 'B', 'C'].map((n) => [n, this.clientes.filter((c) => c.nivel_cliente === n).length]);
    },
    porSegmento() {
      return contar(this.clientes.map((c) => c.segmento || 'Não informado'));
    },
    porConsultor() {
      return contar(this.clientes.map((c) => c.consultor || 'Sem consultor'));
    },
    servicos() {
      return contar(this.clientes.flatMap((c) => c.servicos_contratados));
    },

    faturamentoPorSegmento() {
      const soma = {};
      this.clientes.forEach((c) => {
        const s = c.segmento || 'Não informado';
        soma[s] = (soma[s] || 0) + (c.faturamento_anual || 0);
      });
      return Object.entries(soma).sort((a, b) => b[1] - a[1]);
    },

    qualidade() {
      const p = this.ativa;
      if (!p || !p.totalLinhas) return [];

      const duplicadas = new Set(
        p.erros.filter((e) => e.tipo === TIPOS_ERRO.DUPLICADO).map((e) => e.linha),
      ).size;

      const celulas = this.clientes.length * OBRIGATORIOS.length;
      const cheias = this.clientes.reduce(
        (t, c) => t + OBRIGATORIOS.filter((campo) => preenchido(c[campo])).length, 0);

      return [
        { label: 'Completude dos campos obrigatórios', pct: celulas ? (cheias / celulas) * 100 : 0 },
        { label: 'Linhas sem inconsistência', pct: ((p.totalLinhas - p.linhasComErro) / p.totalLinhas) * 100 },
        { label: 'Registros únicos', pct: ((p.totalLinhas - duplicadas) / p.totalLinhas) * 100 },
      ];
    },

    colunas() {
      if (!this.clientes.length) return [];
      return Object.keys(this.clientes[0]).map((nome) => {
        const valores = this.clientes.map((c) => c[nome]);
        const cheios = valores.filter(preenchido);
        return {
          nome,
          tipo: tipoDaColuna(valores),
          preench: (cheios.length / valores.length) * 100,
          exemplo: cheios.length ? exemplo(cheios[0]) : '—',
        };
      });
    },

    /** Observações sobre a planilha (não são os insights por cliente do back-end). */
    insights() {
      if (!this.clientes.length) return [];
      const total = this.clientes.length;
      const pct = (n) => `${Math.round((n / total) * 100)}%`;
      const lista = [];

      const [segmento, qtdSeg] = this.porSegmento[0];
      lista.push(`${segmento} é o segmento com mais clientes: ${qtdSeg} de ${total} (${pct(qtdSeg)}).`);

      const [segFat] = this.faturamentoPorSegmento;
      if (segFat) lista.push(`${segFat[0]} concentra o maior faturamento anual somado da planilha.`);

      const [consultor, qtdCons] = this.porConsultor[0];
      lista.push(`${consultor} atende a maior carteira, com ${qtdCons} clientes.`);

      if (this.servicos.length) {
        const [servico, qtdServ] = this.servicos[0];
        lista.push(`${servico} é o serviço mais contratado, presente em ${qtdServ} clientes.`);
      }

      const nivelA = this.porNivel[0][1];
      lista.push(`${pct(nivelA)} da planilha está no nível A.`);
      return lista;
    },
  },

  actions: {
    async carregarLista() {
      this.carregando = true;
      this.erro = '';
      try {
        const { data } = await api.get('/api/importacoes');
        this.lista = data;
        this.carregada = true;
      } catch (error) {
        this.erro = mensagemErro(error, 'Não foi possível carregar as planilhas.');
      } finally {
        this.carregando = false;
      }
    },

    /** Carrega a lista (se preciso) e o detalhe da planilha ativa. */
    async garantirAtiva() {
      if (!this.carregada) await this.carregarLista();
      const resumo = this.ativaResumo;
      if (!resumo || this.ativa) return;
      this.carregandoDetalhe = true;
      this.erroDetalhe = '';
      try {
        const { data } = await api.get(`/api/importacoes/${resumo.id}`);
        this.detalhe = paraPlanilha(data);
      } catch (error) {
        this.erroDetalhe = mensagemErro(error, 'Não foi possível abrir a planilha.');
      } finally {
        this.carregandoDetalhe = false;
      }
    },

    async selecionar(id) {
      this.ativaId = String(id);
      localStorage.setItem(ATIVA_KEY, this.ativaId);
      await this.garantirAtiva();
    },

    /** Apaga do histórico. Os clientes importados continuam no banco. */
    async remover(id) {
      await api.delete(`/api/importacoes/${id}`);
      if (this.detalhe?.id === String(id)) this.detalhe = null;
      if (this.ativaId === String(id)) {
        this.ativaId = null;
        localStorage.removeItem(ATIVA_KEY);
      }
      await this.carregarLista();
    },

    limpar() {
      this.$reset();
      this.ativaId = localStorage.getItem(ATIVA_KEY);
    },
  },
});
