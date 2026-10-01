import { defineStore } from 'pinia';
import { authService, iniciais, paraUsuario } from '../services/auth';
import { usePlanilhasStore } from './planilhasStore';
import { useUploadStore } from './uploadStore';

const TOKEN_KEY = 'di_token';
const USER_KEY = 'di_user';
const ANTECEDENCIA_RENOVACAO_MS = 5 * 60 * 1000;
const MAIOR_ESPERA_MS = 2 ** 31 - 1;

let timerRenovacao = null;

// "Manter conectado" guarda a sessão no localStorage; sem ele, no sessionStorage (some ao fechar o navegador).
const ler = (chave) => localStorage.getItem(chave) ?? sessionStorage.getItem(chave);

function lerUsuario() {
  try {
    return JSON.parse(ler(USER_KEY)) || null;
  } catch {
    return null;
  }
}

export function lerPayload(token) {
  try {
    return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
  } catch {
    return null;
  }
}

export function tokenValido(token) {
  const payload = token ? lerPayload(token) : null;
  return Boolean(payload) && (!payload.exp || payload.exp * 1000 > Date.now());
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: ler(TOKEN_KEY),
    usuario: lerUsuario(),
    manterConectado: Boolean(localStorage.getItem(TOKEN_KEY)),
  }),

  getters: {
    logado: (state) => tokenValido(state.token),
    nomeExibicao: (state) => state.usuario?.nome || 'Usuário',
    papel: (state) => state.usuario?.papel || 'leitor',
    ehAdmin() {
      return this.papel === 'admin';
    },
    /** Administradores e analistas alteram dados; leitores e consultores só consultam. */
    podeEditar() {
      return this.papel === 'admin' || this.papel === 'analista';
    },
    ehConsultor: (state) => state.usuario?.tipo === 'consultor',
  },

  actions: {
    /** Lança o erro da API quando as credenciais são recusadas. */
    async login(email, senha, opcoes = {}) {
      if (!email || !senha) return false;
      const { token, user, manterConectado } = await authService.login(email, senha, opcoes);
      this.salvarSessao(token, user, manterConectado);
      usePlanilhasStore().limpar();
      return true;
    },

    async cadastrar({ nome, email, senha, empresa }) {
      await authService.cadastrar({ nome, email, senha, empresa: empresa || null });
      return this.login(email, senha);
    },

    salvarSessao(token, usuario, manterConectado) {
      this.token = token;
      this.usuario = usuario;
      this.manterConectado = manterConectado;
      this.limparArmazenamento();
      const destino = manterConectado ? localStorage : sessionStorage;
      destino.setItem(TOKEN_KEY, token);
      destino.setItem(USER_KEY, JSON.stringify(usuario));
      this.agendarRenovacao();
    },

    /** Atualiza nome, papel e preferências com o que está no servidor. */
    async carregarPerfil() {
      if (!this.logado) return;
      try {
        this.atualizarUsuario(paraUsuario(await authService.perfil()));
      } catch {
        // 401 já encerra a sessão no interceptor; outros erros mantêm os dados atuais.
      }
    },

    atualizarUsuario(dados) {
      this.usuario = { ...this.usuario, ...dados };
      this.usuario.inicial = iniciais(this.usuario.nome);
      const destino = this.manterConectado ? localStorage : sessionStorage;
      destino.setItem(USER_KEY, JSON.stringify(this.usuario));
    },

    /** Renova o token alguns minutos antes de ele expirar, enquanto a aba estiver aberta. */
    agendarRenovacao() {
      clearTimeout(timerRenovacao);
      const payload = this.token ? lerPayload(this.token) : null;
      if (!payload?.exp) return;
      const espera = payload.exp * 1000 - Date.now() - ANTECEDENCIA_RENOVACAO_MS;
      timerRenovacao = setTimeout(() => this.renovar(), Math.min(Math.max(espera, 0), MAIOR_ESPERA_MS));
    },

    async renovar() {
      if (!this.logado) return;
      try {
        this.token = await authService.renovar();
        (this.manterConectado ? localStorage : sessionStorage).setItem(TOKEN_KEY, this.token);
        this.agendarRenovacao();
      } catch {
        // Se a sessão já não vale, o interceptor encerra e leva ao login.
      }
    },

    limparArmazenamento() {
      [localStorage, sessionStorage].forEach((s) => {
        s.removeItem(TOKEN_KEY);
        s.removeItem(USER_KEY);
      });
    },

    /** Encerra a sessão e tira da tela os dados carregados (planilhas e upload pendente). */
    logout() {
      clearTimeout(timerRenovacao);
      this.token = null;
      this.usuario = null;
      this.limparArmazenamento();
      usePlanilhasStore().limpar();
      useUploadStore().limpar();
    },
  },
});
