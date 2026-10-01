import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { lerPayload, tokenValido, useAuthStore } from './authStore';

const b64 = (obj) => btoa(JSON.stringify(obj)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_');
const token = (exp) => `${b64({ alg: 'HS256' })}.${b64({ sub: 'a@a.com', exp })}.assinatura`;
const agora = () => Math.floor(Date.now() / 1000);
const usuario = { id: 1, tipo: 'usuario', papel: 'analista', nome: 'Ana Souza', email: 'a@a.com', inicial: 'AS', prefs: {} };

describe('authStore', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    setActivePinia(createPinia());
  });

  it('lê o payload e confere a validade do token', () => {
    expect(lerPayload(token(123)).exp).toBe(123);
    expect(tokenValido(token(agora() + 60))).toBe(true);
    expect(tokenValido(token(agora() - 60))).toBe(false);
    expect(tokenValido('local.123')).toBe(false);
    expect(tokenValido(null)).toBe(false);
  });

  it('"Manter conectado" guarda a sessão no localStorage; sem ele, no sessionStorage', () => {
    const auth = useAuthStore();
    auth.salvarSessao(token(agora() + 3600), usuario, false);
    expect(sessionStorage.getItem('di_token')).toBeTruthy();
    expect(localStorage.getItem('di_token')).toBeNull();

    auth.salvarSessao(token(agora() + 3600), usuario, true);
    expect(localStorage.getItem('di_token')).toBeTruthy();
    expect(sessionStorage.getItem('di_token')).toBeNull();
  });

  it('expõe as permissões do papel e limpa tudo ao sair', () => {
    const auth = useAuthStore();
    auth.salvarSessao(token(agora() + 3600), usuario, true);
    expect(auth.logado).toBe(true);
    expect(auth.podeEditar).toBe(true);
    expect(auth.ehAdmin).toBe(false);

    auth.atualizarUsuario({ papel: 'leitor', nome: 'Ana Maria Souza' });
    expect(auth.podeEditar).toBe(false);
    expect(auth.usuario.inicial).toBe('AM');

    auth.logout();
    expect(auth.logado).toBe(false);
    expect(localStorage.getItem('di_token')).toBeNull();
    expect(localStorage.getItem('di_user')).toBeNull();
  });
});
