import api from './api';

function iniciais(nome) {
  return nome.split(' ').filter(Boolean).slice(0, 2).map((p) => p[0]).join('').toUpperCase();
}

export const authService = {
  async login(email, senha) {
    const { data } = await api.post('/api/usuarios/login', { email, senha });
    const user = { id: data.id, nome: data.nome, email: data.email, papel: 'admin', inicial: iniciais(data.nome) };
    return { token: data.token, user };
  },

  async cadastrar(nome, email, senha) {
    await api.post('/api/usuarios', { nome, email, senha });
  },
};
