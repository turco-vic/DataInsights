<script setup>
import { RouterLink } from 'vue-router';
import AppLogo from './AppLogo.vue';
</script>

<template>
  <header class="sticky top-0 z-20 border-b border-navy/10 bg-mist/85 backdrop-blur">
    <div class="mx-auto flex max-w-[1180px] items-center justify-between gap-6 px-8 py-4">
      <RouterLink to="/"><AppLogo /></RouterLink>
      <nav class="flex items-center gap-8">
        <a href="#inicio" class="hidden text-[15px] font-medium text-navy/70 hover:text-ocean sm:block">Início</a>
        <a href="#sobre" class="hidden text-[15px] font-medium text-navy/70 hover:text-ocean sm:block">Sobre</a>
        <RouterLink
          to="/login"
          class="rounded-lg bg-navy px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-ocean">
          Entrar
        </RouterLink>
      </nav>
    </div>
  </header>
</template>
