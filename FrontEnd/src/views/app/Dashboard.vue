<script setup>
import { ref, computed, onMounted } from 'vue';
import StatusBadge from '../../components/StatusBadge.vue';
import Icon from '../../components/Icon.vue';
import { dashboardService } from '../../services/dashboard';

const periodos = ['3 meses', '6 meses', '12 meses'];
const periodo = ref('12 meses');

const kpis = ref([]);
const bars = ref([]);
const unidades = ref([]);
const heat = ref([]);
const clientes = ref([]);
const linePoints = ref('');
const areaPoints = ref('');

onMounted(async () => {
  const d = await dashboardService.getVisaoGeral();
  kpis.value = d.kpis;
  bars.value = d.bars;
  unidades.value = d.unidades;
  heat.value = d.heat;
  clientes.value = d.clientes;
  linePoints.value = d.linePoints;
  areaPoints.value = d.areaPoints;
});

const deltaTone = { up: 'text-emerald-700', down: 'text-red-700', neutral: 'text-navy/55' };
const unidadeCores = ['bg-navy', 'bg-ocean', 'bg-sky', 'bg-navy/15'];

function barCor(i, total) {
  if (i === total - 1) return 'bg-navy';
  if (i === total - 2) return 'bg-ocean';
  return 'bg-navy/20';
}
function heatBg(n) {
  return `color-mix(in srgb, var(--color-ocean) ${Math.round((0.08 + n * 0.92) * 100)}%, transparent)`;
}

const donut = computed(() => {
  const cores = ['var(--color-navy)', 'var(--color-ocean)', 'var(--color-sky)', 'color-mix(in srgb, var(--color-navy) 14%, transparent)'];
  let acc = 0;
  const segs = unidades.value.map((u, i) => {
    const inicio = acc;
    acc += u.pct;
    return `${cores[i] || cores[3]} ${inicio}% ${acc}%`;
  });
  return `conic-gradient(${segs.join(', ')})`;
});
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Dashboard</h1>
        <p class="mt-1.5 text-sm text-navy/60">Gerado a partir de Faturamento_2026_Q1.xlsx · atualizado há 12 minutos</p>
      </div>
      <div class="flex items-center gap-1.5 rounded-lg border border-navy/15 bg-white p-1">
        <button
          v-for="p in periodos" :key="p" type="button" @click="periodo = p"
          class="rounded-md px-3.5 py-1.5 text-[13px] font-semibold transition"
          :class="periodo === p ? 'bg-navy text-white' : 'text-navy/60 hover:text-navy'"
        >{{ p }}</button>
      </div>
    </div>

    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <div v-for="k in kpis" :key="k.label" class="rounded-xl border border-navy/10 bg-white px-5 py-4">
        <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">{{ k.label }}</div>
        <div class="mt-2 text-3xl font-bold tracking-tight text-navy">{{ k.value }}</div>
        <div class="mt-1.5 flex items-center gap-1 text-[13px] font-semibold" :class="deltaTone[k.trend]">
          <Icon v-if="k.trend !== 'neutral'" :name="k.trend === 'up' ? 'trend-up' : 'trend-down'" class="h-3.5 w-3.5" />
          {{ k.delta }}
        </div>
      </div>
    </div>

    <div class="mt-4 grid gap-4 lg:grid-cols-2">
      <div class="min-w-0 rounded-xl border border-navy/10 bg-white px-6 pb-5 pt-5 lg:col-span-2">
        <div class="mb-5 flex items-baseline justify-between gap-4">
          <h2 class="text-base font-bold tracking-tight text-navy">Faturamento por mês</h2>
          <span class="text-xs text-navy/50">em R$ milhares</span>
        </div>
        <div class="flex h-42 items-end gap-2">
          <div v-for="(b, i) in bars" :key="b.mes" class="flex-1 rounded-t" :class="barCor(i, bars.length)" :style="{ height: b.valor + '%' }"></div>
        </div>
        <div class="mt-2.5 flex gap-2">
          <div v-for="b in bars" :key="b.mes" class="flex-1 text-center text-[10px] font-semibold text-navy/40">{{ b.mes }}</div>
        </div>
      </div>

      <div class="min-w-0 rounded-xl border border-navy/10 bg-white px-6 pb-5 pt-5">
        <h2 class="text-base font-bold tracking-tight text-navy">Tendência de receita</h2>
        <p class="mb-4 mt-1 text-xs text-navy/50">Média móvel de 3 meses</p>
        <svg viewBox="0 0 520 120" preserveAspectRatio="none" class="block h-36 w-full overflow-visible">
          <polygon :points="areaPoints" fill="color-mix(in srgb, var(--color-ocean) 14%, transparent)" />
          <polyline :points="linePoints" fill="none" stroke="var(--color-ocean)" stroke-width="3" stroke-linejoin="round" stroke-linecap="round" />
          <circle cx="520" cy="12" r="5" fill="var(--color-navy)" />
        </svg>
      </div>

      <div class="min-w-0 rounded-xl border border-navy/10 bg-white p-6">
        <h2 class="mb-5 text-base font-bold tracking-tight text-navy">Receita por unidade</h2>
        <div class="flex flex-wrap items-center gap-6">
          <div class="flex h-33 w-33 flex-none items-center justify-center rounded-full" :style="{ background: donut }">
            <div class="flex h-20 w-20 flex-col items-center justify-center rounded-full bg-white">
              <div class="text-sm font-bold tracking-tight text-navy">R$ 4,8M</div>
              <div class="text-[10px] font-semibold text-navy/50">total</div>
            </div>
          </div>
          <div class="flex min-w-35 flex-1 flex-col gap-3">
            <div v-for="(u, i) in unidades" :key="u.nome" class="flex items-center gap-2.5">
              <span class="h-2.5 w-2.5 flex-none rounded" :class="unidadeCores[i] || 'bg-navy/15'"></span>
              <span class="flex-1 text-sm font-medium text-navy/75">{{ u.nome }}</span>
              <span class="text-[13px] font-bold text-navy">{{ u.pct }}%</span>
            </div>
          </div>
        </div>
      </div>

      <div class="min-w-0 rounded-xl border border-navy/10 bg-white p-6">
        <h2 class="text-base font-bold tracking-tight text-navy">Volume de pedidos</h2>
        <p class="mb-4 mt-1 text-xs text-navy/50">Por dia da semana, últimas 5 semanas</p>
        <div class="grid grid-cols-7 gap-1.5">
          <div v-for="(n, i) in heat" :key="i" class="aspect-square rounded" :style="{ background: heatBg(n) }"></div>
        </div>
        <div class="mt-3.5 flex items-center gap-2 text-[11px] font-semibold text-navy/45">
          menos
          <span class="h-1.5 flex-1 rounded-full" style="background: linear-gradient(to right, color-mix(in srgb, var(--color-ocean) 10%, transparent), var(--color-ocean))"></span>
          mais
        </div>
      </div>
    </div>

    <div class="mt-4 overflow-hidden rounded-xl border border-navy/10 bg-white">
      <div class="flex flex-wrap items-center justify-between gap-5 border-b border-navy/10 px-6 py-4">
        <h2 class="text-base font-bold tracking-tight text-navy">Maiores clientes</h2>
        <div class="flex items-center gap-2">
          <span class="rounded-full bg-sky/10 px-3 py-1.5 text-xs font-semibold text-ocean">Todas as unidades</span>
          <span class="rounded-full border border-navy/15 px-3 py-1.5 text-xs font-medium text-navy/60">Receita ↓</span>
        </div>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full min-w-[720px] border-collapse">
          <thead>
            <tr class="border-b border-navy/10 text-[11px] font-semibold uppercase tracking-wide text-navy/50">
              <th class="px-6 py-3 text-left">Cliente</th>
              <th class="px-4 py-3 text-left">Unidade</th>
              <th class="px-4 py-3 text-right">Receita</th>
              <th class="px-4 py-3 text-right">Variação</th>
              <th class="px-6 py-3 text-right">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in clientes" :key="c.nome" class="border-b border-navy/5 last:border-0 hover:bg-mist">
              <td class="px-6 py-3.5 text-sm font-semibold text-navy">{{ c.nome }}</td>
              <td class="px-4 py-3.5 text-sm text-navy/65">{{ c.unidade }}</td>
              <td class="whitespace-nowrap px-4 py-3.5 text-right text-sm font-semibold text-navy">{{ c.receita }}</td>
              <td class="whitespace-nowrap px-4 py-3.5 text-right text-[13px] font-bold" :class="c.up ? 'text-emerald-700' : 'text-red-700'">{{ c.variacao }}</td>
              <td class="px-6 py-3.5 text-right"><StatusBadge :status="c.status" /></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
