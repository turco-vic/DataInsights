<script setup>
import { nextTick, ref, watch } from 'vue';
import Icon from '../Icon.vue';

const props = defineProps({
  aberto: { type: Boolean, default: false },
  titulo: { type: String, required: true },
  largura: { type: String, default: 'max-w-lg' },
});
const emit = defineEmits(['fechar']);

const painel = ref(null);

// Ao abrir, leva o foco para o primeiro campo do formulário.
watch(() => props.aberto, (aberto) => {
  if (!aberto) return;
  nextTick(() => painel.value?.querySelector('input, select, textarea, button:not([aria-label="Fechar"])')?.focus());
});
</script>

<template>
  <div
    v-if="aberto"
    class="fixed inset-0 z-50 flex items-end justify-center bg-navy/40 px-0 sm:items-center sm:px-4 print:hidden"
    @click.self="emit('fechar')" @keydown.esc="emit('fechar')"
  >
    <div
      ref="painel" role="dialog" aria-modal="true" :aria-label="titulo"
      class="flex max-h-[92vh] w-full flex-col overflow-hidden rounded-t-2xl bg-white shadow-xl sm:rounded-2xl"
      :class="largura"
    >
      <div class="flex items-center justify-between gap-4 border-b border-navy/10 px-6 py-4">
        <h2 class="text-[15px] font-bold text-navy">{{ titulo }}</h2>
        <button type="button" aria-label="Fechar" class="rounded-lg p-1 text-navy/40 hover:bg-navy/5 hover:text-navy" @click="emit('fechar')">
          <Icon name="close" class="h-5 w-5" />
        </button>
      </div>
      <div class="overflow-y-auto px-6 py-5">
        <slot />
      </div>
    </div>
  </div>
</template>
