<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Icon from '../../components/Icon.vue';
import { filesService } from '../../services/files';

const router = useRouter();
const inputArquivo = ref(null);
const processando = ref(false);
const resumo = ref(null);

const etapas = [
  { t: '1 · Leitura', d: 'Cabeçalhos e tipos são detectados automaticamente.', c: 'text-navy' },
  { t: '2 · Tratamento', d: 'Duplicidades removidas e formatos padronizados.', c: 'text-ocean' },
  { t: '3 · Dashboard', d: 'Gráficos e indicadores prontos para explorar.', c: 'text-sky' },
];

function abrirSeletor() {
  inputArquivo.value?.click();
}

async function processar(arquivo) {
  processando.value = true;
  resumo.value = await filesService.enviar(arquivo);
  processando.value = false;
}

function aoEscolher(evento) {
  const arquivo = evento.target.files?.[0];
  if (arquivo) processar(arquivo);
}

function aoSoltar(evento) {
  const arquivo = evento.dataTransfer.files?.[0];
  if (arquivo) processar(arquivo);
}

function reenviar() {
  resumo.value = null;
  if (inputArquivo.value) inputArquivo.value.value = '';
}
</script>

<template>
  <div class="max-w-[860px]">
    <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Upload de planilha</h1>
    <p class="mt-1.5 text-sm text-navy/60">Envie um arquivo Excel ou CSV. O tratamento dos dados é automático.</p>

    <input ref="inputArquivo" type="file" accept=".xlsx,.xls,.csv" class="hidden" @change="aoEscolher" />

    <div
      v-if="!resumo"
      class="mt-6 rounded-2xl border-2 border-dashed border-sky/50 bg-white px-8 py-14 text-center transition hover:border-sky"
      @dragover.prevent @drop.prevent="aoSoltar"
    >
      <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-sky/10 text-ocean">
        <Icon name="upload" class="h-6 w-6" />
      </div>
      <h2 class="mt-5 text-xl font-bold tracking-tight text-navy">Arraste a planilha para cá</h2>
      <p class="mx-auto mt-2 max-w-100 text-sm leading-relaxed text-navy/60">Formatos aceitos: .xlsx, .xls e .csv, até 50 MB por arquivo.</p>
      <button
        type="button" @click="abrirSeletor" :disabled="processando"
        class="mt-6 rounded-lg bg-ocean px-7 py-3 text-sm font-semibold text-white transition hover:bg-navy disabled:opacity-60"
      >{{ processando ? 'Processando…' : 'Selecionar arquivo' }}</button>
    </div>

    <div v-else class="mt-6 rounded-2xl border border-navy/10 bg-white px-7 py-6">
      <div class="flex flex-wrap items-center justify-between gap-5">
        <div class="flex items-center gap-3.5">
          <div class="flex h-11 w-11 items-center justify-center rounded-xl bg-emerald-600/10 text-emerald-700">
            <Icon name="check" class="h-5 w-5" />
          </div>
          <div>
            <div class="text-[15px] font-semibold text-navy">{{ resumo.arquivo }}</div>
            <div class="text-[13px] text-navy/55">{{ resumo.linhas }} linhas · {{ resumo.colunas }} colunas · tratado em {{ resumo.tempo }}</div>
          </div>
        </div>
        <div class="flex gap-3">
          <button type="button" @click="reenviar" class="rounded-lg border border-navy/15 px-5 py-2.5 text-sm font-medium text-navy/65 transition hover:text-navy">Enviar outro</button>
          <button type="button" @click="router.push('/app')" class="rounded-lg bg-ocean px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-navy">Ver dashboard</button>
        </div>
      </div>
      <div class="mt-6 grid gap-px overflow-hidden rounded-xl bg-navy/10 sm:grid-cols-3">
        <div v-for="r in resumo.metrica" :key="r.label" class="bg-mist px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">{{ r.label }}</div>
          <div class="mt-1 text-xl font-bold text-navy">{{ r.valor }}</div>
        </div>
      </div>
    </div>

    <div class="mt-4 grid gap-4 sm:grid-cols-3">
      <div v-for="e in etapas" :key="e.t" class="rounded-xl border border-navy/10 bg-white px-5 py-4">
        <div class="text-[13px] font-bold" :class="e.c">{{ e.t }}</div>
        <p class="mt-2 text-[13px] leading-relaxed text-navy/60">{{ e.d }}</p>
      </div>
    </div>
  </div>
</template>
