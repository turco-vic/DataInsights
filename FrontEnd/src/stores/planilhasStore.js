import { defineStore } from 'pinia';

// Planilhas enviadas: cada upload tratado fica guardado aqui e no localStorage,
// então Dashboard, Planilhas e Dataset trabalham com dados reais mesmo sem backend.
// Quando o Spring Boot existir, salvar() vira um POST e o carregamento vira um GET.

const STORAGE_KEY = 'di_planilhas';
const ATIVA_KEY = 'di_planilha_ativa';

const MESES = ['JAN', 'FEV', 'MAR', 'ABR', 'MAI', 'JUN', 'JUL', 'AGO', 'SET', 'OUT', 'NOV', 'DEZ'];

// Campos obrigatórios do dicionário de dados, usados na métrica de completude.
const OBRIGATORIOS = [
  'codigo_cliente', 'nome_cliente', 'consultor', 'segmento',
  'nivel_cliente', 'faturamento_anual', 'servicos_contratados', 'data_contratacao',
];

// No JSON a data vira texto; ao carregar, volta a ser Date.
function carregar() {
  try {
    const lista = JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];
    lista.forEach((p) =>
      p.clientes.forEach((c) => {
        c.data_contratacao = c.data_contratacao ? new Date(c.data_contratacao) : null;
      }),
    );
    return lista;
  } catch {
    return [];
  }
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

export const usePlanilhasStore = defineStore('planilhas', {
  state: () => ({
    planilhas: carregar(), // da mais recente para a mais antiga
    ativaId: localStorage.getItem(ATIVA_KEY),
  }),

  getters: {
    temPlanilhas: (state) => state.planilhas.length > 0,
    ativa: (state) => state.planilhas.find((p) => p.id === state.ativaId) || state.planilhas[0] || null,
    clientes() {
      return this.ativa?.clientes || [];
    },

    faturamentoTotal() {
      return this.clientes.reduce((t, c) => t + (c.faturamento_anual || 0), 0);
    },
    faturamentoMedio() {
      const comValor = this.clientes.filter((c) => typeof c.faturamento_anual === 'number');
      return comValor.length ? this.faturamentoTotal / comValor.length : 0;
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

    // Faturamento somado por segmento, do maior para o menor.
    faturamentoPorSegmento() {
      const soma = {};
      this.clientes.forEach((c) => {
        const s = c.segmento || 'Não informado';
        soma[s] = (soma[s] || 0) + (c.faturamento_anual || 0);
      });
      return Object.entries(soma).sort((a, b) => b[1] - a[1]);
    },

    // Contratações por mês, preenchendo com zero os meses sem nenhuma.
    contratacoesPorMes() {
      const datas = this.clientes.map((c) => c.data_contratacao).filter(Boolean);
      if (!datas.length) return [];

      const chave = (d) => d.getFullYear() * 12 + d.getMonth();
      const contagem = {};
      datas.forEach((d) => { contagem[chave(d)] = (contagem[chave(d)] || 0) + 1; });

      const inicio = Math.min(...datas.map(chave));
      const fim = Math.max(...datas.map(chave));
      const meses = [];
      for (let k = inicio; k <= fim; k++) {
        const ano = String(Math.floor(k / 12)).slice(2);
        meses.push({ rotulo: `${MESES[k % 12]}/${ano}`, quantidade: contagem[k] || 0 });
      }
      return meses;
    },

    maioresClientes() {
      return [...this.clientes]
        .filter((c) => typeof c.faturamento_anual === 'number')
        .sort((a, b) => b.faturamento_anual - a.faturamento_anual)
        .slice(0, 8);
    },

    qualidade() {
      const p = this.ativa;
      if (!p || !p.totalLinhas) return [];

      const celulas = this.clientes.length * OBRIGATORIOS.length;
      const cheias = this.clientes.reduce(
        (t, c) => t + OBRIGATORIOS.filter((campo) => preenchido(c[campo])).length, 0);

      return [
        { label: 'Completude dos campos obrigatórios', pct: celulas ? (cheias / celulas) * 100 : 0 },
        { label: 'Linhas sem inconsistência', pct: ((p.totalLinhas - p.linhasComErro) / p.totalLinhas) * 100 },
        { label: 'Registros únicos', pct: (this.clientes.length / p.totalLinhas) * 100 },
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

    insights() {
      if (!this.clientes.length) return [];
      const total = this.clientes.length;
      const pct = (n) => `${Math.round((n / total) * 100)}%`;
      const lista = [];

      const [segmento, qtdSeg] = this.porSegmento[0];
      lista.push(`${segmento} é o segmento com mais clientes: ${qtdSeg} de ${total} (${pct(qtdSeg)}).`);

      const [segFat] = this.faturamentoPorSegmento;
      if (segFat) lista.push(`${segFat[0]} concentra o maior faturamento anual somado da carteira.`);

      const [consultor, qtdCons] = this.porConsultor[0];
      lista.push(`${consultor} atende a maior carteira, com ${qtdCons} clientes.`);

      if (this.servicos.length) {
        const [servico, qtdServ] = this.servicos[0];
        lista.push(`${servico} é o serviço mais contratado, presente em ${qtdServ} clientes.`);
      }

      const nivelA = this.porNivel[0][1];
      lista.push(`${pct(nivelA)} da carteira está no nível A.`);
      return lista;
    },
  },

  actions: {
    // Guarda o resultado de um upload. Reenviar um arquivo com o mesmo nome substitui o anterior.
    salvar({ nome, autor, dadosTratados, erros }) {
      // Duplicados ficam de fora dos indicadores ("manter único" no dicionário de dados).
      const vistos = new Set();
      const clientes = dadosTratados.filter((c) => {
        if (!c.codigo_cliente) return true;
        if (vistos.has(c.codigo_cliente)) return false;
        vistos.add(c.codigo_cliente);
        return true;
      });

      const linhasComErro = new Set(
        erros.map((e) => e.match(/^Linha (\d+)/)?.[1]).filter(Boolean),
      ).size;

      const planilha = {
        id: `${Date.now()}`,
        nome,
        autor,
        enviadaEm: new Date().toISOString(),
        totalLinhas: dadosTratados.length,
        linhasComErro,
        erros,
        status: erros.length ? 'erro' : 'processado',
        clientes,
      };

      const anteriores = this.planilhas.filter((p) => p.nome !== nome);
      this.planilhas = [planilha, ...anteriores];
      this.ativaId = planilha.id;

      try {
        this.persistir();
        return true;
      } catch {
        // localStorage tem ~5 MB: planilha grande demais fica só na memória, até o F5.
        return false;
      }
    },

    selecionar(id) {
      this.ativaId = id;
      localStorage.setItem(ATIVA_KEY, id);
    },

    remover(id) {
      this.planilhas = this.planilhas.filter((p) => p.id !== id);
      if (this.ativaId === id) this.ativaId = this.planilhas[0]?.id || null;
      this.persistir();
    },

    persistir() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.planilhas));
      if (this.ativaId) localStorage.setItem(ATIVA_KEY, this.ativaId);
      else localStorage.removeItem(ATIVA_KEY);
    },
  },
});
