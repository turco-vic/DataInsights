import api from './api';

export function iniciais(nome) {
  return (nome || '').split(' ').filter(Boolean).slice(0, 2).map((p) => p[0]).join('').toUpperCase();
}

/** Converte a resposta de login/perfil da API no usuário guardado na sessão. */
export function paraUsuario(dados) {
  return {
    id: dados.id,
    tipo: (dados.tipo || 'USUARIO').toLowerCase(),
    papel: (dados.papel || 'leitor').toLowerCase(),
    nome: dados.nome,
    email: dados.email,
    inicial: iniciais(dados.nome),
    prefs: {
      tratarDados: dados.prefTratarDados ?? true,
      gerarDashboard: dados.prefGerarDashboard ?? true,
      resumoSemanal: dados.prefResumoSemanal ?? false,
    },
  };
}

export const authService = {
  async login(email, senha, { manterConectado = false, consultor = false } = {}) {
    const rota = consultor ? '/api/consultores/login' : '/api/usuarios/login';
    const { data } = await api.post(rota, { email, senha, manterConectado });
    return { token: data.token, user: paraUsuario(data), manterConectado: data.manterConectado };
  },

  async cadastrar(dados) {
    const { data } = await api.post('/api/auth/cadastro', dados);
    return data;
  },

  async renovar() {
    const { data } = await api.post('/api/auth/renovar');
    return data.token;
  },

  async perfil() {
    const { data } = await api.get('/api/me');
    return data;
  },

  async esqueciSenha(email) {
    await api.post('/api/auth/esqueci-senha', { email });
  },

  async redefinirSenha(token, novaSenha) {
    await api.post('/api/auth/redefinir-senha', { token, novaSenha });
  },
};
