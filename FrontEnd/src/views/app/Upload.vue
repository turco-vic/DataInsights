<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import Icon from '../../components/Icon.vue';
import { useUploadStore } from '../../stores/uploadStore';
import { usePlanilhasStore } from '../../stores/planilhasStore';
import { useAuthStore } from '../../stores/authStore';

const router = useRouter();
const upload = useUploadStore();
const planilhas = usePlanilhasStore();
const auth = useAuthStore();

// null = nada enviado ainda; true/false = conseguiu ou não guardar no navegador.
const salvo = ref(null);
const inputArquivo = ref(null);

// A prévia mostra só as primeiras linhas: uma planilha de 15 mil registros
// renderizada inteira trava o navegador sem acrescentar informação.
const LIMITE_PREVIA = 20;
const previa = computed(() => upload.dadosTratados.slice(0, LIMITE_PREVIA));

// Segmentos do maior para o menor: é a prova visual de que a padronização funcionou.
const segmentos = computed(() =>
  Object.entries(upload.porSegmento).sort((a, b) => b[1] - a[1]),
);

const formatarMoeda = (valor) =>
  valor === null
    ? '—'
    : valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 });

const formatarData = (valor) => (valor ? valor.toLocaleDateString('pt-BR') : '—');

const etapas = [
  { t: '1 · Leitura', d: 'Cabeçalhos e tipos são detectados automaticamente.', c: 'text-navy' },
  { t: '2 · Tratamento', d: 'Segmentos unificados e formatos padronizados.', c: 'text-ocean' },
  { t: '3 · Dashboard', d: 'Gráficos e indicadores prontos para explorar.', c: 'text-sky' },
];

function abrirSeletor() {
  inputArquivo.value?.click();
}

// A view só entrega o arquivo ao store: quem lê e trata os dados é o Pinia.
async function processar(arquivo) {
  salvo.value = null;
  upload.selecionarArquivo(arquivo);
  await upload.processarPlanilha();

  // Planilha lida: fica guardada e passa a alimentar Dashboard, Planilhas e Dataset.
  if (upload.temDados) {
    salvo.value = planilhas.salvar({
      nome: arquivo.name,
      autor: auth.nomeExibicao,
      dadosTratados: upload.dadosTratados,
      erros: upload.erros,
    });
  }
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
  upload.limpar();
  salvo.value = null;
  if (inputArquivo.value) inputArquivo.value.value = '';
}
</script>

<template>
  <div class="max-w-[860px]">
    <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Upload de planilha</h1>
    <p class="mt-1.5 text-sm text-navy/60">Envie um arquivo Excel ou CSV. O tratamento dos dados é automático.</p>

    <input ref="inputArquivo" type="file" accept=".xlsx,.xls,.csv" class="hidden" @change="aoEscolher" />

    <div
      v-if="!upload.temDados"
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
      >{{ upload.carregando ? 'Lendo arquivo…' : 'Selecionar arquivo' }}</button>
    </div>

    <div v-else class="mt-6 rounded-2xl border border-navy/10 bg-white px-7 py-6">
      <div class="flex flex-wrap items-center justify-between gap-5">
        <div class="flex items-center gap-3.5">
          <div class="flex h-11 w-11 items-center justify-center rounded-xl bg-emerald-600/10 text-emerald-700">
            <Icon name="check" class="h-5 w-5" />
          </div>
          <div>
            <div class="text-[15px] font-semibold text-navy">{{ upload.arquivo?.name }}</div>
            <div class="text-[13px] text-navy/55">
              {{ upload.totalClientes }} linhas lidas e padronizadas
              <span v-if="salvo" class="font-semibold text-emerald-700">· salva em Planilhas enviadas</span>
              <span v-else-if="salvo === false" class="font-semibold text-amber-700">· grande demais para guardar; disponível até recarregar</span>
            </div>
          </div>
        </div>
        <div class="flex gap-3">
          <button type="button" @click="reenviar" class="rounded-lg border border-navy/15 px-5 py-2.5 text-sm font-medium text-navy/65 transition hover:text-navy">Enviar outro</button>
          <button type="button" @click="router.push('/app')" class="rounded-lg bg-ocean px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-navy">Ver dashboard</button>
        </div>
      </div>
      <div class="mt-6 grid gap-px overflow-hidden rounded-xl bg-navy/10 sm:grid-cols-2 lg:grid-cols-4">
        <div class="bg-mist px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Clientes</div>
          <div class="mt-1 text-xl font-bold text-navy">{{ upload.totalClientes }}</div>
        </div>
        <div class="bg-mist px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Nível A</div>
          <div class="mt-1 text-xl font-bold text-navy">{{ upload.clientesNivelA }}</div>
        </div>
        <div class="bg-mist px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Faturamento</div>
          <div class="mt-1 text-xl font-bold text-navy">{{ formatarMoeda(upload.faturamentoTotal) }}</div>
        </div>
        <div class="bg-mist px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Inconsistências</div>
          <div class="mt-1 text-xl font-bold" :class="upload.totalErros ? 'text-amber-700' : 'text-navy'">{{ upload.totalErros }}</div>
        </div>
      </div>

      <div class="mt-5 border-t border-navy/10 pt-5">
        <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Clientes por segmento</div>
        <div class="mt-3 flex flex-wrap gap-2">
          <span
            v-for="[nome, quantidade] in segmentos" :key="nome"
            class="rounded-lg bg-sky/10 px-3 py-1.5 text-[13px] font-medium text-ocean"
          >{{ nome }} · {{ quantidade }}</span>
        </div>
      </div>
    </div>

    <div v-if="upload.totalErros" class="mt-4 rounded-xl border border-amber-500/30 bg-amber-50 px-5 py-4">
      <div class="text-[13px] font-bold text-amber-800">{{ upload.totalErros }} inconsistência(s) encontrada(s)</div>
      <ul class="mt-2 space-y-1 text-[13px] leading-relaxed text-amber-900/80">
        <li v-for="(erro, i) in upload.erros.slice(0, 10)" :key="i">· {{ erro }}</li>
        <li v-if="upload.totalErros > 10" class="text-amber-900/60">… e mais {{ upload.totalErros - 10 }}.</li>
      </ul>
    </div>

    <section v-if="upload.temDados" class="mt-4 rounded-2xl border border-navy/10 bg-white">
      <div class="flex items-center justify-between border-b border-navy/10 px-6 py-4">
        <h2 class="text-[15px] font-bold text-navy">Prévia dos clientes</h2>
        <span class="text-[13px] text-navy/55">mostrando {{ previa.length }} de {{ upload.totalClientes }}</span>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="border-b border-navy/10 text-xs uppercase tracking-wide text-navy/50">
            <tr>
              <th class="px-6 py-3 font-semibold">Código</th>
              <th class="px-6 py-3 font-semibold">Cliente</th>
              <th class="px-6 py-3 font-semibold">Consultor</th>
              <th class="px-6 py-3 font-semibold">Segmento</th>
              <th class="px-6 py-3 font-semibold">Nível</th>
              <th class="px-6 py-3 font-semibold">Faturamento</th>
              <th class="px-6 py-3 font-semibold">Contratação</th>
              <th class="px-6 py-3 font-semibold">Serviços</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(cliente, i) in previa" :key="i" class="border-b border-navy/5 last:border-0">
              <td class="px-6 py-3 text-navy/70">{{ cliente.codigo_cliente }}</td>
              <td class="px-6 py-3 font-medium text-navy">{{ cliente.nome_cliente }}</td>
              <td class="px-6 py-3 text-navy/70">{{ cliente.consultor || '—' }}</td>
              <td class="px-6 py-3 text-navy/70">{{ cliente.segmento }}</td>
              <td class="px-6 py-3 text-navy/70">{{ cliente.nivel_cliente }}</td>
              <td class="px-6 py-3 whitespace-nowrap text-navy/70">{{ formatarMoeda(cliente.faturamento_anual) }}</td>
              <td class="px-6 py-3 whitespace-nowrap text-navy/70">{{ formatarData(cliente.data_contratacao) }}</td>
              <td class="px-6 py-3 text-navy/70">{{ cliente.servicos_contratados.join(', ') || '—' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <div v-if="!upload.temDados" class="mt-4 grid gap-4 sm:grid-cols-3">
      <div v-for="e in etapas" :key="e.t" class="rounded-xl border border-navy/10 bg-white px-5 py-4">
        <div class="text-[13px] font-bold" :class="e.c">{{ e.t }}</div>
        <p class="mt-2 text-[13px] leading-relaxed text-navy/60">{{ e.d }}</p>
      </div>
    </div>
  </div>
</template>
