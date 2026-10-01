import { defineStore } from 'pinia';

let proximoId = 1;
let resolverDialogo = null;

export const useUiStore = defineStore('ui', {
  state: () => ({
    notificacoes: [],
    dialogo: null,
  }),

  actions: {
    /** Mostra uma notificação passageira. tipo: 'sucesso' | 'erro' | 'aviso'. */
    notificar(mensagem, tipo = 'sucesso', duracao = 4000) {
      const id = proximoId++;
      this.notificacoes.push({ id, mensagem, tipo });
      setTimeout(() => this.dispensar(id), duracao);
    },

    dispensar(id) {
      this.notificacoes = this.notificacoes.filter((n) => n.id !== id);
    },

    /** Abre o diálogo de confirmação e resolve com true (confirmou) ou false (cancelou). */
    confirmar({ titulo, mensagem, botao = 'Confirmar', perigo = false }) {
      if (resolverDialogo) resolverDialogo(false);
      this.dialogo = { titulo, mensagem, botao, perigo };
      return new Promise((resolve) => {
        resolverDialogo = resolve;
      });
    },

    responder(confirmou) {
      this.dialogo = null;
      resolverDialogo?.(confirmou);
      resolverDialogo = null;
    },
  },
});
