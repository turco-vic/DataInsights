import { usuarioAtual } from './mock';
// import { http } from './http';

function iniciais(nome) {
  return nome.split(' ').filter(Boolean).slice(0, 2).map((p) => p[0]).join('').toUpperCase();
}

export const authService = {
  // Mock: qualquer e-mail/senha autentica. Retorna token + usuário.
  async login(email /*, senha */) {
    const nome = usuarioAtual.nome;
    const user = { ...usuarioAtual, email: email || usuarioAtual.email, inicial: iniciais(nome) };
    return { token: `mock.${Date.now()}`, user };

    // Backend Java:
    // return http.post('/auth/login', { email, senha });
  },
};
