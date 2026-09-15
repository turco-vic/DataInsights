<script setup>
import { RouterLink } from 'vue-router';
import Icon from './Icon.vue';

defineProps({ texto: { type: String, default: 'Envie uma planilha para ver os dados aqui.' } });
</script>

<template>
  <div class="mt-6 rounded-2xl border-2 border-dashed border-sky/40 bg-white px-8 py-14 text-center">
    <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-sky/10 text-ocean">
      <Icon name="document" class="h-6 w-6" />
    </div>
    <h2 class="mt-5 text-xl font-bold tracking-tight text-navy">Nenhuma planilha enviada</h2>
    <p class="mx-auto mt-2 max-w-100 text-sm leading-relaxed text-navy/60">{{ texto }}</p>
    <RouterLink to="/app/upload" class="mt-6 inline-block rounded-lg bg-ocean px-7 py-3 text-sm font-semibold text-white transition hover:bg-navy">
      Enviar planilha
    </RouterLink>
  </div>
</template>
