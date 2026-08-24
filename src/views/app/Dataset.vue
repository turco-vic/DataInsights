<script setup>
import { ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { colunas, bars, linePoints, areaPoints } from '../../data/mock';

const router = useRouter();
const tab = ref('resumo');
const tabs = [
  { id: 'resumo', label: 'Resumo' },
  { id: 'colunas', label: 'Colunas' },
  { id: 'graficos', label: 'Gráficos' },
];
const qualidade = [
  { label: 'Completude', v: '98,4%', w: '98.4%', c: 'bg-navy' },
  { label: 'Consistência de tipos', v: '96,1%', w: '96.1%', c: 'bg-ocean' },
  { label: 'Registros únicos', v: '99,9%', w: '99.9%', c: 'bg-sky' },
];
const insights = [
  { c: 'bg-navy', t: 'Novembro e dezembro concentram 21% do faturamento do período.' },
  { c: 'bg-ocean', t: 'A unidade Comercial cresce 14% enquanto Indústria recua 3%.' },
  { c: 'bg-sky', t: '12,6% dos registros não têm desconto preenchido, o que pode distorcer a margem.' },
];
</script>

<template>
  <div>
    <div class="mb-3 text-[13px] font-semibold text-navy/50">
      <RouterLink to="/app/planilhas" class="font-bold">Planilhas enviadas</RouterLink> / Faturamento_2026_Q1.xlsx
    </div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-3xl font-extrabold tracking-[-0.03em] text-navy">Faturamento_2026_Q1.xlsx</h1>
        <p class="mt-2 text-[15px] text-navy/60">Enviado por Ana Ribeiro · 18/08/2026 · 14.820 linhas · 12 colunas</p>
      </div>
      <div class="flex gap-3">
        <button type="button" class="rounded-full border border-navy/15 bg-white px-5 py-[11px] text-sm font-semibold text-navy/70 hover:text-navy">Exportar PDF</button>
        <button type="button" @click="router.push('/app')" class="rounded-full bg-ocean px-[22px] py-[11px] text-sm font-bold text-white hover:bg-sky">Abrir dashboard</button>
      </div>
    </div>

    <div class="my-6 flex gap-[26px] border-b border-navy/10">
      <button
        v-for="t in tabs" :key="t.id" type="button" @click="tab = t.id"
        class="border-b-2 px-0.5 pb-3 text-[14.5px] font-bold"
        :class="tab === t.id ? 'border-navy text-navy' : 'border-transparent text-navy/50'"
      >{{ t.label }}</button>
    </div>

    <div v-if="tab === 'resumo'" class="grid gap-4 lg:grid-cols-2">
      <div class="rounded-2xl border border-navy/10 bg-white p-[26px]">
        <h2 class="mb-4 text-base font-extrabold tracking-tight text-navy">Qualidade dos dados</h2>
        <div class="flex flex-col gap-3.5">
          <div v-for="q in qualidade" :key="q.label">
            <div class="flex justify-between text-[13.5px] font-semibold text-navy/70">{{ q.label }}<span class="font-bold text-navy">{{ q.v }}</span></div>
            <div class="mt-[7px] h-1.5 overflow-hidden rounded-full bg-navy/10"><div class="h-full" :class="q.c" :style="{ width: q.w }"></div></div>
          </div>
        </div>
      </div>
      <div class="rounded-2xl border border-navy/10 bg-white p-[26px]">
        <h2 class="mb-4 text-base font-extrabold tracking-tight text-navy">Insights detectados</h2>
        <div class="flex flex-col gap-4">
          <div v-for="i in insights" :key="i.t" class="flex gap-3">
            <span class="w-1.5 flex-none rounded-full" :class="i.c"></span>
            <p class="text-sm leading-relaxed text-navy/70">{{ i.t }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="tab === 'colunas'" class="overflow-hidden rounded-2xl border border-navy/10 bg-white">
      <div class="overflow-x-auto"><table class="w-full min-w-[720px] border-collapse">
        <thead>
          <tr class="border-b border-navy/10 text-[11.5px] font-bold tracking-[0.06em] text-navy/50">
            <th class="px-6 py-3.5 text-left">COLUNA</th>
            <th class="px-4 py-3.5 text-left">TIPO</th>
            <th class="w-[220px] px-4 py-3.5 text-left">PREENCHIMENTO</th>
            <th class="px-6 py-3.5 text-left">EXEMPLO</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in colunas" :key="c.nome" class="border-b border-navy/5">
            <td class="px-6 py-[15px] text-sm font-bold text-navy">{{ c.nome }}</td>
            <td class="px-4 py-[15px]"><span class="rounded-md bg-sky/10 px-2.5 py-1 text-xs font-bold text-ocean">{{ c.tipo }}</span></td>
            <td class="px-4 py-[15px]">
              <div class="flex items-center gap-2.5">
                <div class="h-1.5 flex-1 overflow-hidden rounded-full bg-navy/10"><div class="h-full bg-ocean" :style="{ width: c.w }"></div></div>
                <span class="text-[12.5px] font-bold text-navy">{{ c.preench }}</span>
              </div>
            </td>
            <td class="px-6 py-[15px] text-[13.5px] text-navy/60">{{ c.exemplo }}</td>
          </tr>
        </tbody>
      </table></div>
    </div>

    <div v-else class="grid gap-4 lg:grid-cols-2">
      <div class="rounded-2xl border border-navy/10 bg-white p-6">
        <h2 class="mb-5 text-base font-extrabold tracking-tight text-navy">Faturamento por mês</h2>
        <div class="flex h-[150px] items-end gap-2">
          <div v-for="b in bars" :key="b.mes" class="flex-1 rounded-t-[5px]" :class="b.color" :style="{ height: b.h + '%' }"></div>
        </div>
      </div>
      <div class="rounded-2xl border border-navy/10 bg-white p-6">
        <h2 class="mb-5 text-base font-extrabold tracking-tight text-navy">Tendência</h2>
        <svg viewBox="0 0 520 120" preserveAspectRatio="none" class="block h-[150px] w-full">
          <polygon :points="areaPoints" fill="rgba(46,145,201,0.14)" />
          <polyline :points="linePoints" fill="none" stroke="#004B85" stroke-width="3" stroke-linejoin="round" />
        </svg>
      </div>
    </div>
  </div>
</template>
