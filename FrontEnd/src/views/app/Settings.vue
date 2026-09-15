<script setup>
import { ref } from 'vue';
import Icon from '../../components/Icon.vue';
import { useAuthStore } from '../../stores/authStore';

const u = useAuthStore().usuario || {};
const perfil = ref({
  nome: u.nome || '',
  email: u.email || '',
  cargo: u.cargo || 'Controladoria',
  fuso: u.fuso || 'America/Sao_Paulo (GMT-3)',
});

const prefs = ref([
  { t: 'Tratar dados automaticamente', d: 'Remove duplicidades e padroniza formatos no upload.', on: true },
  { t: 'Gerar dashboard ao concluir', d: 'Abre os gráficos assim que o arquivo é processado.', on: true },
  { t: 'Resumo semanal por e-mail', d: 'Toda segunda-feira, com as principais variações.', on: false },
]);

const salvo = ref(false);
const campo = 'w-full rounded-lg border border-navy/15 bg-mist px-3.5 py-2.5 text-[15px] text-navy outline-none transition focus:border-sky focus:bg-white focus:ring-4 focus:ring-sky/10';

function salvar() {
  // TODO: persistir perfil e preferências no backend.
  salvo.value = true;
  setTimeout(() => { salvo.value = false; }, 2500);
}
</script>

<template>
  <div class="max-w-[760px]">
    <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Configurações</h1>
    <p class="mt-1.5 text-sm text-navy/60">Dados da conta, preferências de análise e segurança.</p>

    <div class="mt-6 rounded-xl border border-navy/10 bg-white px-7 py-6">
      <h2 class="mb-5 text-base font-bold tracking-tight text-navy">Perfil</h2>
      <div class="grid gap-4 sm:grid-cols-2">
        <label class="flex flex-col gap-2">
          <span class="text-[13px] font-semibold text-navy">Nome</span>
          <input v-model="perfil.nome" type="text" :class="campo" />
        </label>
        <label class="flex flex-col gap-2">
          <span class="text-[13px] font-semibold text-navy">E-mail</span>
          <input v-model="perfil.email" type="email" :class="campo" />
        </label>
        <label class="flex flex-col gap-2">
          <span class="text-[13px] font-semibold text-navy">Cargo</span>
          <input v-model="perfil.cargo" type="text" :class="campo" />
        </label>
        <label class="flex flex-col gap-2">
          <span class="text-[13px] font-semibold text-navy">Fuso horário</span>
          <input v-model="perfil.fuso" type="text" :class="campo" />
        </label>
      </div>
    </div>

    <div class="mt-4 rounded-xl border border-navy/10 bg-white px-7 py-6">
      <h2 class="text-base font-bold tracking-tight text-navy">Preferências de análise</h2>
      <div class="flex flex-col divide-y divide-navy/5">
        <div v-for="p in prefs" :key="p.t" class="flex items-center justify-between gap-5 py-4">
          <div>
            <div class="text-sm font-semibold text-navy">{{ p.t }}</div>
            <div class="mt-1 text-[13px] text-navy/60">{{ p.d }}</div>
          </div>
          <button
            type="button" @click="p.on = !p.on" role="switch" :aria-checked="p.on"
            class="flex h-[26px] w-11 flex-none rounded-full p-0.5 transition"
            :class="p.on ? 'bg-ocean justify-end' : 'bg-navy/15 justify-start'"
          >
            <span class="block h-5 w-5 rounded-full bg-white shadow-sm"></span>
          </button>
        </div>
      </div>
    </div>

    <div class="mt-4 rounded-xl border border-navy/10 bg-white px-7 py-6">
      <h2 class="mb-4 text-base font-bold tracking-tight text-navy">Segurança</h2>
      <div class="flex flex-wrap items-center justify-between gap-5">
        <div>
          <div class="text-sm font-semibold text-navy">Senha</div>
          <div class="mt-1 text-[13px] text-navy/60">Alterada há 3 meses.</div>
        </div>
        <button type="button" class="rounded-lg border border-navy/15 bg-mist px-5 py-2.5 text-sm font-medium text-navy/70 transition hover:text-navy">Alterar senha</button>
      </div>
    </div>

    <div class="mt-6 flex items-center gap-4">
      <button type="button" @click="salvar" class="rounded-lg bg-ocean px-7 py-3 text-sm font-semibold text-white transition hover:bg-navy">Salvar alterações</button>
      <button type="button" class="text-sm font-medium text-navy/60 transition hover:text-navy">Cancelar</button>
      <span v-if="salvo" class="flex items-center gap-1.5 text-sm font-semibold text-emerald-700">
        <Icon name="check" class="h-4 w-4" /> Alterações salvas
      </span>
    </div>
  </div>
</template>
