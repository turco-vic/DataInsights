<script setup>
import { ref, computed, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import StatusBadge from '../../components/StatusBadge.vue';
import { filesService } from '../../services/files';

const arquivos = ref([]);
onMounted(async () => { arquivos.value = await filesService.listar(); });

const filtros = [
  { label: 'Todos', status: null },
  { label: 'Processados', status: 'processado' },
  { label: 'Processando', status: 'processando' },
  { label: 'Com erros', status: 'erro' },
];
const filtro = ref(null);

const lista = computed(() =>
  filtro.value ? arquivos.value.filter((a) => a.status === filtro.value) : arquivos.value
);
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Planilhas enviadas</h1>
        <p class="mt-1.5 text-sm text-navy/60">{{ arquivos.length }} arquivos processados nos últimos 12 meses</p>
      </div>
      <RouterLink to="/app/upload" class="rounded-lg bg-ocean px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-navy">Enviar planilha</RouterLink>
    </div>

    <div class="my-5 flex flex-wrap items-center gap-2">
      <button
        v-for="f in filtros" :key="f.label" type="button" @click="filtro = f.status"
        class="rounded-full px-3.5 py-1.5 text-[13px] font-medium transition"
        :class="filtro === f.status ? 'bg-navy text-white' : 'border border-navy/15 bg-white text-navy/65 hover:text-navy'"
      >{{ f.label }}</button>
    </div>

    <div class="overflow-hidden rounded-xl border border-navy/10 bg-white">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[720px] border-collapse">
          <thead>
            <tr class="border-b border-navy/10 text-[11px] font-semibold uppercase tracking-wide text-navy/50">
              <th class="px-6 py-3.5 text-left">Arquivo</th>
              <th class="px-4 py-3.5 text-left">Enviado por</th>
              <th class="px-4 py-3.5 text-left">Data</th>
              <th class="px-4 py-3.5 text-right">Linhas</th>
              <th class="px-4 py-3.5 text-right">Status</th>
              <th class="px-6 py-3.5"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in lista" :key="a.nome" class="border-b border-navy/5 last:border-0 hover:bg-mist">
              <td class="px-6 py-3.5 text-sm font-semibold text-navy">{{ a.nome }}</td>
              <td class="px-4 py-3.5 text-sm text-navy/65">{{ a.autor }}</td>
              <td class="px-4 py-3.5 text-[13px] text-navy/55">{{ a.data }}</td>
              <td class="whitespace-nowrap px-4 py-3.5 text-right text-sm font-semibold text-navy">{{ a.linhas }}</td>
              <td class="px-4 py-3.5 text-right"><StatusBadge :status="a.status" /></td>
              <td class="px-6 py-3.5 text-right"><RouterLink to="/app/dataset" class="whitespace-nowrap text-[13px] font-semibold text-ocean hover:text-navy">Abrir</RouterLink></td>
            </tr>
            <tr v-if="!lista.length">
              <td colspan="6" class="px-6 py-10 text-center text-sm text-navy/50">Nenhuma planilha neste filtro.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
