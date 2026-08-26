<script setup>
import { computed } from 'vue';
import { RouterLink } from 'vue-router';
import Icon from '../Icon.vue';
import { auth } from '../../stores/auth';

const PAPEL = { admin: 'Administrador', analista: 'Analista', leitor: 'Leitor' };

const user = computed(() => auth.user.value || {});
const papelLabel = computed(() => PAPEL[user.value.papel] || 'Usuário');
</script>

<template>
  <header
    class="sticky top-0 z-10 flex items-center gap-6 border-b border-navy/10 bg-mist/85 px-6 py-3 backdrop-blur lg:px-8">
    <label class="flex max-w-md flex-1 items-center gap-2.5 rounded-lg border border-navy/15 bg-white px-3.5 py-2.5">
      <Icon name="search" class="h-4 w-4 flex-none text-navy/40" />
      <input
        type="search" placeholder="Buscar planilhas, colunas ou clientes"
        class="min-w-0 flex-1 border-none bg-transparent text-sm text-navy outline-none placeholder:text-navy/40" />
    </label>

    <div class="ml-auto flex items-center gap-5">
      <RouterLink
        to="/app/upload"
        class="hidden items-center gap-2 rounded-lg bg-ocean px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-navy sm:inline-flex">
        <Icon name="plus" class="h-4 w-4" />
        Nova planilha
      </RouterLink>

      <div class="flex items-center gap-2.5">
        <div class="flex h-9 w-9 items-center justify-center rounded-full bg-navy text-xs font-bold text-white">
          {{ user.inicial || '—' }}
        </div>
        <div class="hidden leading-tight sm:block">
          <div class="text-[13px] font-semibold text-navy">{{ user.nome || 'Convidado' }}</div>
          <div class="text-[11.5px] text-navy/50">{{ papelLabel }}</div>
        </div>
      </div>
    </div>
  </header>
</template>
