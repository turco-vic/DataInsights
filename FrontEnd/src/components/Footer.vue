<script setup>
import { RouterLink } from 'vue-router';
import AppLogo from './AppLogo.vue';

const contato = import.meta.env.VITE_CONTATO_EMAIL;

const links = [
  { t: 'Sobre', to: '/#sobre' },
  { t: 'Privacidade', to: '/privacidade' },
  { t: 'Termos de uso', to: '/termos' },
];
</script>

<template>
  <footer class="border-t border-navy/10 bg-white">
    <div class="mx-auto flex max-w-[1180px] flex-wrap items-center justify-between gap-7 px-8 py-10">
      <div class="flex flex-wrap items-center gap-4">
        <RouterLink to="/"><AppLogo size="text-lg" /></RouterLink>
        <span class="text-sm text-navy/55">© 2026 DataInsights · Todos os direitos reservados.</span>
      </div>
      <nav class="flex flex-wrap items-center gap-6" aria-label="Rodapé">
        <RouterLink v-for="l in links" :key="l.to" :to="l.to"
          class="text-sm font-medium text-navy/65 hover:text-ocean">{{ l.t }}</RouterLink>
        <a v-if="contato" :href="`mailto:${contato}`" class="text-sm font-medium text-navy/65 hover:text-ocean">Contato</a>
      </nav>
    </div>
  </footer>
</template>
