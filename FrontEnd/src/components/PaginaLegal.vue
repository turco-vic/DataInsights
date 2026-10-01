<script setup>
import Header from './Header.vue';
import Footer from './Footer.vue';

defineProps({
  titulo: { type: String, required: true },
  atualizadoEm: { type: String, required: true },
});
</script>

<template>
  <div class="min-h-screen bg-mist text-navy">
    <Header />
    <main class="mx-auto max-w-[760px] px-6 py-14 sm:px-8">
      <h1 class="text-3xl font-bold tracking-tight text-navy sm:text-4xl">{{ titulo }}</h1>
      <p class="mt-2 text-sm text-navy/55">Última atualização: {{ atualizadoEm }}</p>
      <div class="legal mt-8 text-[15px] leading-relaxed text-navy/75">
        <slot />
      </div>
    </main>
    <Footer />
  </div>
</template>

<style scoped>
.legal :deep(h2) {
  margin-top: 2rem;
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-navy);
}
.legal :deep(p),
.legal :deep(ul) {
  margin-top: 0.75rem;
}
.legal :deep(ul) {
  list-style: disc;
  padding-left: 1.25rem;
}
.legal :deep(li) {
  margin-top: 0.35rem;
}
</style>
