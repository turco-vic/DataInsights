import { reactive, computed } from 'vue';

// Store de autenticação simples e reativo (sem dependências extras).
// Persistido em localStorage: o usuário loga uma vez e permanece entre reloads.
// Quando o backend Java existir, o token e o usuário virão da resposta real de login.

const TOKEN_KEY = 'di_token';
const USER_KEY = 'di_user';

function ler(chave) {
  try {
    const bruto = localStorage.getItem(chave);
    return bruto ? JSON.parse(bruto) : null;
  } catch {
    return null;
  }
}

const state = reactive({
  token: localStorage.getItem(TOKEN_KEY) || null,
  user: ler(USER_KEY),
});

export const auth = {
  state,
  isAuthenticated: computed(() => Boolean(state.token)),
  user: computed(() => state.user),

  setSession(token, user) {
    state.token = token;
    state.user = user;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },

  logout() {
    state.token = null;
    state.user = null;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
};
