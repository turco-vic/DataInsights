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

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || null,
    usuario: lerUsuario(),
  }),

  getters: {
    logado: (state) => Boolean(state.token),
    nomeExibicao: (state) => state.usuario?.nome || 'Usuário',
  },

  actions: {
    async login(email, senha, nome) {
      if (!email || !senha) return false;

      const { token, user } = await authService.login(email, senha, nome);
      this.token = token;
      this.usuario = user;
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(user));
      return true;
    },

    logout() {
      this.token = null;
      this.usuario = null;
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    },
  },
});
