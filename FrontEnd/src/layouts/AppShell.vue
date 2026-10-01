<script setup>
import { ref, watch } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import Sidebar from '../components/layout/Sidebar.vue';
import Topbar from '../components/layout/Topbar.vue';

const route = useRoute();
const menuAberto = ref(false);

watch(() => route.fullPath, () => {
  menuAberto.value = false;
});
</script>

<template>
  <div class="flex min-h-screen bg-mist text-navy print:block print:bg-white">
    <div
      v-if="menuAberto"
      class="fixed inset-0 z-30 bg-navy/40 lg:hidden"
      aria-hidden="true"
      @click="menuAberto = false"
    ></div>
    <Sidebar :aberto="menuAberto" @fechar="menuAberto = false" />
    <div class="flex min-w-0 flex-1 flex-col">
      <Topbar @abrir-menu="menuAberto = true" />
      <main class="mx-auto w-full max-w-[1280px] flex-1 p-4 sm:p-6 lg:p-8 print:max-w-none print:p-0">
        <RouterView />
      </main>
    </div>
  </div>
</template>
