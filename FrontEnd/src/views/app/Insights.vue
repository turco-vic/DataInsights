<script setup>
import { computed, onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import Icon from '../../components/Icon.vue';
import api, { mensagemErro } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';
import { useUiStore } from '../../stores/uiStore';
import { botaoPrimario, campo, cartao } from '../../utils/estilos';

const auth = useAuthStore();
const ui = useUiStore();

const insights = ref([]);
const carregando = ref(true);
const erro = ref('');
const gerando = ref(false);

const tipo = ref(null);
const busca = ref('');

const tipos = computed(() => {
  const contagem = {};
  insights.value.forEach((i) => { contagem[i.tipo] = (contagem[i.tipo] || 0) + 1; });
  return Object.entries(contagem).sort((a, b) => b[1] - a[1]);
});

const normalizar = (t) => String(t || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();

const filtrados = computed(() => {
  const t = normalizar(busca.value.trim());
  return insights.value.filter((i) =>
    (!tipo.value || i.tipo === tipo.value)
    && (!t || normalizar(`${i.cliente?.nome} ${i.cliente?.codigo} ${i.descricao}`).includes(t)));
});

async function carregar() {
  carregando.value = true;
  erro.value = '';
  try {
    insights.value = (await api.get('/api/insights')).data;
  } catch (e) {
    erro.value = mensagemErro(e, 'Não foi possível carregar os insights.');
  } finally {
    carregando.value = false;
  }
}

async function gerar() {
  gerando.value = true;
  try {
    const { data } = await api.post('/api/insights/gerar');
    ui.notificar(data.criados ? `${data.criados} insight(s) novo(s) encontrado(s).` : 'Nenhum insight novo: a carteira já está analisada.');
    await carregar();
  } catch (e) {
    ui.notificar(mensagemErro(e), 'erro');
  } finally {
    gerando.value = false;
  }
}

async function excluir(i) {
  const ok = await ui.confirmar({ titulo: `Excluir o insight "${i.tipo}"?`, mensagem: i.descricao, botao: 'Excluir insight', perigo: true });
  if (!ok) return;
  try {
    await api.delete(`/api/insights/${i.id}`);
    insights.value = insights.value.filter((x) => x.id !== i.id);
    ui.notificar('Insight excluído.');
  } catch (e) {
    ui.notificar(mensagemErro(e), 'erro');
  }
}

onMounted(carregar);
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Insights</h1>
        <p class="mt-1.5 max-w-2xl text-sm text-navy/60">
          Oportunidades e alertas por cliente. Os automáticos são gerados a cada planilha importada:
          nível A com um só serviço, nível C faturando acima da média do nível A, cliente sem contrato ativo e cliente sem consultor.
        </p>
      </div>
      <button v-if="auth.podeEditar" type="button" :disabled="gerando" :class="botaoPrimario" @click="gerar">
        {{ gerando ? 'Analisando…' : 'Analisar carteira agora' }}
      </button>
    </div>

    <div class="mt-6 flex flex-wrap items-center gap-2">
      <button
        type="button" class="rounded-full px-3.5 py-1.5 text-[13px] font-medium transition"
        :class="!tipo ? 'bg-navy text-white' : 'border border-navy/15 bg-white text-navy/65 hover:text-navy'"
        @click="tipo = null"
      >Todos ({{ insights.length }})</button>
      <button
        v-for="[t, qtd] in tipos" :key="t" type="button"
        class="rounded-full px-3.5 py-1.5 text-[13px] font-medium transition"
        :class="tipo === t ? 'bg-navy text-white' : 'border border-navy/15 bg-white text-navy/65 hover:text-navy'"
        @click="tipo = t"
      >{{ t }} ({{ qtd }})</button>
      <label class="ml-auto w-full sm:w-64">
        <span class="sr-only">Buscar</span>
        <input v-model="busca" type="search" placeholder="Buscar por cliente ou texto" :class="campo" />
      </label>
    </div>

    <p v-if="carregando && !insights.length" class="mt-6 text-sm text-navy/55">Carregando insights…</p>
    <p v-else-if="erro" class="mt-6 text-sm font-medium text-red-700">{{ erro }}</p>
    <p v-else-if="!filtrados.length" :class="cartao" class="mt-4 px-6 py-10 text-center text-sm text-navy/55">
      {{ insights.length ? 'Nenhum insight com esses filtros.' : 'Nenhum insight ainda. Importe uma planilha ou analise a carteira.' }}
    </p>
    <div v-else class="mt-4 grid gap-3 md:grid-cols-2">
      <article v-for="i in filtrados" :key="i.id" :class="cartao" class="flex gap-4 px-5 py-4">
        <span class="w-1 flex-none rounded-full bg-ocean"></span>
        <div class="min-w-0 flex-1">
          <div class="flex items-start justify-between gap-3">
            <div>
              <span class="text-[11px] font-bold uppercase tracking-wide text-ocean">{{ i.tipo }}</span>
              <RouterLink :to="`/app/clientes/${i.cliente?.id}`" class="block text-sm font-bold text-navy hover:text-ocean">
                {{ i.cliente?.nome }} <span class="font-medium text-navy/50">· {{ i.cliente?.codigo }}</span>
              </RouterLink>
            </div>
            <button v-if="auth.podeEditar" type="button" aria-label="Excluir insight" class="flex-none text-navy/40 hover:text-red-700" @click="excluir(i)">
              <Icon name="trash" class="h-4 w-4" />
            </button>
          </div>
          <p class="mt-1.5 text-[13px] leading-relaxed text-navy/70">{{ i.descricao }}</p>
        </div>
      </article>
    </div>
  </div>
</template>
