<script setup>
import Icon from '../Icon.vue';
import { useUiStore } from '../../stores/uiStore';

const ui = useUiStore();

const TOM = {
  sucesso: 'border-emerald-600/25 bg-emerald-50 text-emerald-800',
  erro: 'border-red-600/25 bg-red-50 text-red-800',
  aviso: 'border-amber-500/30 bg-amber-50 text-amber-900',
};
</script>

<template>
  <div aria-live="polite" class="pointer-events-none fixed bottom-4 right-4 z-[60] flex w-[calc(100%-2rem)] max-w-sm flex-col gap-2 print:hidden">
    <TransitionGroup
      enter-from-class="translate-y-2 opacity-0" enter-active-class="transition duration-200"
      leave-to-class="opacity-0" leave-active-class="transition duration-150"
    >
      <div
        v-for="n in ui.notificacoes" :key="n.id" role="status"
        class="pointer-events-auto flex items-start gap-3 rounded-xl border px-4 py-3 text-sm font-medium shadow-lg"
        :class="TOM[n.tipo] || TOM.sucesso"
      >
        <Icon v-if="n.tipo === 'sucesso'" name="check" class="mt-0.5 h-4 w-4 flex-none" />
        <p class="flex-1 leading-snug">{{ n.mensagem }}</p>
        <button type="button" @click="ui.dispensar(n.id)" aria-label="Fechar notificação" class="flex-none opacity-50 hover:opacity-100">
          <Icon name="close" class="h-4 w-4" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>
