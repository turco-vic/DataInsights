<script setup>
import { ref } from 'vue';

const perfil = ref({ nome: 'Ana Ribeiro', email: 'ana.ribeiro@empresa.com', cargo: 'Controladoria', fuso: 'America/Sao_Paulo (GMT-3)' });
const prefs = ref([
  { t: 'Tratar dados automaticamente', d: 'Remove duplicidades e padroniza formatos no upload.', on: true },
  { t: 'Gerar dashboard ao concluir', d: 'Abre os gráficos assim que o arquivo é processado.', on: true },
  { t: 'Resumo semanal por e-mail', d: 'Toda segunda-feira, com as principais variações.', on: false },
]);
const campo = 'w-full rounded-[10px] border border-navy/15 bg-mist px-3.5 py-3 text-[15px] text-navy outline-none focus:border-sky focus:bg-white';
</script>

<template>
  <div class="max-w-[760px]">
    <h1 class="text-3xl font-extrabold tracking-[-0.03em] text-navy">Configurações</h1>
    <p class="mt-2 text-[15px] text-navy/60">Dados da conta, preferências de análise e segurança.</p>

    <div class="mt-6 rounded-2xl border border-navy/10 bg-white px-7 py-[26px]">
      <h2 class="mb-5 text-base font-extrabold tracking-tight text-navy">Perfil</h2>
      <div class="grid gap-[18px] sm:grid-cols-2">
        <label class="flex flex-col gap-2">
          <span class="text-[13px] font-bold text-navy">Nome</span>
          <input v-model="perfil.nome" type="text" :class="campo" />
        </label>
        <label class="flex flex-col gap-2">
          <span class="text-[13px] font-bold text-navy">E-mail</span>
          <input v-model="perfil.email" type="email" :class="campo" />
        </label>
        <label class="flex flex-col gap-2">
          <span class="text-[13px] font-bold text-navy">Cargo</span>
          <input v-model="perfil.cargo" type="text" :class="campo" />
        </label>
        <label class="flex flex-col gap-2">
          <span class="text-[13px] font-bold text-navy">Fuso horário</span>
          <input v-model="perfil.fuso" type="text" :class="campo" />
        </label>
      </div>
    </div>

    <div class="mt-4 rounded-2xl border border-navy/10 bg-white px-7 py-[26px]">
      <h2 class="text-base font-extrabold tracking-tight text-navy">Preferências de análise</h2>
      <div class="flex flex-col divide-y divide-navy/5">
        <div v-for="p in prefs" :key="p.t" class="flex items-center justify-between gap-5 py-[18px]">
          <div>
            <div class="text-[14.5px] font-bold text-navy">{{ p.t }}</div>
            <div class="mt-1 text-[13px] text-navy/60">{{ p.d }}</div>
          </div>
          <button
            type="button" @click="p.on = !p.on" role="switch" :aria-checked="p.on"
            class="flex h-[26px] w-11 flex-none rounded-full p-[3px] transition"
            :class="[p.on ? 'bg-ocean justify-end' : 'bg-navy/15 justify-start']"
          >
            <span class="block h-5 w-5 rounded-full bg-white"></span>
          </button>
        </div>
      </div>
    </div>

    <div class="mt-4 rounded-2xl border border-navy/10 bg-white px-7 py-[26px]">
      <h2 class="mb-[18px] text-base font-extrabold tracking-tight text-navy">Segurança</h2>
      <div class="flex flex-wrap items-center justify-between gap-5">
        <div>
          <div class="text-[14.5px] font-bold text-navy">Senha</div>
          <div class="mt-1 text-[13px] text-navy/60">Alterada há 3 meses.</div>
        </div>
        <button type="button" class="rounded-full border border-navy/15 bg-mist px-5 py-[11px] text-sm font-semibold text-navy/70 hover:text-navy">Alterar senha</button>
      </div>
    </div>

    <div class="mt-6 flex items-center gap-3.5">
      <button type="button" class="rounded-full bg-ocean px-[30px] py-3.5 text-[15px] font-bold text-white hover:bg-sky">Salvar alterações</button>
      <button type="button" class="text-[15px] font-semibold text-navy/60 hover:text-navy">Cancelar</button>
    </div>
  </div>
</template>
