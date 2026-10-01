import { defineStore } from 'pinia';
import { authService } from '../services/auth';


const TOKEN_KEY = 'di_token';
const USER_KEY = 'di_user';

function lerUsuario() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY)) || null;
  } catch {
    return null;
  }
}

function tokenValido(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return !payload.exp || payload.exp * 1000 > Date.now();
  } catch {
    return false;
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || null,
    usuario: lerUsuario(),
  }),

  getters: {
    logado: (state) => Boolean(state.token) && tokenValido(state.token),
    nomeExibicao: (state) => state.usuario?.nome || 'Usuário',
  },

  actions: {
    /** Lança o erro da API quando as credenciais são recusadas. */
    async login(email, senha) {
      if (!email || !senha) return false;

      const { token, user } = await authService.login(email, senha);
      this.token = token;
      this.usuario = user;
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(user));
      return true;
    },

    async cadastrar(nome, email, senha) {
      await authService.cadastrar(nome, email, senha);
      return this.login(email, senha);
    },

    atualizarUsuario(dados) {
      this.usuario = { ...this.usuario, ...dados };
      localStorage.setItem(USER_KEY, JSON.stringify(this.usuario));
    },

    logout() {
      this.token = null;
      this.usuario = null;
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    },
  },
});
