<script setup>
import { computed } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import AppLogo from '../AppLogo.vue';
import Icon from '../Icon.vue';
import { useAuthStore } from '../../stores/authStore';

defineProps({ aberto: { type: Boolean, default: false } });
const emit = defineEmits(['fechar']);

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const EDITORES = ['admin', 'analista'];

const grupos = [
  {
    titulo: 'Análise',
    itens: [
      { to: '/app', label: 'Dashboard', icon: 'dashboard' },
      { to: '/app/analise', label: 'Análise (Python)', icon: 'activity' },
      { to: '/app/upload', label: 'Upload de planilha', icon: 'upload', papeis: EDITORES },
      { to: '/app/relatorio', label: 'Relatório de validação', icon: 'check', papeis: EDITORES },
      { to: '/app/planilhas', label: 'Planilhas enviadas', icon: 'files' },
      { to: '/app/dataset', label: 'Relatório do dataset', icon: 'dataset' },
    ],
  },
  {
    titulo: 'Cadastros',
    itens: [
      { to: '/app/clientes', label: 'Clientes', icon: 'briefcase' },
      { to: '/app/consultores', label: 'Consultores', icon: 'user' },
      { to: '/app/servicos', label: 'Serviços', icon: 'layers' },
      { to: '/app/insights', label: 'Insights', icon: 'bulb' },
    ],
  },
  {
    titulo: 'Administração',
    itens: [
      { to: '/app/usuarios', label: 'Usuários', icon: 'users', papeis: ['admin'] },
      { to: '/app/telemetria', label: 'Telemetria', icon: 'activity', papeis: ['admin'] },
      { to: '/app/configuracoes', label: 'Configurações', icon: 'settings' },
    ],
  },
];

const visiveis = computed(() =>
  grupos
    .map((g) => ({ ...g, itens: g.itens.filter((i) => !i.papeis || i.papeis.includes(auth.papel)) }))
    .filter((g) => g.itens.length),
);

const ativo = (to) => (to === '/app' ? route.path === to : route.path === to || route.path.startsWith(`${to}/`));

function sair() {
  auth.logout();
  router.push('/');
}
</script>

<template>
  <aside
    id="menu-lateral"
    class="fixed inset-y-0 left-0 z-40 flex h-screen w-64 flex-none flex-col justify-between overflow-y-auto border-r border-navy/10 bg-white py-6 transition-transform duration-200 lg:sticky lg:top-0 lg:bottom-auto lg:translate-x-0 print:hidden"
    :class="aberto ? 'translate-x-0 shadow-xl' : '-translate-x-full'"
    @keydown.esc="emit('fechar')"
  >
    <div>
      <div class="flex items-center justify-between px-6 pb-6">
        <RouterLink to="/" class="block">
          <AppLogo />
        </RouterLink>
        <button type="button" @click="emit('fechar')" aria-label="Fechar menu" class="rounded-lg p-1.5 text-navy/50 hover:bg-navy/5 hover:text-navy lg:hidden">
          <Icon name="close" class="h-5 w-5" />
        </button>
      </div>

      <nav class="flex flex-col gap-5 px-3" aria-label="Navegação principal">
        <div v-for="g in visiveis" :key="g.titulo">
          <div class="mb-1.5 px-3 text-[11px] font-bold uppercase tracking-[0.12em] text-navy/40">{{ g.titulo }}</div>
          <div class="flex flex-col gap-0.5">
            <RouterLink
              v-for="n in g.itens" :key="n.to" :to="n.to"
              class="flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition"
              :class="ativo(n.to)
                ? 'bg-ocean/10 font-semibold text-ocean'
                : 'font-medium text-navy/65 hover:bg-navy/5 hover:text-navy'"
              :aria-current="ativo(n.to) ? 'page' : undefined"
            >
              <Icon :name="n.icon" class="h-[18px] w-[18px] flex-none" />
              {{ n.label }}
            </RouterLink>
          </div>
        </div>
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
