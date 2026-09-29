
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
  async login(email, senha, nome) {
    const nomeFinal = (nome || '').trim() || nomeDoEmail(email);
    const user = { nome: nomeFinal, email, papel: 'admin', inicial: iniciais(nomeFinal) };
    return { token: `local.${Date.now()}`, user };

  },
};
