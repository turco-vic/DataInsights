<script setup>
import { ref, computed } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import SemPlanilhas from '../../components/SemPlanilhas.vue';
import { usePlanilhasStore } from '../../stores/planilhasStore';
import { dataHora, moedaCompacta, porcentagem } from '../../utils/formato';

const router = useRouter();
const planilhas = usePlanilhasStore();
const p = computed(() => planilhas.ativa);

const tab = ref('resumo');
const tabs = [
  { id: 'resumo', label: 'Resumo' },
  { id: 'colunas', label: 'Colunas' },
  { id: 'graficos', label: 'Gráficos' },
];

// Vue não expõe window no template; a impressão do navegador também salva em PDF.
const imprimir = () => window.print();

const acento = ['bg-navy', 'bg-ocean', 'bg-sky'];

const faturamentoSegmento = computed(() => {
  const maximo = planilhas.faturamentoPorSegmento[0]?.[1] || 1;
  return planilhas.faturamentoPorSegmento.map(([nome, valor]) => ({ nome, valor, largura: (valor / maximo) * 100 }));
});

const niveis = computed(() => {
  const total = planilhas.clientes.length || 1;
  return planilhas.porNivel.map(([nivel, qtd]) => ({ nivel, qtd, pct: (qtd / total) * 100 }));
});
</script>

<template>
  <div>
    <SemPlanilhas v-if="!p" texto="O relatório do dataset aparece depois do primeiro upload." />

    <template v-else>
      <div class="mb-3 text-[13px] font-medium text-navy/50">
        <RouterLink to="/app/planilhas" class="font-semibold hover:text-ocean">Planilhas enviadas</RouterLink> / {{ p.nome }}
      </div>
      <div class="flex flex-wrap items-end justify-between gap-6">
        <div>
          <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">{{ p.nome }}</h1>
          <p class="mt-1.5 text-sm text-navy/60">
            Enviado por {{ p.autor }} · {{ dataHora(p.enviadaEm) }} · {{ p.totalLinhas }} linhas · {{ planilhas.colunas.length }} colunas
          </p>
        </div>
        <div class="flex gap-3">
          <button type="button" @click="imprimir" class="rounded-lg border border-navy/15 bg-white px-5 py-2.5 text-sm font-medium text-navy/70 transition hover:text-navy">Imprimir / PDF</button>
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
            <div v-for="(q, i) in planilhas.qualidade" :key="q.label">
              <div class="flex justify-between text-[13px] font-medium text-navy/70">{{ q.label }}<span class="font-bold text-navy">{{ porcentagem(q.pct) }}</span></div>
              <div class="mt-1.5 h-1.5 overflow-hidden rounded-full bg-navy/10"><div class="h-full" :class="acento[i] || 'bg-navy'" :style="{ width: q.pct + '%' }"></div></div>
            </div>
          </div>
        </div>
        <div class="rounded-xl border border-navy/10 bg-white p-6">
          <h2 class="mb-4 text-base font-bold tracking-tight text-navy">Insights detectados</h2>
          <div class="flex flex-col gap-4">
            <div v-for="(texto, i) in planilhas.insights" :key="texto" class="flex gap-3">
              <span class="w-1 flex-none rounded-full" :class="acento[i % acento.length]"></span>
              <p class="text-sm leading-relaxed text-navy/70">{{ texto }}</p>
            </div>
          </div>
        </div>
        <div v-if="p.erros.length" class="rounded-xl border border-amber-500/30 bg-amber-50 px-6 py-5 lg:col-span-2">
          <h2 class="text-sm font-bold text-amber-800">{{ p.erros.length }} inconsistência(s) encontrada(s) no upload</h2>
          <ul class="mt-2 space-y-1 text-[13px] leading-relaxed text-amber-900/80">
            <li v-for="(erro, i) in p.erros" :key="i">· {{ erro }}</li>
          </ul>
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
              <tr v-for="c in planilhas.colunas" :key="c.nome" class="border-b border-navy/5 last:border-0">
                <td class="px-6 py-3.5 text-sm font-semibold text-navy">{{ c.nome }}</td>
                <td class="px-4 py-3.5"><span class="rounded-md bg-sky/10 px-2.5 py-1 text-xs font-semibold text-ocean">{{ c.tipo }}</span></td>
                <td class="px-4 py-3.5">
                  <div class="flex items-center gap-2.5">
                    <div class="h-1.5 flex-1 overflow-hidden rounded-full bg-navy/10"><div class="h-full bg-ocean" :style="{ width: c.preench + '%' }"></div></div>
                    <span class="text-xs font-bold text-navy">{{ porcentagem(c.preench) }}</span>
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
          <h2 class="mb-5 text-base font-bold tracking-tight text-navy">Faturamento por segmento</h2>
          <div class="flex flex-col gap-3">
            <div v-for="s in faturamentoSegmento" :key="s.nome">
              <div class="flex justify-between text-[13px] font-medium text-navy/70">{{ s.nome }}<span class="font-bold text-navy">{{ moedaCompacta(s.valor) }}</span></div>
              <div class="mt-1.5 h-1.5 overflow-hidden rounded-full bg-navy/10"><div class="h-full bg-ocean" :style="{ width: s.largura + '%' }"></div></div>
            </div>
          </div>
        </div>
        <div class="rounded-xl border border-navy/10 bg-white p-6">
          <h2 class="mb-5 text-base font-bold tracking-tight text-navy">Distribuição por nível</h2>
          <div class="flex h-38 items-end gap-6 px-4">
            <div v-for="(n, i) in niveis" :key="n.nivel" class="flex h-full flex-1 flex-col justify-end text-center">
              <div class="mb-1 text-[12px] font-bold text-navy">{{ n.qtd }}</div>
              <div class="rounded-t" :class="acento[i]" :style="{ height: n.pct + '%' }"></div>
              <div class="mt-2 text-[12px] font-semibold text-navy/50">Nível {{ n.nivel }}</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
