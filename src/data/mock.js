export const kpis = [
    { label: 'FATURAMENTO', value: 'R$ 4,82 M', delta: '+12,4% vs. período anterior', tone: 'text-navy', deltaTone: 'text-green-700' },
    { label: 'TICKET MÉDIO', value: 'R$ 1.940', delta: '+3,1% vs. período anterior', tone: 'text-ocean', deltaTone: 'text-green-700' },
    { label: 'COLABORADORES', value: '312', delta: '+8 no mês', tone: 'text-navy', deltaTone: 'text-navy/55' },
    { label: 'MARGEM', value: '23,6%', delta: '-1,2 p.p. vs. período anterior', tone: 'text-navy', deltaTone: 'text-red-700' },
];

export const bars = [
    ['JAN', 46], ['FEV', 52], ['MAR', 61], ['ABR', 55], ['MAI', 68], ['JUN', 74],
    ['JUL', 66], ['AGO', 79], ['SET', 83], ['OUT', 76], ['NOV', 91], ['DEZ', 100],
].map(([mes, h], i) => ({ mes, h, color: i === 11 ? 'bg-navy' : i === 10 ? 'bg-ocean' : 'bg-navy/15' }));

export const linePoints = '0,96 52,84 104,88 156,66 208,58 260,62 312,40 364,30 416,34 468,18 520,12';
export const areaPoints = linePoints + ' 520,120 0,120';

export const unidades = [
    { nome: 'Comercial', pct: '46%', cor: 'bg-navy' },
    { nome: 'Serviços', pct: '30%', cor: 'bg-ocean' },
    { nome: 'Indústria', pct: '16%', cor: 'bg-sky' },
    { nome: 'Outros', pct: '8%', cor: 'bg-navy/15' },
];

export const heat = [0.15, 0.3, 0.55, 0.9, 0.7, 0.2, 0.1, 0.25, 0.4, 0.6, 1, 0.8, 0.3, 0.12,
    0.2, 0.35, 0.5, 0.75, 0.6, 0.25, 0.08, 0.3, 0.45, 0.7, 0.85, 0.95, 0.35, 0.15,
    0.1, 0.28, 0.42, 0.66, 0.58, 0.18, 0.06]
    .map((n, i) => ({ i, bg: `rgba(0,75,133,${(0.06 + n * 0.94).toFixed(2)})` }));

const ok = 'bg-green-700/10 text-green-700';
const info = 'bg-sky/15 text-ocean';
const bad = 'bg-red-700/10 text-red-700';
const off = 'bg-navy/10 text-navy/60';

export const clientes = [
    { nome: 'Metalúrgica Andrade', unidade: 'Indústria', receita: 'R$ 486.200', var: '+14,2%', up: true, status: 'Ativo', badge: ok },
    { nome: 'Grupo Vertes', unidade: 'Comercial', receita: 'R$ 412.780', var: '+8,6%', up: true, status: 'Ativo', badge: ok },
    { nome: 'Nordeste Log', unidade: 'Serviços', receita: 'R$ 355.140', var: '-2,3%', up: false, status: 'Em revisão', badge: info },
    { nome: 'Construtora Pilar', unidade: 'Indústria', receita: 'R$ 298.900', var: '+21,7%', up: true, status: 'Ativo', badge: ok },
    { nome: 'Alfa Distribuidora', unidade: 'Comercial', receita: 'R$ 241.360', var: '+1,9%', up: true, status: 'Ativo', badge: ok },
    { nome: 'Têxtil Rosário', unidade: 'Indústria', receita: 'R$ 187.020', var: '-6,8%', up: false, status: 'Inativo', badge: off },
];

export const arquivos = [
    { nome: 'Faturamento_2026_Q1.xlsx', autor: 'Ana Ribeiro', data: '18/08/2026 · 09:12', linhas: '14.820', status: 'Processado', badge: ok },
    { nome: 'Colaboradores_Agosto.xlsx', autor: 'Marcos Leal', data: '17/08/2026 · 16:40', linhas: '312', status: 'Processado', badge: ok },
    { nome: 'Custos_Operacionais.csv', autor: 'Ana Ribeiro', data: '16/08/2026 · 11:05', linhas: '8.417', status: 'Processando', badge: info },
    { nome: 'Contratos_2025.xls', autor: 'Júlia Nunes', data: '14/08/2026 · 08:52', linhas: '1.204', status: 'Processado', badge: ok },
    { nome: 'Vendas_Regiao_Sul.xlsx', autor: 'Pedro Sato', data: '12/08/2026 · 15:31', linhas: '—', status: 'Com erros', badge: bad },
];

export const colunas = [
    { nome: 'data_emissao', tipo: 'Data', preench: '100%', w: '100%', exemplo: '2026-03-14' },
    { nome: 'cliente', tipo: 'Texto', preench: '99,8%', w: '99.8%', exemplo: 'Grupo Vertes' },
    { nome: 'unidade', tipo: 'Categoria', preench: '100%', w: '100%', exemplo: 'Comercial' },
    { nome: 'valor_bruto', tipo: 'Moeda', preench: '100%', w: '100%', exemplo: 'R$ 12.480,00' },
    { nome: 'desconto', tipo: 'Moeda', preench: '87,4%', w: '87.4%', exemplo: 'R$ 320,00' },
    { nome: 'colaborador_resp', tipo: 'Texto', preench: '94,1%', w: '94.1%', exemplo: 'M. Leal' },
];

export const usuarios = [
    { nome: 'Ana Ribeiro', email: 'ana.ribeiro@empresa.com', papel: 'Administrador', badge: 'bg-navy text-white', acesso: 'Hoje · 09:10', inicial: 'AR' },
    { nome: 'Marcos Leal', email: 'marcos.leal@empresa.com', papel: 'Analista', badge: 'bg-sky/15 text-ocean', acesso: 'Hoje · 08:02', inicial: 'ML' },
    { nome: 'Júlia Nunes', email: 'julia.nunes@empresa.com', papel: 'Analista', badge: 'bg-sky/15 text-ocean', acesso: 'Ontem · 17:45', inicial: 'JN' },
    { nome: 'Pedro Sato', email: 'pedro.sato@empresa.com', papel: 'Leitor', badge: 'bg-navy/10 text-navy/65', acesso: '12/08/2026', inicial: 'PS' },
    { nome: 'Carla Menezes', email: 'carla.menezes@empresa.com', papel: 'Leitor', badge: 'bg-navy/10 text-navy/65', acesso: '02/08/2026', inicial: 'CM' },
];
