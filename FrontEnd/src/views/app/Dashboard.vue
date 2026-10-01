<script setup>
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import SemPlanilhas from '../../components/SemPlanilhas.vue';
import api, { mensagemErro } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';
import { moeda, moedaCompacta, dataHora } from '../../utils/formato';

const router = useRouter();
const auth = useAuthStore();

const painel = ref(null);
const carregando = ref(true);
const erro = ref('');

async function carregar() {
  carregando.value = true;
  erro.value = '';
  try {
    painel.value = (await api.get('/api/dashboard')).data;
  } catch (e) {
    erro.value = mensagemErro(e, 'Não foi possível carregar o dashboard.');
  } finally {
    carregando.value = false;
  }
}

onMounted(carregar);

const total = computed(() => painel.value?.totalClientes || 0);

const kpis = computed(() => {
  const p = painel.value;
  const nivelA = p.porNivel.find((n) => n.nome === 'A')?.quantidade || 0;
  return [
    { label: 'Clientes', value: p.totalClientes, sub: 'na carteira', to: '/app/clientes' },
    { label: 'Faturamento anual', value: moedaCompacta(p.faturamentoTotal), sub: `média de ${moedaCompacta(p.faturamentoMedio)} por cliente` },
    { label: 'Clientes nível A', value: nivelA, sub: `${total.value ? Math.round((nivelA / total.value) * 100) : 0}% da carteira`, to: { path: '/app/clientes', query: { nivel: 'A' } } },
    { label: 'Contratos ativos', value: p.contratosAtivos, sub: `${p.insights} insight(s) registrado(s)`, to: '/app/insights' },
  ];
});

const meses = computed(() => {
  const lista = painel.value?.contratacoesPorMes || [];
  const maximo = Math.max(1, ...lista.map((m) => m.quantidade));
  return lista.map((m) => ({ rotulo: m.nome, quantidade: m.quantidade, altura: (m.quantidade / maximo) * 100 }));
});

const CORES = ['var(--color-navy)', 'var(--color-ocean)', 'var(--color-sky)', 'color-mix(in srgb, var(--color-navy) 14%, transparent)'];
const segmentos = computed(() => {
  const lista = painel.value?.porSegmento || [];
  const base = total.value || 1;
  const principais = lista.slice(0, 3).map((s) => ({ nome: s.nome, qtd: s.quantidade }));
  const outros = lista.slice(3).reduce((t, s) => t + s.quantidade, 0);
  if (outros) principais.push({ nome: 'Outros', qtd: outros });
  return principais.map((s, i) => ({ ...s, pct: Math.round((s.qtd / base) * 100), cor: CORES[i] }));
});

const donut = computed(() => {
  let acc = 0;
  const partes = segmentos.value.map((s) => {
    const inicio = acc;
    acc += s.pct;
    return `${s.cor} ${inicio}% ${acc}%`;
  });
  return `conic-gradient(${partes.join(', ')})`;
});

const servicos = computed(() => {
  const lista = painel.value?.servicos || [];
  const maximo = lista[0]?.quantidade || 1;
  return lista.map((s) => ({ nome: s.nome, qtd: s.quantidade, largura: (s.quantidade / maximo) * 100 }));
});

function barCor(i, quantidade) {
  if (i === quantidade - 1) return 'bg-navy';
  if (i === quantidade - 2) return 'bg-ocean';
  return 'bg-navy/20';
}
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Dashboard</h1>
        <p v-if="painel?.ultimaImportacao" class="mt-1.5 text-sm text-navy/60">
          Carteira atual · última planilha: {{ painel.ultimaImportacao.nomeArquivo }} em {{ dataHora(painel.ultimaImportacao.enviadaEm) }}
        </p>
        <p v-else-if="painel" class="mt-1.5 text-sm text-navy/60">Carteira atual, com todos os clientes cadastrados.</p>
      </div>
    </div>

    <p v-if="carregando && !painel" class="text-sm text-navy/55">Carregando indicadores…</p>
    <p v-else-if="erro" class="rounded-xl border border-red-500/30 bg-red-50 px-5 py-4 text-sm font-medium text-red-800">{{ erro }}</p>

    <SemPlanilhas
      v-else-if="!total"
      titulo="Nenhum cliente ainda"
      :texto="auth.podeEditar ? 'O dashboard é montado a partir dos clientes. Envie a primeira planilha para começar.' : 'Assim que um analista enviar a primeira planilha, os indicadores aparecem aqui.'"
      :mostrar-botao="auth.podeEditar"
    />

    <template v-else>
      <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <component
          :is="k.to ? RouterLink : 'div'" v-for="k in kpis" :key="k.label" :to="k.to"
          class="block rounded-xl border border-navy/10 bg-white px-5 py-4 transition"
          :class="k.to ? 'hover:border-ocean/40' : ''"
        >
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">{{ k.label }}</div>
          <div class="mt-2 text-3xl font-bold tracking-tight text-navy">{{ k.value }}</div>
          <div class="mt-1.5 text-[13px] font-medium text-navy/55">{{ k.sub }}</div>
        </component>
      </div>

      <div class="mt-4 grid gap-4 lg:grid-cols-2">
        <div class="min-w-0 rounded-xl border border-navy/10 bg-white px-6 pb-5 pt-5 lg:col-span-2">
          <div class="mb-5 flex items-baseline justify-between gap-4">
            <h2 class="text-base font-bold tracking-tight text-navy">Contratações por mês</h2>
            <span class="text-xs text-navy/50">clientes por data de contratação{{ meses.length >= 36 ? ' · últimos 36 meses' : '' }}</span>
          </div>
          <div v-if="meses.length" class="overflow-x-auto">
            <div :style="{ minWidth: Math.max(480, meses.length * 28) + 'px' }">
              <div class="flex h-42 items-end gap-2">
                <div v-for="(m, i) in meses" :key="m.rotulo" class="flex h-full flex-1 flex-col justify-end" :title="`${m.rotulo}: ${m.quantidade}`">
                  <div class="mb-1 text-center text-[11px] font-bold text-navy/60">{{ m.quantidade || '' }}</div>
                  <div class="rounded-t" :class="barCor(i, meses.length)" :style="{ height: m.altura + '%' }"></div>
                </div>
              </div>
              <div class="mt-2.5 flex gap-2">
                <div v-for="m in meses" :key="m.rotulo" class="flex-1 text-center text-[10px] font-semibold text-navy/40">{{ m.rotulo }}</div>
              </div>
            </div>
          </div>
          <p v-else class="text-sm text-navy/50">Nenhum cliente tem data de contratação.</p>
        </div>

        <div class="min-w-0 rounded-xl border border-navy/10 bg-white p-6">
          <h2 class="mb-5 text-base font-bold tracking-tight text-navy">Clientes por segmento</h2>
          <div class="flex flex-wrap items-center gap-6">
            <div class="flex h-33 w-33 flex-none items-center justify-center rounded-full" :style="{ background: donut }">
              <div class="flex h-20 w-20 flex-col items-center justify-center rounded-full bg-white">
                <div class="text-lg font-bold tracking-tight text-navy">{{ total }}</div>
                <div class="text-[10px] font-semibold text-navy/50">clientes</div>
              </div>
            </div>
            <div class="flex min-w-35 flex-1 flex-col gap-3">
              <div v-for="s in segmentos" :key="s.nome" class="flex items-center gap-2.5">
                <span class="h-2.5 w-2.5 flex-none rounded" :style="{ background: s.cor }"></span>
                <span class="flex-1 text-sm font-medium text-navy/75">{{ s.nome }}</span>
                <span class="text-[13px] font-bold text-navy">{{ s.pct }}%</span>
              </div>
            </div>
          </div>
        </div>

        <div class="min-w-0 rounded-xl border border-navy/10 bg-white p-6">
          <h2 class="mb-5 text-base font-bold tracking-tight text-navy">Serviços mais contratados</h2>
          <p v-if="!servicos.length" class="text-sm text-navy/50">Nenhum contrato ativo.</p>
          <div class="flex flex-col gap-3">
            <div v-for="s in servicos" :key="s.nome">
              <div class="flex justify-between text-[13px] font-medium text-navy/70">{{ s.nome }}<span class="font-bold text-navy">{{ s.qtd }}</span></div>
              <div class="mt-1.5 h-1.5 overflow-hidden rounded-full bg-navy/10"><div class="h-full bg-ocean" :style="{ width: s.largura + '%' }"></div></div>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-4 overflow-hidden rounded-xl border border-navy/10 bg-white">
        <div class="flex items-baseline justify-between gap-4 border-b border-navy/10 px-6 py-4">
          <h2 class="text-base font-bold tracking-tight text-navy">Maiores clientes por faturamento</h2>
          <RouterLink to="/app/clientes" class="text-[13px] font-semibold text-ocean hover:text-navy">Ver todos</RouterLink>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full min-w-[720px] border-collapse">
            <thead>
              <tr class="border-b border-navy/10 text-[11px] font-semibold uppercase tracking-wide text-navy/50">
                <th class="px-6 py-3 text-left">Cliente</th>
                <th class="px-4 py-3 text-left">Segmento</th>
                <th class="px-4 py-3 text-left">Consultor</th>
                <th class="px-4 py-3 text-center">Nível</th>
                <th class="px-6 py-3 text-right">Faturamento anual</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="c in painel.maioresClientes" :key="c.id"
                class="cursor-pointer border-b border-navy/5 last:border-0 hover:bg-mist"
                @click="router.push(`/app/clientes/${c.id}`)"
              >
                <td class="px-6 py-3.5 text-sm font-semibold text-navy">{{ c.nome }}</td>
                <td class="px-4 py-3.5 text-sm text-navy/65">{{ c.segmento || '—' }}</td>
                <td class="px-4 py-3.5 text-sm text-navy/65">{{ c.consultor || '—' }}</td>
                <td class="px-4 py-3.5 text-center"><span v-if="c.nivel" class="rounded-full bg-sky/10 px-2.5 py-1 text-xs font-bold text-ocean">{{ c.nivel }}</span></td>
                <td class="whitespace-nowrap px-6 py-3.5 text-right text-sm font-semibold text-navy">{{ moeda(c.faturamentoAnual) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
