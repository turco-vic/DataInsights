<script setup>
import { onMounted, ref, watch } from 'vue';
import { RouterLink } from 'vue-router';
import Icon from '../../components/Icon.vue';
import api, { mensagemErro } from '../../services/api';
import { EVENTOS } from '../../utils/dominio';
import { botaoSecundario, cabecalhoTabela, campo, cartao } from '../../utils/estilos';
import { dataHora } from '../../utils/formato';

const eventos = ref([]);
const carregando = ref(true);
const erro = ref('');
const filtro = ref('');

const TOM = {
  login_falhou: 'bg-red-600/10 text-red-700',
  cliente_excluido: 'bg-red-600/10 text-red-700',
  usuario_excluido: 'bg-red-600/10 text-red-700',
  importacao: 'bg-emerald-600/10 text-emerald-700',
  insights_gerados: 'bg-sky/15 text-ocean',
};

async function carregar() {
  carregando.value = true;
  erro.value = '';
  try {
    const params = filtro.value ? { evento: filtro.value } : {};
    eventos.value = (await api.get('/api/telemetrias', { params })).data;
  } catch (e) {
    erro.value = mensagemErro(e, 'Não foi possível carregar a telemetria.');
  } finally {
    carregando.value = false;
  }
}

watch(filtro, carregar);
onMounted(carregar);
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Telemetria</h1>
        <p class="mt-1.5 text-sm text-navy/60">Os 500 eventos mais recentes: logins, importações, exclusões e trocas de senha.</p>
      </div>
      <div class="flex gap-3">
        <label class="w-56">
          <span class="sr-only">Tipo de evento</span>
          <select v-model="filtro" :class="campo">
            <option value="">Todos os eventos</option>
            <option v-for="(rotulo, chave) in EVENTOS" :key="chave" :value="chave">{{ rotulo }}</option>
          </select>
        </label>
        <button type="button" :class="botaoSecundario" class="flex items-center gap-2" :disabled="carregando" @click="carregar">
          <Icon name="refresh" class="h-4 w-4" /> Atualizar
        </button>
      </div>
    </div>

    <div :class="cartao" class="mt-6 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[720px] border-collapse">
          <thead>
            <tr :class="cabecalhoTabela">
              <th class="px-6 py-3.5 text-left">Quando</th>
              <th class="px-4 py-3.5 text-left">Evento</th>
              <th class="px-4 py-3.5 text-left">Detalhe</th>
              <th class="px-6 py-3.5 text-left">Cliente</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="carregando && !eventos.length"><td colspan="4" class="px-6 py-8 text-center text-sm text-navy/55">Carregando…</td></tr>
            <tr v-else-if="erro"><td colspan="4" class="px-6 py-8 text-center text-sm font-medium text-red-700">{{ erro }}</td></tr>
            <tr v-else-if="!eventos.length"><td colspan="4" class="px-6 py-10 text-center text-sm text-navy/55">Nenhum evento registrado.</td></tr>
            <template v-else>
              <tr v-for="t in eventos" :key="t.id" class="border-b border-navy/5 last:border-0">
                <td class="whitespace-nowrap px-6 py-3 text-[13px] text-navy/60">{{ dataHora(t.dataHora) }}</td>
                <td class="px-4 py-3">
                  <span class="whitespace-nowrap rounded-full px-2.5 py-1 text-xs font-bold" :class="TOM[t.evento] || 'bg-navy/5 text-navy/70'">
                    {{ EVENTOS[t.evento] || t.evento }}
                  </span>
                </td>
                <td class="px-4 py-3 text-[13px] text-navy/70">{{ t.mensagem || '—' }}</td>
                <td class="px-6 py-3 text-[13px]">
                  <RouterLink v-if="t.cliente" :to="`/app/clientes/${t.cliente.id}`" class="font-semibold text-ocean hover:text-navy">{{ t.cliente.nome }}</RouterLink>
                  <span v-else class="text-navy/40">—</span>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
