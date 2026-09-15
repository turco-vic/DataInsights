import { defineStore } from 'pinia';
import { authService } from '../services/auth';

// Store de login (aula 7): Pinia guarda a sessão e o localStorage faz ela
// sobreviver ao F5. Diferente do slide, guardamos token + usuário em vez de
// um booleano: o http.js já envia o token e o backend Java autentica por JWT.

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
  // STATE = dados guardados; começam lendo o localStorage.
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || null,
    usuario: lerUsuario(),
  }),

  // GETTERS = valores calculados a partir do state.
  getters: {
    logado: (state) => Boolean(state.token),
    nomeExibicao: (state) => state.usuario?.nome || 'Usuário',
  },

  // ACTIONS = ações que alteram o state.
  actions: {
    // Devolve false quando falta e-mail ou senha, como no slide.
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
