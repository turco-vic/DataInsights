<script setup>
import { RouterLink } from 'vue-router';
import AppLogo from './AppLogo.vue';

const links = [
  { t: 'Sobre', href: '#sobre' },
  { t: 'Privacidade', href: '#privacidade' },
  { t: 'Termos de uso', href: '#termos' },
  { t: 'Contato', href: '#contato' },
];
</script>

<template>
  <footer class="border-t border-navy/10 bg-white">
    <div class="mx-auto flex max-w-[1180px] flex-wrap items-center justify-between gap-7 px-8 py-10">
      <div class="flex flex-wrap items-center gap-4">
        <RouterLink to="/"><AppLogo size="text-lg" /></RouterLink>
        <span class="text-sm text-navy/55">© 2026 DataInsights · Todos os direitos reservados.</span>
      </div>
      <nav class="flex flex-wrap items-center gap-6">
        <a v-for="l in links" :key="l.href" :href="l.href"
          class="text-sm font-medium text-navy/65 hover:text-ocean">{{ l.t }}</a>
      </nav>
    </div>
  </footer>
</template>
