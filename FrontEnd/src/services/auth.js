// import { http } from './http';

function nomeDoEmail(email) {
  return email
    .split('@')[0]
    .split(/[._-]+/)
    .filter(Boolean)
    .map((p) => p.charAt(0).toUpperCase() + p.slice(1))
    .join(' ');
}

function iniciais(nome) {
  return nome.split(' ').filter(Boolean).slice(0, 2).map((p) => p[0]).join('').toUpperCase();
}

export const authService = {
  // Sem backend: qualquer e-mail/senha preenchidos autentica. O usuário é
  // montado a partir do que foi digitado — nenhum dado fictício.
  async login(email, senha, nome) {
    const nomeFinal = (nome || '').trim() || nomeDoEmail(email);
    const user = { nome: nomeFinal, email, papel: 'admin', inicial: iniciais(nomeFinal) };
    return { token: `local.${Date.now()}`, user };

    // Backend Java:
    // return http.post('/usuarios/login', { email, senha });
  },
};
