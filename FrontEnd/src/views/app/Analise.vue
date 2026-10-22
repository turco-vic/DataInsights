<script setup>
// Gráficos com as estatísticas que o serviço Python calculou e gravou no banco (aula de Ciência de Dados).
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import BaseChart from '../../components/BaseChart.vue';
import SemPlanilhas from '../../components/SemPlanilhas.vue';
import api, { mensagemErro } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';
import { usePlanilhasStore } from '../../stores/planilhasStore';
import { useUiStore } from '../../stores/uiStore';
import { moeda, moedaCompacta, dataHora } from '../../utils/formato';
import { botaoPrimario, campo } from '../../utils/estilos';

const auth = useAuthStore();
const planilhas = usePlanilhasStore();
const ui = useUiStore();

const loteId = ref('');
const lote = ref(null);
const d = ref(null); // o JSON que o Python salvou (null enquanto o ETL não terminou)
const geradoEm = ref(null);
const carregando = ref(true);
const semPlanilha = ref(false);
const erro = ref('');
let relogio = null;

const ETAPAS = {
  AGUARDANDO_ETL: { label: 'Aguardando o Python', cls: 'bg-sky/15 text-ocean' },
  PROCESSANDO: { label: 'Processando', cls: 'bg-sky/15 text-ocean' },
  PROCESSADO: { label: 'Processado', cls: 'bg-emerald-600/10 text-emerald-700' },
  ERRO: { label: 'Erro no ETL', cls: 'bg-red-600/10 text-red-700' },
};
const etapa = computed(() => ETAPAS[lote.value?.statusEtl] || { label: 'Sem análise', cls: 'bg-navy/10 text-navy/60' });
const esperando = computed(() => ['AGUARDANDO_ETL', 'PROCESSANDO'].includes(lote.value?.statusEtl) && !d.value);

// Cada gráfico lê uma lista do JSON do Python e separa em nomes (rotulos) e números (valores).
const seg = computed(() => d.value?.porSegmento ?? []);
const niv = computed(() => d.value?.porNivel ?? []);
const con = computed(() => d.value?.porConsultor ?? []);
const ser = computed(() => d.value?.porServico ?? []);
const mes = computed(() => d.value?.porMes ?? []);

const kpis = computed(() => {
  const r = d.value.resumo;
  return [
    { label: 'Clientes', value: r.totalClientes, sub: `${r.consultores} consultor(es)` },
    { label: 'Faturamento total', value: moedaCompacta(r.faturamentoTotal), sub: `de ${moeda(r.minimo)} a ${moeda(r.maximo)}` },
    { label: 'Média', value: moedaCompacta(r.media), sub: moeda(r.media) },
    { label: 'Mediana', value: moedaCompacta(r.mediana), sub: moeda(r.mediana) },
    { label: 'Desvio padrão', value: moedaCompacta(r.desvioPadrao), sub: r.coeficienteVariacao == null ? 'precisa de 2 ou mais clientes' : `variação de ${Math.round(r.coeficienteVariacao * 100)}% da média` },
  ];
});

function nomeMes(m) {
  const [ano, numero] = m.split('-');
  return `${numero}/${ano.slice(2)}`;
}

async function buscar() {
  erro.value = '';
  try {
    const params = loteId.value ? { loteId: loteId.value } : {};
    const resposta = (await api.get('/api/estatisticas', { params })).data;
    lote.value = resposta.lote;
    d.value = resposta.estatisticas;
    geradoEm.value = resposta.geradoEm;
    semPlanilha.value = false;
    if (!loteId.value) loteId.value = resposta.lote.id;
  } catch (e) {
    if (e.response?.status === 404) semPlanilha.value = true;
    else erro.value = mensagemErro(e, 'Não foi possível carregar a análise.');
  } finally {
    carregando.value = false;
  }
  // Enquanto o Python trabalha, pergunta de novo a cada 5 segundos.
  if (esperando.value && !relogio) relogio = setInterval(buscar, 5000);
  if (!esperando.value && relogio) {
    clearInterval(relogio);
    relogio = null;
  }
}

async function trocarPlanilha() {
  carregando.value = true;
  d.value = null;
  await buscar();
}

async function reenviar() {
  try {
    lote.value = (await api.post(`/api/importacoes/${lote.value.id}/etl`)).data;
    ui.notificar('Planilha enviada ao serviço Python.');
    await buscar();
  } catch (e) {
    ui.notificar(mensagemErro(e), 'erro');
  }
}

onMounted(() => {
  planilhas.carregarLista().catch(() => {});
  buscar();
});
onBeforeUnmount(() => relogio && clearInterval(relogio));
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Análise da planilha</h1>
        <p class="mt-1.5 text-sm text-navy/60">Estatísticas e insights calculados pelo serviço Python (pandas)</p>
      </div>
      <select v-if="planilhas.lista.length" v-model="loteId" @change="trocarPlanilha" :class="campo" class="max-w-80" aria-label="Planilha">
        <option v-for="p in planilhas.lista" :key="p.id" :value="p.id">{{ p.nomeArquivo }} · {{ dataHora(p.enviadaEm) }}</option>
      </select>
    </div>

    <p v-if="carregando" class="text-sm text-navy/55">Carregando análise…</p>
    <p v-else-if="erro" class="rounded-xl border border-red-500/30 bg-red-50 px-5 py-4 text-sm font-medium text-red-800">{{ erro }}</p>
    <SemPlanilhas
      v-else-if="semPlanilha"
      texto="A análise é feita pelo serviço Python depois que uma planilha é importada."
      :mostrar-botao="auth.podeEditar"
    />

    <template v-else-if="lote">
      <div class="mb-4 flex flex-wrap items-center gap-3 rounded-xl border border-navy/10 bg-white px-5 py-3.5 text-sm">
        <span class="rounded-full px-2.5 py-1 text-xs font-bold" :class="etapa.cls">{{ etapa.label }}</span>
        <span class="flex-1 text-navy/65">{{ lote.mensagemEtl || 'Esta planilha foi importada antes da etapa de Ciência de Dados.' }}</span>
        <span v-if="geradoEm" class="text-xs text-navy/45">calculado em {{ dataHora(geradoEm) }}</span>
        <button
          v-if="auth.podeEditar && !esperando" type="button" @click="reenviar"
          :class="d ? 'text-[13px] font-semibold text-ocean hover:text-navy' : botaoPrimario"
        >{{ d ? 'Calcular de novo' : 'Enviar ao Python' }}</button>
      </div>

      <p v-if="esperando" class="rounded-xl border border-sky/30 bg-sky/10 px-5 py-4 text-sm font-medium text-ocean">
        O Python está processando a planilha. Esta tela atualiza sozinha.
      </p>

      <template v-if="d">
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
          <div v-for="k in kpis" :key="k.label" class="rounded-xl border border-navy/10 bg-white px-5 py-4">
            <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">{{ k.label }}</div>
            <div class="mt-2 text-2xl font-bold tracking-tight text-navy">{{ k.value }}</div>
            <div class="mt-1.5 text-[13px] font-medium text-navy/55">{{ k.sub }}</div>
          </div>
        </div>

        <div v-if="d.insights?.length" class="mt-4 rounded-xl border border-navy/10 bg-white p-6">
          <h2 class="mb-3 text-base font-bold tracking-tight text-navy">O que os dados mostram</h2>
          <ul class="list-disc space-y-1.5 pl-5 text-sm leading-relaxed text-navy/75">
            <li v-for="frase in d.insights" :key="frase">{{ frase }}</li>
          </ul>
        </div>

        <div class="mt-4 grid gap-4 lg:grid-cols-2">
          <div class="min-w-0 rounded-xl border border-navy/10 bg-white p-6">
            <h2 class="mb-4 text-base font-bold tracking-tight text-navy">Clientes por segmento</h2>
            <BaseChart tipo="bar" titulo="Clientes" :rotulos="seg.map((x) => x.segmento)" :valores="seg.map((x) => x.clientes)" />
          </div>
          <div class="min-w-0 rounded-xl border border-navy/10 bg-white p-6">
            <h2 class="mb-4 text-base font-bold tracking-tight text-navy">Clientes por nível</h2>
            <BaseChart tipo="doughnut" titulo="Clientes" :rotulos="niv.map((x) => 'Nível ' + x.nivel)" :valores="niv.map((x) => x.clientes)" />
          </div>
          <div class="min-w-0 rounded-xl border border-navy/10 bg-white p-6">
            <h2 class="mb-4 text-base font-bold tracking-tight text-navy">Faturamento por consultor</h2>
            <BaseChart tipo="bar" horizontal titulo="Faturamento" :formatar="moedaCompacta" :rotulos="con.map((x) => x.consultor)" :valores="con.map((x) => x.faturamentoTotal)" />
          </div>
          <div class="min-w-0 rounded-xl border border-navy/10 bg-white p-6">
            <h2 class="mb-4 text-base font-bold tracking-tight text-navy">Serviços mais contratados</h2>
            <BaseChart tipo="bar" horizontal titulo="Clientes" :rotulos="ser.map((x) => x.servico)" :valores="ser.map((x) => x.contratos)" />
          </div>
          <div class="min-w-0 rounded-xl border border-navy/10 bg-white p-6 lg:col-span-2">
            <h2 class="mb-4 text-base font-bold tracking-tight text-navy">Contratações por mês</h2>
            <BaseChart tipo="line" titulo="Clientes" :rotulos="mes.map((x) => nomeMes(x.mes))" :valores="mes.map((x) => x.clientes)" />
          </div>
        </div>

        <p class="mt-4 text-xs text-navy/50">
          Linhas recebidas: {{ d.qualidade.linhasRecebidas }} · válidas: {{ d.qualidade.linhasValidas }}
          · segmentos corrigidos: {{ d.qualidade.correcoesSegmento }}
          <template v-if="d.correlacao?.pearson != null"> · correlação serviços × faturamento (Pearson): {{ String(d.correlacao.pearson).replace('.', ',') }}</template>
        </p>
      </template>
    </template>
  </div>
</template>
