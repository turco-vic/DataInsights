<script setup>
import { RouterLink, useRoute, useRouter } from 'vue-router';
import AppLogo from '../AppLogo.vue';
import Icon from '../Icon.vue';
import { useAuthStore } from '../../stores/authStore';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const nav = [
  { to: '/app', label: 'Dashboard', icon: 'dashboard' },
  { to: '/app/upload', label: 'Upload de planilha', icon: 'upload' },
  { to: '/app/planilhas', label: 'Planilhas enviadas', icon: 'files' },
  { to: '/app/dataset', label: 'Relatório do dataset', icon: 'dataset' },
  { to: '/app/usuarios', label: 'Usuários', icon: 'users' },
  { to: '/app/configuracoes', label: 'Configurações', icon: 'settings' },
];

const ativo = (to) => route.path === to;

function sair() {
  auth.logout();
  router.push('/');
}
</script>

<template>
  <aside class="sticky top-0 flex h-screen w-64 flex-none flex-col justify-between border-r border-navy/10 bg-white py-6">
    <div>
      <RouterLink to="/" class="block px-6 pb-8">
        <AppLogo />
      </RouterLink>

      <div class="mb-2 px-6 text-[11px] font-bold uppercase tracking-[0.12em] text-navy/40">Navegação</div>
      <nav class="flex flex-col gap-1 px-3">
        <RouterLink
          v-for="n in nav" :key="n.to" :to="n.to"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition"
          :class="ativo(n.to)
            ? 'bg-ocean/10 font-semibold text-ocean'
            : 'font-medium text-navy/65 hover:bg-navy/5 hover:text-navy'"
        >
          <Icon :name="n.icon" class="h-[18px] w-[18px] flex-none" />
          {{ n.label }}
        </RouterLink>
      </nav>
    </div>

    <div class="px-3">
      <button
        type="button" @click="sair"
        class="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-navy/60 transition hover:bg-navy/5 hover:text-navy"
      >
        <Icon name="logout" class="h-[18px] w-[18px] flex-none" />
        Sair da conta
      </button>
    </div>
  </aside>
</template>
