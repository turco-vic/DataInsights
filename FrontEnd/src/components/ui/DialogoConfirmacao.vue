<script setup>
import { nextTick, ref, watch } from 'vue';
import { useUiStore } from '../../stores/uiStore';

const ui = useUiStore();
const botaoConfirmar = ref(null);

watch(() => ui.dialogo, (aberto) => {
  if (aberto) nextTick(() => botaoConfirmar.value?.focus());
});
</script>

<template>
  <div
    v-if="ui.dialogo"
    class="fixed inset-0 z-50 flex items-center justify-center bg-navy/40 px-4 print:hidden"
    @click.self="ui.responder(false)" @keydown.esc="ui.responder(false)"
  >
    <div role="alertdialog" aria-modal="true" aria-labelledby="dialogo-titulo" aria-describedby="dialogo-mensagem"
      class="w-full max-w-md rounded-2xl bg-white px-6 py-5 shadow-xl">
      <h2 id="dialogo-titulo" class="text-[15px] font-bold text-navy">{{ ui.dialogo.titulo }}</h2>
      <p id="dialogo-mensagem" class="mt-2 text-sm leading-relaxed text-navy/70">{{ ui.dialogo.mensagem }}</p>
      <div class="mt-6 flex justify-end gap-3">
        <button type="button" @click="ui.responder(false)"
          class="rounded-lg px-4 py-2.5 text-sm font-semibold text-navy/70 transition hover:bg-navy/5 hover:text-navy">Cancelar</button>
        <button ref="botaoConfirmar" type="button" @click="ui.responder(true)"
          class="rounded-lg px-5 py-2.5 text-sm font-semibold text-white transition"
          :class="ui.dialogo.perigo ? 'bg-red-600 hover:bg-red-700' : 'bg-ocean hover:bg-navy'">
          {{ ui.dialogo.botao }}
        </button>
      </div>
    </div>
  </div>
</template>
