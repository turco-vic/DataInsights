<script setup>
import { computed } from 'vue';
import { usePlanilhasStore } from '../../stores/planilhasStore';
import SemPlanilhas from '../../components/SemPlanilhas.vue';
import { moeda, moedaCompacta, dataHora } from '../../utils/formato';

// Tudo aqui vem da planilha ativa guardada no planilhasStore — nenhum dado fixo.
const planilhas = usePlanilhasStore();
const p = computed(() => planilhas.ativa);

const kpis = computed(() => {
  const total = planilhas.clientes.length;
  const nivelA = planilhas.porNivel[0][1];
  const duplicadas = p.value.totalLinhas - total;
  return [
    { label: 'Clientes', value: total, sub: duplicadas ? `${duplicadas} duplicado(s) removido(s)` : `${p.value.totalLinhas} linhas lidas` },
    { label: 'Faturamento anual', value: moedaCompacta(planilhas.faturamentoTotal), sub: `média de ${moedaCompacta(planilhas.faturamentoMedio)} por cliente` },
    { label: 'Clientes nível A', value: nivelA, sub: `${total ? Math.round((nivelA / total) * 100) : 0}% da carteira` },
    { label: 'Inconsistências', value: p.value.erros.length, sub: `em ${p.value.linhasComErro} linha(s)`, alerta: p.value.erros.length > 0 },
  ];
});

const meses = computed(() => {
  const lista = planilhas.contratacoesPorMes;
  const maximo = Math.max(1, ...lista.map((m) => m.quantidade));
  return lista.map((m) => ({ ...m, altura: (m.quantidade / maximo) * 100 }));
});

// Três maiores segmentos + "Outros", para o donut continuar legível.
const CORES = ['var(--color-navy)', 'var(--color-ocean)', 'var(--color-sky)', 'color-mix(in srgb, var(--color-navy) 14%, transparent)'];
const segmentos = computed(() => {
  const total = planilhas.clientes.length || 1;
  const lista = planilhas.porSegmento;
  const principais = lista.slice(0, 3).map(([nome, qtd]) => ({ nome, qtd }));
  const outros = lista.slice(3).reduce((t, [, qtd]) => t + qtd, 0);
  if (outros) principais.push({ nome: 'Outros', qtd: outros });
  return principais.map((s, i) => ({ ...s, pct: Math.round((s.qtd / total) * 100), cor: CORES[i] }));
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
  const maximo = planilhas.servicos[0]?.[1] || 1;
  return planilhas.servicos.map(([nome, qtd]) => ({ nome, qtd, largura: (qtd / maximo) * 100 }));
});

function barCor(i, total) {
  if (i === total - 1) return 'bg-navy';
  if (i === total - 2) return 'bg-ocean';
  return 'bg-navy/20';
}
</script>

<template>
  <div>
    <div class="mb-6 flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Dashboard</h1>
        <p v-if="p" class="mt-1.5 text-sm text-navy/60">Gerado a partir de {{ p.nome }} · enviada em {{ dataHora(p.enviadaEm) }}</p>
      </div>
      <select
        v-if="planilhas.planilhas.length > 1"
        :value="p.id" @change="planilhas.selecionar($event.target.value)"
        class="rounded-lg border border-navy/15 bg-white px-3.5 py-2 text-[13px] font-semibold text-navy outline-none focus:border-sky"
      >
        <option v-for="item in planilhas.planilhas" :key="item.id" :value="item.id">{{ item.nome }}</option>
      </select>
    </div>

    <SemPlanilhas v-if="!p" texto="O dashboard é montado a partir da planilha que você enviar." />

    <template v-else>
      <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <div v-for="k in kpis" :key="k.label" class="rounded-xl border border-navy/10 bg-white px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">{{ k.label }}</div>
          <div class="mt-2 text-3xl font-bold tracking-tight" :class="k.alerta ? 'text-amber-700' : 'text-navy'">{{ k.value }}</div>
          <div class="mt-1.5 text-[13px] font-medium text-navy/55">{{ k.sub }}</div>
        </div>
      </div>

      <div class="mt-4 grid gap-4 lg:grid-cols-2">
        <div class="min-w-0 rounded-xl border border-navy/10 bg-white px-6 pb-5 pt-5 lg:col-span-2">
          <div class="mb-5 flex items-baseline justify-between gap-4">
            <h2 class="text-base font-bold tracking-tight text-navy">Contratações por mês</h2>
            <span class="text-xs text-navy/50">quantidade de clientes</span>
          </div>
          <div v-if="meses.length" class="overflow-x-auto">
            <div class="min-w-[480px]">
              <div class="flex h-42 items-end gap-2">
                <div v-for="(m, i) in meses" :key="m.rotulo" class="flex h-full flex-1 flex-col justify-end">
                  <div class="mb-1 text-center text-[11px] font-bold text-navy/60">{{ m.quantidade || '' }}</div>
                  <div class="rounded-t" :class="barCor(i, meses.length)" :style="{ height: m.altura + '%' }"></div>
                </div>
              </div>
              <div class="mt-2.5 flex gap-2">
                <div v-for="m in meses" :key="m.rotulo" class="flex-1 text-center text-[10px] font-semibold text-navy/40">{{ m.rotulo }}</div>
              </div>
            </div>
          </div>
          <p v-else class="text-sm text-navy/50">A planilha não tem datas de contratação válidas.</p>
        </div>

        <div class="min-w-0 rounded-xl border border-navy/10 bg-white p-6">
          <h2 class="mb-5 text-base font-bold tracking-tight text-navy">Clientes por segmento</h2>
          <div class="flex flex-wrap items-center gap-6">
            <div class="flex h-33 w-33 flex-none items-center justify-center rounded-full" :style="{ background: donut }">
              <div class="flex h-20 w-20 flex-col items-center justify-center rounded-full bg-white">
                <div class="text-lg font-bold tracking-tight text-navy">{{ planilhas.clientes.length }}</div>
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
          <div class="flex flex-col gap-3">
            <div v-for="s in servicos" :key="s.nome">
              <div class="flex justify-between text-[13px] font-medium text-navy/70">{{ s.nome }}<span class="font-bold text-navy">{{ s.qtd }}</span></div>
              <div class="mt-1.5 h-1.5 overflow-hidden rounded-full bg-navy/10"><div class="h-full bg-ocean" :style="{ width: s.largura + '%' }"></div></div>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-4 overflow-hidden rounded-xl border border-navy/10 bg-white">
        <div class="border-b border-navy/10 px-6 py-4">
          <h2 class="text-base font-bold tracking-tight text-navy">Maiores clientes por faturamento</h2>
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
              <tr v-for="c in planilhas.maioresClientes" :key="c.codigo_cliente" class="border-b border-navy/5 last:border-0 hover:bg-mist">
                <td class="px-6 py-3.5 text-sm font-semibold text-navy">{{ c.nome_cliente }}</td>
                <td class="px-4 py-3.5 text-sm text-navy/65">{{ c.segmento }}</td>
                <td class="px-4 py-3.5 text-sm text-navy/65">{{ c.consultor || '—' }}</td>
                <td class="px-4 py-3.5 text-center"><span class="rounded-full bg-sky/10 px-2.5 py-1 text-xs font-bold text-ocean">{{ c.nivel_cliente }}</span></td>
                <td class="whitespace-nowrap px-6 py-3.5 text-right text-sm font-semibold text-navy">{{ moeda(c.faturamento_anual) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
