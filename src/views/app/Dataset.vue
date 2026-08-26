<script setup>
import { ref, onMounted } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { datasetService } from '../../services/dataset';

const router = useRouter();
const tab = ref('resumo');
const tabs = [
  { id: 'resumo', label: 'Resumo' },
  { id: 'colunas', label: 'Colunas' },
  { id: 'graficos', label: 'Gráficos' },
];

const colunas = ref([]);
const qualidade = ref([]);
const insights = ref([]);
const bars = ref([]);
const linePoints = ref('');
const areaPoints = ref('');

onMounted(async () => {
  const d = await datasetService.obter();
  colunas.value = d.colunas;
  qualidade.value = d.qualidade;
  insights.value = d.insights;
  bars.value = d.bars;
  linePoints.value = d.linePoints;
  areaPoints.value = d.areaPoints;
});

const acento = ['bg-navy', 'bg-ocean', 'bg-sky'];

function barCor(i, total) {
  if (i === total - 1) return 'bg-navy';
  if (i === total - 2) return 'bg-ocean';
  return 'bg-navy/20';
}

function exportarPdf() {
  // TODO: backend gera o PDF do relatório do dataset.
  alert('A exportação em PDF ficará disponível quando o backend estiver conectado.');
}
</script>

<template>
  <div>
    <div class="mb-3 text-[13px] font-medium text-navy/50">
      <RouterLink to="/app/planilhas" class="font-semibold hover:text-ocean">Planilhas enviadas</RouterLink> / Faturamento_2026_Q1.xlsx
    </div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Faturamento_2026_Q1.xlsx</h1>
        <p class="mt-1.5 text-sm text-navy/60">Enviado por Ana Ribeiro · 18/08/2026 · 14.820 linhas · 12 colunas</p>
      </div>
      <div class="flex gap-3">
        <button type="button" @click="exportarPdf" class="rounded-lg border border-navy/15 bg-white px-5 py-2.5 text-sm font-medium text-navy/70 transition hover:text-navy">Exportar PDF</button>
        <button type="button" @click="router.push('/app')" class="rounded-lg bg-ocean px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-navy">Abrir dashboard</button>
      </div>
    </div>

    <div class="my-6 flex gap-6 border-b border-navy/10">
      <button
        v-for="t in tabs" :key="t.id" type="button" @click="tab = t.id"
        class="-mb-px border-b-2 px-0.5 pb-3 text-sm font-semibold transition"
        :class="tab === t.id ? 'border-navy text-navy' : 'border-transparent text-navy/50 hover:text-navy/80'"
      >{{ t.label }}</button>
    </div>

    <div v-if="tab === 'resumo'" class="grid gap-4 lg:grid-cols-2">
      <div class="rounded-xl border border-navy/10 bg-white p-6">
        <h2 class="mb-4 text-base font-bold tracking-tight text-navy">Qualidade dos dados</h2>
        <div class="flex flex-col gap-3.5">
          <div v-for="(q, i) in qualidade" :key="q.label">
            <div class="flex justify-between text-[13px] font-medium text-navy/70">{{ q.label }}<span class="font-bold text-navy">{{ q.pct.toFixed(1).replace('.', ',') }}%</span></div>
            <div class="mt-1.5 h-1.5 overflow-hidden rounded-full bg-navy/10"><div class="h-full" :class="acento[i] || 'bg-navy'" :style="{ width: q.pct + '%' }"></div></div>
          </div>
        </div>
      </div>
      <div class="rounded-xl border border-navy/10 bg-white p-6">
        <h2 class="mb-4 text-base font-bold tracking-tight text-navy">Insights detectados</h2>
        <div class="flex flex-col gap-4">
          <div v-for="(texto, i) in insights" :key="texto" class="flex gap-3">
            <span class="w-1 flex-none rounded-full" :class="acento[i % acento.length]"></span>
            <p class="text-sm leading-relaxed text-navy/70">{{ texto }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="tab === 'colunas'" class="overflow-hidden rounded-xl border border-navy/10 bg-white">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[720px] border-collapse">
          <thead>
            <tr class="border-b border-navy/10 text-[11px] font-semibold uppercase tracking-wide text-navy/50">
              <th class="px-6 py-3.5 text-left">Coluna</th>
              <th class="px-4 py-3.5 text-left">Tipo</th>
              <th class="w-56 px-4 py-3.5 text-left">Preenchimento</th>
              <th class="px-6 py-3.5 text-left">Exemplo</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in colunas" :key="c.nome" class="border-b border-navy/5 last:border-0">
              <td class="px-6 py-3.5 text-sm font-semibold text-navy">{{ c.nome }}</td>
              <td class="px-4 py-3.5"><span class="rounded-md bg-sky/10 px-2.5 py-1 text-xs font-semibold text-ocean">{{ c.tipo }}</span></td>
              <td class="px-4 py-3.5">
                <div class="flex items-center gap-2.5">
                  <div class="h-1.5 flex-1 overflow-hidden rounded-full bg-navy/10"><div class="h-full bg-ocean" :style="{ width: c.preench + '%' }"></div></div>
                  <span class="text-xs font-bold text-navy">{{ c.preench.toFixed(1).replace('.', ',') }}%</span>
                </div>
              </td>
              <td class="px-6 py-3.5 text-[13px] text-navy/60">{{ c.exemplo }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-else class="grid gap-4 lg:grid-cols-2">
      <div class="rounded-xl border border-navy/10 bg-white p-6">
        <h2 class="mb-5 text-base font-bold tracking-tight text-navy">Faturamento por mês</h2>
        <div class="flex h-38 items-end gap-2">
          <div v-for="(b, i) in bars" :key="b.mes" class="flex-1 rounded-t" :class="barCor(i, bars.length)" :style="{ height: b.valor + '%' }"></div>
        </div>
      </div>
      <div class="rounded-xl border border-navy/10 bg-white p-6">
        <h2 class="mb-5 text-base font-bold tracking-tight text-navy">Tendência</h2>
        <svg viewBox="0 0 520 120" preserveAspectRatio="none" class="block h-38 w-full">
          <polygon :points="areaPoints" fill="color-mix(in srgb, var(--color-ocean) 14%, transparent)" />
          <polyline :points="linePoints" fill="none" stroke="var(--color-ocean)" stroke-width="3" stroke-linejoin="round" />
        </svg>
      </div>
    </div>
  </div>
</template>
