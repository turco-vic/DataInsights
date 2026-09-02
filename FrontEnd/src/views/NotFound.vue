<script setup>
import { RouterLink } from 'vue-router';
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-mist px-6 py-14 text-center text-navy">
    <div class="max-w-[480px]">
      <div class="text-[92px] font-bold leading-none tracking-tight text-navy">404</div>
      <div class="mx-auto mt-5 h-1 w-16 rounded-full bg-sky"></div>
      <h1 class="mt-6 text-[26px] font-bold tracking-tight text-navy">Página não encontrada</h1>
      <p class="mt-3 text-base leading-relaxed text-navy/60">O endereço não existe ou a planilha vinculada a ele foi removida.</p>
      <div class="mt-8 flex flex-wrap items-center justify-center gap-3.5">
        <RouterLink to="/app"
          class="rounded-lg bg-ocean px-7 py-3 text-sm font-semibold text-white transition hover:bg-navy">Ir para o dashboard</RouterLink>
        <RouterLink to="/"
          class="border-b border-navy/25 pb-0.5 text-sm font-medium text-navy transition hover:border-ocean hover:text-ocean">Voltar ao site</RouterLink>
      </div>
    </div>
  </div>
</template>
