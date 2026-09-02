// Dados de exemplo (fixtures) — DTOs puros, sem nenhuma classe de apresentação.
// Servem para o app rodar 100% offline enquanto o backend Java não existe.
// Cada serviço em src/services/* consome estes dados; quando o backend entrar,
// basta trocar a implementação do serviço por uma chamada HTTP real.
//
// Formato dos enums:
//   trend  : 'up' | 'down' | 'neutral'   (variação de um KPI)
//   status : 'ativo' | 'revisao' | 'inativo' | 'processado' | 'processando' | 'erro'
//   papel  : 'admin' | 'analista' | 'leitor'

export const usuarioAtual = {
  nome: 'Ana Ribeiro',
  email: 'ana.ribeiro@empresa.com',
  papel: 'admin',
  cargo: 'Controladoria',
  fuso: 'America/Sao_Paulo (GMT-3)',
  inicial: 'AR',
};

export const kpis = [
  { label: 'Faturamento', value: 'R$ 4,82 M', delta: '+12,4% vs. período anterior', trend: 'up' },
  { label: 'Ticket médio', value: 'R$ 1.940', delta: '+3,1% vs. período anterior', trend: 'up' },
  { label: 'Colaboradores', value: '312', delta: '+8 no mês', trend: 'neutral' },
  { label: 'Margem', value: '23,6%', delta: '-1,2 p.p. vs. período anterior', trend: 'down' },
];

// Altura relativa (0–100) de cada mês; a cor de destaque é decidida na view.
export const bars = [
  { mes: 'JAN', valor: 46 }, { mes: 'FEV', valor: 52 }, { mes: 'MAR', valor: 61 },
  { mes: 'ABR', valor: 55 }, { mes: 'MAI', valor: 68 }, { mes: 'JUN', valor: 74 },
  { mes: 'JUL', valor: 66 }, { mes: 'AGO', valor: 79 }, { mes: 'SET', valor: 83 },
  { mes: 'OUT', valor: 76 }, { mes: 'NOV', valor: 91 }, { mes: 'DEZ', valor: 100 },
];

// Geometria da linha de tendência (coordenadas do SVG 520x120) — não é apresentação.
export const linePoints = '0,96 52,84 104,88 156,66 208,58 260,62 312,40 364,30 416,34 468,18 520,12';
export const areaPoints = `${linePoints} 520,120 0,120`;

export const unidades = [
  { nome: 'Comercial', pct: 46 },
  { nome: 'Serviços', pct: 30 },
  { nome: 'Indústria', pct: 16 },
  { nome: 'Outros', pct: 8 },
];

// Intensidades 0–1 do mapa de calor (7 colunas x 5 semanas). A cor é calculada na view.
export const heat = [
  0.15, 0.30, 0.55, 0.90, 0.70, 0.20, 0.10,
  0.25, 0.40, 0.60, 1.00, 0.80, 0.30, 0.12,
  0.20, 0.35, 0.50, 0.75, 0.60, 0.25, 0.08,
  0.30, 0.45, 0.70, 0.85, 0.95, 0.35, 0.15,
  0.10, 0.28, 0.42, 0.66, 0.58, 0.18, 0.06,
];

export const clientes = [
  { nome: 'Metalúrgica Andrade', unidade: 'Indústria', receita: 'R$ 486.200', variacao: '+14,2%', up: true, status: 'ativo' },
  { nome: 'Grupo Vertes', unidade: 'Comercial', receita: 'R$ 412.780', variacao: '+8,6%', up: true, status: 'ativo' },
  { nome: 'Nordeste Log', unidade: 'Serviços', receita: 'R$ 355.140', variacao: '-2,3%', up: false, status: 'revisao' },
  { nome: 'Construtora Pilar', unidade: 'Indústria', receita: 'R$ 298.900', variacao: '+21,7%', up: true, status: 'ativo' },
  { nome: 'Alfa Distribuidora', unidade: 'Comercial', receita: 'R$ 241.360', variacao: '+1,9%', up: true, status: 'ativo' },
  { nome: 'Têxtil Rosário', unidade: 'Indústria', receita: 'R$ 187.020', variacao: '-6,8%', up: false, status: 'inativo' },
];

export const arquivos = [
  { nome: 'Faturamento_2026_Q1.xlsx', autor: 'Ana Ribeiro', data: '18/08/2026 · 09:12', linhas: '14.820', status: 'processado' },
  { nome: 'Colaboradores_Agosto.xlsx', autor: 'Marcos Leal', data: '17/08/2026 · 16:40', linhas: '312', status: 'processado' },
  { nome: 'Custos_Operacionais.csv', autor: 'Ana Ribeiro', data: '16/08/2026 · 11:05', linhas: '8.417', status: 'processando' },
  { nome: 'Contratos_2025.xls', autor: 'Júlia Nunes', data: '14/08/2026 · 08:52', linhas: '1.204', status: 'processado' },
  { nome: 'Vendas_Regiao_Sul.xlsx', autor: 'Pedro Sato', data: '12/08/2026 · 15:31', linhas: '—', status: 'erro' },
];

export const colunas = [
  { nome: 'data_emissao', tipo: 'Data', preench: 100, exemplo: '2026-03-14' },
  { nome: 'cliente', tipo: 'Texto', preench: 99.8, exemplo: 'Grupo Vertes' },
  { nome: 'unidade', tipo: 'Categoria', preench: 100, exemplo: 'Comercial' },
  { nome: 'valor_bruto', tipo: 'Moeda', preench: 100, exemplo: 'R$ 12.480,00' },
  { nome: 'desconto', tipo: 'Moeda', preench: 87.4, exemplo: 'R$ 320,00' },
  { nome: 'colaborador_resp', tipo: 'Texto', preench: 94.1, exemplo: 'M. Leal' },
];

export const qualidade = [
  { label: 'Completude', pct: 98.4 },
  { label: 'Consistência de tipos', pct: 96.1 },
  { label: 'Registros únicos', pct: 99.9 },
];

export const insights = [
  'Novembro e dezembro concentram 21% do faturamento do período.',
  'A unidade Comercial cresce 14% enquanto Indústria recua 3%.',
  '12,6% dos registros não têm desconto preenchido, o que pode distorcer a margem.',
];

export const usuarios = [
  { nome: 'Ana Ribeiro', email: 'ana.ribeiro@empresa.com', papel: 'admin', acesso: 'Hoje · 09:10', inicial: 'AR' },
  { nome: 'Marcos Leal', email: 'marcos.leal@empresa.com', papel: 'analista', acesso: 'Hoje · 08:02', inicial: 'ML' },
  { nome: 'Júlia Nunes', email: 'julia.nunes@empresa.com', papel: 'analista', acesso: 'Ontem · 17:45', inicial: 'JN' },
  { nome: 'Pedro Sato', email: 'pedro.sato@empresa.com', papel: 'leitor', acesso: '12/08/2026', inicial: 'PS' },
  { nome: 'Carla Menezes', email: 'carla.menezes@empresa.com', papel: 'leitor', acesso: '02/08/2026', inicial: 'CM' },
];

export const uploadResumo = {
  arquivo: 'Faturamento_2026_Q1.xlsx',
  linhas: '14.820',
  colunas: 12,
  tempo: '11s',
  metrica: [
    { label: 'Linhas válidas', valor: '14.796' },
    { label: 'Duplicadas', valor: '18' },
    { label: 'Campos vazios', valor: '6' },
  ],
};
