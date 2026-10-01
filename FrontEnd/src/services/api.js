import axios from 'axios';
import { useAuthStore } from '../stores/authStore';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const { token } = useAuthStore();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Token expirado ou inválido: encerra a sessão e volta para o login.
api.interceptors.response.use(
  (resposta) => resposta,
  (error) => {
    const rotaDeLogin = error.config?.url?.endsWith('/login');
    if (error.response?.status === 401 && !rotaDeLogin) {
      useAuthStore().logout();
      if (window.location.pathname !== '/login') {
        window.location.assign(`/login?redirect=${encodeURIComponent(window.location.pathname)}`);
      }
    }
    return Promise.reject(error);
  },
);

/** Monta uma mensagem legível a partir do erro devolvido pela API. */
export function mensagemErro(error, padrao = 'Não foi possível concluir a operação.') {
  if (!error.response) return 'Servidor indisponível: não foi possível falar com o back-end.';
  const { mensagem, campos } = error.response.data || {};
  if (campos) {
    const detalhes = Object.entries(campos).map(([campo, msg]) => `${campo}: ${msg}`).join('; ');
    return `${mensagem || padrao} (${detalhes})`;
  }
  return mensagem || padrao;
}

export default api;
