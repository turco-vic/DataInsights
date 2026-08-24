<script setup>
import { ref } from 'vue';
import { kpis, bars, linePoints, areaPoints, unidades, heat, clientes } from '../../data/mock';

const periodos = ['3 meses', '6 meses', '12 meses'];
const periodo = ref('12 meses');
</script>

<template>
  <div>
    <div class="mb-[26px] flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-3xl font-extrabold tracking-[-0.03em] text-navy">Dashboard</h1>
        <p class="mt-2 text-[15px] text-navy/60">Gerado a partir de Faturamento_2026_Q1.xlsx · atualizado há 12 minutos</p>
      </div>
      <div class="flex items-center gap-2">
        <button
          v-for="p in periodos" :key="p" type="button" @click="periodo = p"
          class="rounded-full border border-navy/15 px-4 py-2.5 text-[13px] font-bold"
          :class="periodo === p ? 'bg-navy text-white' : 'bg-white text-navy/70'"
        >{{ p }}</button>
      </div>
    </div>

    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <div v-for="k in kpis" :key="k.label" class="rounded-2xl border border-navy/10 bg-white px-[22px] py-5">
        <div class="text-[11.5px] font-bold tracking-[0.06em] text-navy/50">{{ k.label }}</div>
        <div class="mt-2 text-3xl font-extrabold tracking-[-0.035em]" :class="k.tone">{{ k.value }}</div>
        <div class="mt-1.5 text-[13px] font-bold" :class="k.deltaTone">{{ k.delta }}</div>
      </div>
    </div>

    <div class="mt-4 grid gap-4 lg:grid-cols-2">
      <div class="min-w-0 rounded-2xl border border-navy/10 bg-white px-6 pb-5 pt-[22px] lg:col-span-2">
        <div class="mb-[22px] flex items-baseline justify-between gap-4">
          <h2 class="text-base font-extrabold tracking-tight text-navy">Faturamento por mês</h2>
          <span class="text-[12.5px] text-navy/50">em R$ milhares</span>
        </div>
        <div class="flex h-[168px] items-end gap-2">
          <div v-for="b in bars" :key="b.mes" class="flex-1 rounded-t-[5px]" :class="b.color" :style="{ height: b.h + '%' }"></div>
        </div>
        <div class="mt-2.5 flex gap-2">
          <div v-for="b in bars" :key="b.mes" class="flex-1 text-center text-[10px] font-semibold text-navy/40">{{ b.mes }}</div>
        </div>
      </div>

      <div class="min-w-0 rounded-2xl border border-navy/10 bg-white px-6 pb-5 pt-[22px]">
        <h2 class="text-base font-extrabold tracking-tight text-navy">Tendência de receita</h2>
        <p class="mb-[18px] mt-1.5 text-[12.5px] text-navy/50">Média móvel de 3 meses</p>
        <svg viewBox="0 0 520 120" preserveAspectRatio="none" class="block h-[140px] w-full overflow-visible">
          <polygon :points="areaPoints" fill="rgba(46,145,201,0.14)" />
          <polyline :points="linePoints" fill="none" stroke="#004B85" stroke-width="3" stroke-linejoin="round" stroke-linecap="round" />
          <circle cx="520" cy="12" r="5" fill="#001044" />
        </svg>
      </div>

      <div class="min-w-0 rounded-2xl border border-navy/10 bg-white p-6">
        <h2 class="mb-5 text-base font-extrabold tracking-tight text-navy">Receita por unidade</h2>
        <div class="flex flex-wrap items-center gap-[26px]">
          <div
            class="flex h-[132px] w-[132px] flex-none items-center justify-center rounded-full"
            style="background: conic-gradient(#001044 0 46%, #004B85 46% 76%, #2E91C9 76% 92%, rgba(0,16,68,0.14) 92% 100%)"
          >
            <div class="flex h-[78px] w-[78px] flex-col items-center justify-center rounded-full bg-white">
              <div class="text-[15px] font-extrabold tracking-tight text-navy">R$ 4,8M</div>
              <div class="text-[10px] font-semibold text-navy/50">total</div>
            </div>
          </div>
          <div class="flex min-w-[140px] flex-1 flex-col gap-3">
            <div v-for="u in unidades" :key="u.nome" class="flex items-center gap-2.5">
              <span class="h-2.5 w-2.5 flex-none rounded" :class="u.cor"></span>
              <span class="flex-1 text-[13.5px] font-semibold text-navy/75">{{ u.nome }}</span>
              <span class="text-[13px] font-bold text-navy">{{ u.pct }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="min-w-0 rounded-2xl border border-navy/10 bg-white p-6">
        <h2 class="text-base font-extrabold tracking-tight text-navy">Volume de pedidos</h2>
        <p class="mb-[18px] mt-1.5 text-[12.5px] text-navy/50">Por dia da semana, últimas 5 semanas</p>
        <div class="grid grid-cols-7 gap-1.5">
          <div v-for="c in heat" :key="c.i" class="aspect-square rounded-[5px]" :style="{ background: c.bg }"></div>
        </div>
        <div class="mt-3.5 flex items-center gap-2 text-[11px] font-semibold text-navy/45">
          menos <span class="h-1.5 flex-1 rounded-full bg-gradient-to-r from-ocean/10 to-ocean"></span> mais
        </div>
      </div>
    </div>

    <div class="mt-4 overflow-hidden rounded-2xl border border-navy/10 bg-white">
      <div class="flex flex-wrap items-center justify-between gap-5 border-b border-navy/10 px-6 py-5">
        <h2 class="text-base font-extrabold tracking-tight text-navy">Maiores clientes</h2>
        <div class="flex items-center gap-2">
          <span class="rounded-full bg-sky/10 px-3 py-[7px] text-[12.5px] font-bold text-ocean">Todas as unidades</span>
          <span class="rounded-full border border-navy/15 px-3 py-[7px] text-[12.5px] font-semibold text-navy/60">Receita ↓</span>
        </div>
      </div>
      <div class="overflow-x-auto"><table class="w-full min-w-[720px] border-collapse">
        <thead>
          <tr class="border-b border-navy/10 text-[11.5px] font-bold tracking-[0.06em] text-navy/50">
            <th class="px-6 py-3 text-left">CLIENTE</th>
            <th class="px-4 py-3 text-left">UNIDADE</th>
            <th class="px-4 py-3 text-right">RECEITA</th>
            <th class="px-4 py-3 text-right">VARIAÇÃO</th>
            <th class="px-6 py-3 text-right">STATUS</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in clientes" :key="c.nome" class="border-b border-navy/5 hover:bg-mist">
            <td class="px-6 py-[15px] text-sm font-bold text-navy">{{ c.nome }}</td>
            <td class="px-4 py-[15px] text-sm text-navy/65">{{ c.unidade }}</td>
            <td class="whitespace-nowrap px-4 py-[15px] text-right text-sm font-semibold text-navy">{{ c.receita }}</td>
            <td class="whitespace-nowrap px-4 py-[15px] text-right text-[13.5px] font-bold" :class="c.up ? 'text-green-700' : 'text-red-700'">{{ c.var }}</td>
            <td class="px-6 py-[15px] text-right">
              <span class="whitespace-nowrap rounded-full px-2.5 py-[5px] text-xs font-bold" :class="c.badge">{{ c.status }}</span>
            </td>
          </tr>
        </tbody>
      </table></div>
    </div>
  </div>
</template>
