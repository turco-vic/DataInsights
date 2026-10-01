// Valores aceitos pelo back-end para os campos de cliente.

export const SEGMENTOS = ['Indústria', 'Comércio', 'Serviços', 'Saúde', 'Educação', 'Tecnologia'];

export const NIVEIS = ['A', 'B', 'C'];

export const UFS = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
];

export const PAPEIS = {
  admin: { rotulo: 'Administrador', descricao: 'Gerencia usuários, integrações e todas as planilhas da empresa.', cor: 'text-navy' },
  analista: { rotulo: 'Analista', descricao: 'Envia planilhas e mantém clientes, contratos, serviços e insights.', cor: 'text-ocean' },
  leitor: { rotulo: 'Leitor', descricao: 'Visualiza dashboards, clientes e relatórios, sem editar dados.', cor: 'text-sky' },
  consultor: { rotulo: 'Consultor', descricao: 'Consulta a carteira de clientes, sem editar dados.', cor: 'text-sky' },
};

export const EVENTOS = {
  login: 'Login',
  login_falhou: 'Login recusado',
  cadastro: 'Cadastro',
  importacao: 'Importação',
  planilha_validada: 'Planilha validada',
  cliente_excluido: 'Cliente excluído',
  usuario_criado: 'Usuário criado',
  usuario_excluido: 'Usuário excluído',
  senha_alterada: 'Senha alterada',
  senha_redefinida: 'Senha redefinida',
  insights_gerados: 'Insights gerados',
};

