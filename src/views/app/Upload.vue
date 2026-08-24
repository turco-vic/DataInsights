<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const enviado = ref(false);
const etapas = [
  { t: '1 · Leitura', d: 'Cabeçalhos e tipos são detectados automaticamente.', c: 'text-navy' },
  { t: '2 · Tratamento', d: 'Duplicidades removidas e formatos padronizados.', c: 'text-ocean' },
  { t: '3 · Dashboard', d: 'Gráficos e indicadores prontos para explorar.', c: 'text-sky' },
];
const resumo = [
  { label: 'LINHAS VÁLIDAS', v: '14.796', c: 'text-navy' },
  { label: 'DUPLICADAS', v: '18', c: 'text-ocean' },
  { label: 'CAMPOS VAZIOS', v: '6', c: 'text-navy' },
];
</script>

<template>
  <div class="max-w-[860px]">
    <h1 class="text-3xl font-extrabold tracking-[-0.03em] text-navy">Upload de planilha</h1>
    <p class="mt-2 text-[15px] text-navy/60">Envie um arquivo Excel ou CSV. O tratamento dos dados é automático.</p>

    <div v-if="!enviado" class="mt-[26px] rounded-[18px] border-2 border-dashed border-sky/50 bg-white px-8 py-14 text-center">
      <div class="mx-auto flex h-[54px] w-[54px] items-center justify-center rounded-[14px] bg-sky/10 text-[22px] font-extrabold text-ocean">↑</div>
      <h2 class="mt-[22px] text-[21px] font-extrabold tracking-tight text-navy">Arraste a planilha para cá</h2>
      <p class="mx-auto mt-2.5 max-w-[400px] text-[15px] leading-relaxed text-navy/60">Formatos aceitos: .xlsx, .xls e .csv, até 50 MB por arquivo.</p>
      <button type="button" @click="enviado = true" class="mt-6 rounded-full bg-ocean px-[30px] py-3.5 text-[15px] font-bold text-white transition hover:bg-sky">Selecionar arquivo</button>
    </div>

    <div v-else class="mt-[26px] rounded-[18px] border border-navy/10 bg-white px-7 py-[26px]">
      <div class="flex flex-wrap items-center justify-between gap-5">
        <div class="flex items-center gap-3.5">
          <div class="flex h-[42px] w-[42px] items-center justify-center rounded-[11px] bg-green-700/10 text-lg font-extrabold text-green-700">✓</div>
          <div>
            <div class="text-[15.5px] font-bold text-navy">Faturamento_2026_Q1.xlsx</div>
            <div class="text-[13px] text-navy/55">14.820 linhas · 12 colunas · tratado em 11s</div>
          </div>
        </div>
        <div class="flex gap-3">
          <button type="button" @click="enviado = false" class="rounded-full border border-navy/15 px-5 py-[11px] text-sm font-semibold text-navy/65 hover:text-navy">Enviar outro</button>
          <button type="button" @click="router.push('/app')" class="rounded-full bg-ocean px-[22px] py-[11px] text-sm font-bold text-white hover:bg-sky">Ver dashboard</button>
        </div>
      </div>
      <div class="mt-6 grid gap-px overflow-hidden rounded-xl bg-navy/10 sm:grid-cols-3">
        <div v-for="r in resumo" :key="r.label" class="bg-mist px-[18px] py-4">
          <div class="text-[11px] font-bold tracking-[0.06em] text-navy/50">{{ r.label }}</div>
          <div class="mt-1 text-xl font-extrabold" :class="r.c">{{ r.v }}</div>
        </div>
      </div>
    </div>

    <div class="mt-4 grid gap-4 sm:grid-cols-3">
      <div v-for="e in etapas" :key="e.t" class="rounded-2xl border border-navy/10 bg-white px-[22px] py-5">
        <div class="text-[13px] font-extrabold" :class="e.c">{{ e.t }}</div>
        <p class="mt-2 text-[13.5px] leading-relaxed text-navy/60">{{ e.d }}</p>
      </div>
    </div>
  </div>
</template>
