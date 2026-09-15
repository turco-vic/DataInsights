import { useAuthStore } from '../stores/authStore';

// Wrapper fino sobre fetch. Ainda não é usado (não há backend), mas está pronto:
// quando o backend Java entrar, cada serviço passa a chamar http.get/post/... daqui.
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

async function request(caminho, { method = 'GET', body, headers } = {}) {
  const { token } = useAuthStore(); // dentro da função, depois do app.use(pinia)
  const opcoes = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
  };
  if (body !== undefined) opcoes.body = JSON.stringify(body);

  const resposta = await fetch(`${BASE_URL}${caminho}`, opcoes);
  if (!resposta.ok) {
    throw new Error(`Falha na requisição (${resposta.status})`);
  }
  return resposta.status === 204 ? null : resposta.json();
}

export const http = {
  get: (caminho, opcoes) => request(caminho, { ...opcoes, method: 'GET' }),
  post: (caminho, body, opcoes) => request(caminho, { ...opcoes, method: 'POST', body }),
  put: (caminho, body, opcoes) => request(caminho, { ...opcoes, method: 'PUT', body }),
  del: (caminho, opcoes) => request(caminho, { ...opcoes, method: 'DELETE' }),
};
