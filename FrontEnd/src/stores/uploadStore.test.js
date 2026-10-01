import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { TIPOS_AVISO, TIPOS_ERRO, useUploadStore } from './uploadStore';

const linha = (numero, campos = {}) => ({
  numero_linha: numero,
  codigo_cliente: 'CTI001',
  nome_cliente: 'Empresa Teste',
  email: 'contato@empresa.com',
  consultor: 'Ana Souza',
  segmento: 'Tecnologia',
  nivel_cliente: 'A',
  faturamento_anual: '1.500,50',
  servicos_contratados: 'Backup; Cloud',
  data_contratacao: '10/03/2024',
  cidade: 'São Paulo',
  uf: 'SP',
  ...campos,
});

function validar(store, linhas, tratar = true) {
  store.tratarAutomaticamente = tratar;
  store.dadosOriginais = linhas;
  store.validarColunas();
  store.dadosTratados = linhas.map((l) => store.tratarLinha(l));
  store.validarLinhas();
}

describe('uploadStore', () => {
  beforeEach(() => setActivePinia(createPinia()));

  it('trata e aceita uma linha correta', () => {
    const store = useUploadStore();
    validar(store, [linha(2, { codigo_cliente: ' cti001 ', segmento: 'ind.' })]);

    expect(store.erros).toEqual([]);
    const [cliente] = store.linhasValidas;
    expect(cliente.codigo_cliente).toBe('CTI001');
    expect(cliente.segmento).toBe('Indústria');
    expect(cliente.faturamento_anual).toBe(1500.5);
    expect(cliente.servicos_contratados).toEqual(['Backup', 'Cloud']);
    expect(cliente.data_contratacao).toEqual(new Date(2024, 2, 10));
    expect(store.avisos.map((a) => a.tipo)).toContain(TIPOS_AVISO.PADRONIZADO);
  });

  it('sem tratamento automático, divergências viram erros', () => {
    const store = useUploadStore();
    validar(store, [linha(2, { codigo_cliente: ' cti001 ', segmento: 'ind.' })], false);

    expect(store.avisos).toEqual([]);
    expect(store.erros.map((e) => e.campo)).toEqual(expect.arrayContaining(['codigo_cliente', 'segmento']));
    expect(store.registrosValidos).toBe(0);
  });

  it('aponta campos vazios, e-mail inválido e duplicados', () => {
    const store = useUploadStore();
    validar(store, [
      linha(2),
      linha(3, { email: 'sem-arroba' }),
      linha(4, { nome_cliente: '' }),
      linha(5, { codigo_cliente: 'CTI001', email: 'outro@empresa.com' }),
    ]);

    const tipos = store.erros.map((e) => `${e.linha}:${e.tipo}`);
    expect(tipos).toContain(`3:${TIPOS_ERRO.EMAIL}`);
    expect(tipos).toContain(`4:${TIPOS_ERRO.VAZIO}`);
    expect(tipos).toContain(`5:${TIPOS_ERRO.DUPLICADO}`);
    expect(store.registrosValidos).toBe(1);
  });

  it('bloqueia o envio quando falta uma coluna obrigatória', () => {
    const store = useUploadStore();
    const { email, ...semEmail } = linha(2);
    expect(email).toBeTruthy();
    validar(store, [semEmail]);

    expect(store.errosEstrutura).toHaveLength(1);
    expect(store.podeEnviar).toBe(false);
  });

  it('lê datas de CSV no padrão brasileiro (dia/mês)', async () => {
    const store = useUploadStore();
    const csv = [
      'codigo_cliente,nome_cliente,email,consultor,segmento,nivel_cliente,faturamento_anual,servicos_contratados,data_contratacao,cidade,uf',
      'CTI001,Gama Tech,contato@gama.com,Bruno Lima,Tecnologia,A,1200000,Cloud,05/07/2024,Curitiba,PR',
    ].join('\n');
    store.selecionarArquivo(new File([csv], 'carteira.csv', { type: 'text/csv' }));
    await store.processarPlanilha();

    expect(store.erros).toEqual([]);
    expect(store.linhasValidas[0].data_contratacao).toEqual(new Date(2024, 6, 5));
    expect(store.linhasValidas[0].faturamento_anual).toBe(1200000);
  });

  it('recusa arquivos de outro formato ou acima de 50 MB', () => {
    const store = useUploadStore();
    store.arquivo = { name: 'dados.pdf', size: 10 };
    expect(store.validarArquivo()).toBe(false);

    store.arquivo = { name: 'grande.xlsx', size: 51 * 1024 * 1024 };
    expect(store.validarArquivo()).toBe(false);
    expect(store.erroArquivo).toContain('50 MB');

    store.arquivo = { name: 'ok.csv', size: 1024 };
    expect(store.validarArquivo()).toBe(true);
  });
});
