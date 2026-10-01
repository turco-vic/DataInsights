<script setup>
import { computed } from 'vue';
import { RouterLink } from 'vue-router';
import Icon from '../Icon.vue';
import BuscaGlobal from './BuscaGlobal.vue';
import { useAuthStore } from '../../stores/authStore';
import { PAPEIS } from '../../utils/dominio';

defineEmits(['abrir-menu']);

const auth = useAuthStore();
const user = computed(() => auth.usuario || {});
const papelLabel = computed(() => PAPEIS[auth.papel]?.rotulo || 'Usuário');
</script>

<template>
  <header
    class="sticky top-0 z-20 flex items-center gap-3 border-b border-navy/10 bg-mist/85 px-4 py-3 backdrop-blur sm:gap-6 sm:px-6 lg:px-8 print:hidden">
    <button
      type="button" @click="$emit('abrir-menu')"
      aria-label="Abrir menu" aria-controls="menu-lateral"
      class="flex-none rounded-lg p-2 text-navy/70 transition hover:bg-navy/5 hover:text-navy lg:hidden"
    >
      <Icon name="menu" class="h-5 w-5" />
    </button>
    <BuscaGlobal />

    <div class="ml-auto flex items-center gap-5">
      <RouterLink
        v-if="auth.podeEditar"
        to="/app/upload"
        class="hidden items-center gap-2 rounded-lg bg-ocean px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-navy sm:inline-flex">
        <Icon name="plus" class="h-4 w-4" />
        Nova planilha
      </RouterLink>

      <RouterLink to="/app/configuracoes" class="flex items-center gap-2.5 rounded-lg" title="Configurações da conta">
        <div class="flex h-9 w-9 items-center justify-center rounded-full bg-navy text-xs font-bold text-white">
          {{ user.inicial || '—' }}
        </div>
        <div class="hidden leading-tight sm:block">
          <div class="text-[13px] font-semibold text-navy">{{ user.nome || 'Convidado' }}</div>
          <div class="text-[11.5px] text-navy/50">{{ papelLabel }}</div>
        </div>
      </RouterLink>
    </div>
  </header>
</template>
