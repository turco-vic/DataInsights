import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { paraPlanilha, usePlanilhasStore } from './planilhasStore';

const detalhe = {
  resumo: {
    id: 7, nomeArquivo: 'carteira.xlsx', autorNome: 'Ana', enviadaEm: '2026-09-30T10:00:00',
    totalLinhas: 3, linhasComErro: 1, criados: 2, atualizados: 0, rejeitados: 0, status: 'erro',
  },
  erros: [{ linha: 4, campo: 'email', tipo: 'Registro duplicado', descricao: 'x' }],
  rejeitados: [],
  clientes: [
    { codigoCliente: 'CTI001', nomeCliente: 'A', email: 'a@a.com', consultor: 'Bia', segmento: 'Saúde', nivelCliente: 'A',
      faturamentoAnual: 100, servicosContratados: ['Backup'], dataContratacao: '2025-05-01', cidade: null, uf: 'SP' },
    { codigoCliente: 'CTI002', nomeCliente: 'B', email: 'b@b.com', consultor: 'Bia', segmento: 'Saúde', nivelCliente: 'B',
      faturamentoAnual: 50, servicosContratados: ['Backup', 'Cloud'], dataContratacao: '2025-06-01', cidade: 'Rio', uf: 'RJ' },
  ],
};

describe('planilhasStore', () => {
  beforeEach(() => setActivePinia(createPinia()));

  it('converte o detalhe da API para o formato do relatório', () => {
    const p = paraPlanilha(detalhe);
    expect(p.id).toBe('7');
    expect(p.nome).toBe('carteira.xlsx');
    expect(p.clientes[0]).toMatchObject({ codigo_cliente: 'CTI001', nivel_cliente: 'A', faturamento_anual: 100, uf: 'SP' });
    expect(p.clientes[0].data_contratacao).toEqual(new Date(2025, 4, 1));
  });

  it('calcula colunas, qualidade e observações da planilha ativa', () => {
    const store = usePlanilhasStore();
    store.lista = [detalhe.resumo];
    store.ativaId = '7';
    store.detalhe = paraPlanilha(detalhe);

    expect(store.ativa.nome).toBe('carteira.xlsx');
    expect(store.colunas.find((c) => c.nome === 'data_contratacao').tipo).toBe('Data');
    expect(store.colunas.find((c) => c.nome === 'servicos_contratados').tipo).toBe('Lista');
    expect(store.qualidade.find((q) => q.label === 'Registros únicos').pct).toBeCloseTo((2 / 3) * 100);
    expect(store.servicos[0]).toEqual(['Backup', 2]);
    expect(store.insights[0]).toContain('Saúde');
  });

  it('ignora o detalhe se ele não for da planilha ativa', () => {
    const store = usePlanilhasStore();
    store.lista = [detalhe.resumo, { ...detalhe.resumo, id: 8 }];
    store.ativaId = '8';
    store.detalhe = paraPlanilha(detalhe);
    expect(store.ativa).toBeNull();
  });
});
