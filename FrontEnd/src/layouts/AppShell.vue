<script setup>
import { RouterView } from 'vue-router';
import Sidebar from '../components/layout/Sidebar.vue';
import Topbar from '../components/layout/Topbar.vue';
</script>

<template>
  <div class="flex min-h-screen bg-mist text-navy">
    <Sidebar />
    <div class="flex min-w-0 flex-1 flex-col">
      <Topbar />
      <main class="mx-auto w-full max-w-[1280px] flex-1 p-6 lg:p-8">
        <RouterView />
      </main>
    </div>
  </div>
</template>
