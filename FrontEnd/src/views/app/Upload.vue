<script setup>
import { computed, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import Icon from '../../components/Icon.vue';
import { useUploadStore, COLUNAS_MODELO } from '../../stores/uploadStore';
import { useAuthStore } from '../../stores/authStore';

const router = useRouter();
const upload = useUploadStore();
const auth = useAuthStore();
const inputArquivo = ref(null);

const tratar = computed(() => auth.usuario?.prefs?.tratarDados !== false);

const etapas = computed(() => [
  {
    t: '1 · Leitura',
    d: tratar.value
      ? 'Cabeçalho conferido, espaços extras removidos e textos padronizados.'
      : 'Cabeçalho conferido. O tratamento automático está desligado: divergências viram erros.',
    c: 'text-navy',
  },
  { t: '2 · Validação', d: 'Campos vazios, e-mails, duplicados e formatos verificados.', c: 'text-ocean' },
  { t: '3 · Relatório', d: 'Você revisa os problemas e confirma o envio.', c: 'text-sky' },
]);

function abrirSeletor() {
  inputArquivo.value?.click();
}

async function processar(arquivo) {
  upload.selecionarArquivo(arquivo);
  await upload.processarPlanilha({ tratar: tratar.value });
  if (inputArquivo.value) inputArquivo.value.value = '';
  if (upload.temDados) router.push('/app/relatorio');
}

function aoEscolher(evento) {
  const arquivo = evento.target.files?.[0];
  if (arquivo) processar(arquivo);
}

function aoSoltar(evento) {
  const arquivo = evento.dataTransfer.files?.[0];
  if (arquivo) processar(arquivo);
}
</script>

<template>
  <div class="max-w-[860px]">
    <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Upload de planilha</h1>
    <p class="mt-1.5 text-sm text-navy/60">Envie um arquivo Excel ou CSV. Ele é validado antes de entrar no sistema.</p>

    <div
      v-if="upload.temDados"
      class="mt-6 flex flex-wrap items-center justify-between gap-4 rounded-xl border border-sky/40 bg-sky/5 px-5 py-4"
    >
      <p class="text-sm text-navy/70">
        A validação de <span class="font-semibold text-navy">{{ upload.nomeArquivo }}</span> ainda não foi confirmada.
      </p>
      <RouterLink to="/app/relatorio" class="text-sm font-semibold text-ocean hover:text-navy">Ver relatório →</RouterLink>
    </div>

    <input ref="inputArquivo" type="file" accept=".xlsx,.xls,.csv" class="hidden" @change="aoEscolher" />

    <div
      class="mt-6 rounded-2xl border-2 border-dashed border-sky/50 bg-white px-8 py-14 text-center transition hover:border-sky"
      @dragover.prevent @drop.prevent="aoSoltar"
    >
      <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-sky/10 text-ocean">
        <Icon name="upload" class="h-6 w-6" />
      </div>
      <h2 class="mt-5 text-xl font-bold tracking-tight text-navy">Arraste a planilha para cá</h2>
      <p class="mx-auto mt-2 max-w-100 text-sm leading-relaxed text-navy/60">Formatos aceitos: .xlsx, .xls e .csv, até 50 MB por arquivo.</p>
      <button
        type="button" @click="abrirSeletor" :disabled="upload.carregando"
        class="mt-6 rounded-lg bg-ocean px-7 py-3 text-sm font-semibold text-white transition hover:bg-navy disabled:opacity-60"
      >{{ upload.carregando ? 'Validando arquivo…' : 'Selecionar arquivo' }}</button>
    </div>

    <div v-if="upload.erroArquivo" class="mt-4 rounded-xl border border-red-500/30 bg-red-50 px-5 py-4 text-[13px] font-semibold text-red-800">
      {{ upload.erroArquivo }}
    </div>

    <div class="mt-4 rounded-xl border border-navy/10 bg-white px-5 py-4">
      <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Colunas esperadas no cabeçalho</div>
      <div class="mt-3 flex flex-wrap gap-2">
        <code v-for="coluna in COLUNAS_MODELO" :key="coluna" class="rounded-md bg-mist px-2.5 py-1 text-[12px] text-navy/75">{{ coluna }}</code>
      </div>
      <p class="mt-3 text-[12px] text-navy/50">cidade e uf são opcionais; as demais precisam estar preenchidas em todas as linhas.</p>
    </div>

    <div class="mt-4 grid gap-4 sm:grid-cols-3">
      <div v-for="e in etapas" :key="e.t" class="rounded-xl border border-navy/10 bg-white px-5 py-4">
        <div class="text-[13px] font-bold" :class="e.c">{{ e.t }}</div>
        <p class="mt-2 text-[13px] leading-relaxed text-navy/60">{{ e.d }}</p>
      </div>
    </div>
  </div>
</template>
