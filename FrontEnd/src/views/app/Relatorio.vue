<script setup>
import { ref, computed, watch } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import Icon from '../../components/Icon.vue';
import SemPlanilhas from '../../components/SemPlanilhas.vue';
import { useUploadStore, TIPOS_ERRO, COLUNAS_MODELO } from '../../stores/uploadStore';
import { usePlanilhasStore } from '../../stores/planilhasStore';
import { useAuthStore } from '../../stores/authStore';
import { useUiStore } from '../../stores/uiStore';
import { dataHora, porcentagem } from '../../utils/formato';

const router = useRouter();
const upload = useUploadStore();
const planilhas = usePlanilhasStore();
const auth = useAuthStore();
const ui = useUiStore();

const SITUACAO = {
  erro: { label: 'Erro', cls: 'bg-red-600/10 text-red-700' },
  corrigido: { label: 'Corrigido', cls: 'bg-sky/15 text-ocean' },
  ok: { label: 'OK', cls: 'bg-emerald-600/10 text-emerald-700' },
  valido: { label: 'Válidos', cls: 'bg-emerald-600/10 text-emerald-700' },
};

const resumo = computed(() => [
  ...upload.errosPorTipo
    // Coluna ausente e recusa do servidor só aparecem quando acontecem.
    .filter((e) => (e.tipo !== TIPOS_ERRO.COLUNA && e.tipo !== TIPOS_ERRO.SERVIDOR) || e.quantidade)
    .map((e) => ({ ...e, aba: 'erros', situacao: e.quantidade ? 'erro' : 'ok' })),
  ...upload.avisosPorTipo.map((a) => ({ ...a, aba: 'avisos', situacao: a.quantidade ? 'corrigido' : 'ok' })),
  { tipo: 'Registros válidos', quantidade: upload.registrosValidos, situacao: 'valido' },
]);

const LIMITE = 100;
const aba = ref(upload.totalErros || !upload.totalAvisos ? 'erros' : 'avisos');
const filtroTipo = ref(null);
const limite = ref(LIMITE);

const itensDaAba = computed(() => (aba.value === 'erros' ? upload.erros : upload.avisos));
const tiposDaAba = computed(() =>
  (aba.value === 'erros' ? upload.errosPorTipo : upload.avisosPorTipo).filter((t) => t.quantidade),
);
const filtrados = computed(() =>
  filtroTipo.value ? itensDaAba.value.filter((i) => i.tipo === filtroTipo.value) : itensDaAba.value,
);
const visiveis = computed(() => filtrados.value.slice(0, limite.value));

watch([aba, filtroTipo], () => {
  limite.value = LIMITE;
});

function trocarAba(nova) {
  aba.value = nova;
  filtroTipo.value = null;
}

function verTipo(item) {
  if (!item.aba || !item.quantidade) return;
  aba.value = item.aba;
  filtroTipo.value = item.tipo;
  document.getElementById('detalhes')?.scrollIntoView({ behavior: 'smooth' });
}

const envio = ref(null);

async function enviar() {
  const nome = upload.nomeArquivo;
  const enviados = upload.totalRegistros;

  // Se o servidor recusar, as inconsistências dele entram na lista de erros desta tela.
  if (!(await upload.importar())) {
    if (upload.totalErros) trocarAba('erros');
    return;
  }

  const { resumo } = upload.resultadoImportacao;
  upload.limpar();
  planilhas.carregarLista().then(() => planilhas.selecionar(resumo.id));

  if (auth.usuario?.prefs?.gerarDashboard) {
    ui.notificar(`${nome}: ${resumo.criados} cliente(s) novo(s), ${resumo.atualizados} atualizado(s).`);
    router.push('/app');
    return;
  }
  envio.value = { nome, enviados, resumo };
}

function corrigirEReenviar() {
  upload.limpar();
  router.push('/app/upload');
}

const linhaDoItem = (item) => (item.linha === 1 ? 'Cabeçalho' : item.linha);
</script>

<template>
  <div>
    <div v-if="envio" class="max-w-[760px]">
      <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Relatório de validação</h1>
      <div class="mt-6 rounded-2xl border border-navy/10 bg-white px-7 py-7">
        <div class="flex items-center gap-3.5">
          <div class="flex h-11 w-11 flex-none items-center justify-center rounded-xl bg-emerald-600/10 text-emerald-700">
            <Icon name="check" class="h-5 w-5" />
          </div>
          <div>
            <div class="text-[15px] font-semibold text-navy">{{ envio.nome }} enviada</div>
            <div class="text-[13px] text-navy/60">
              {{ envio.enviados }} registro(s) salvo(s) · planilha 100% consistente
            </div>
          </div>
        </div>

        <div class="mt-5 rounded-lg bg-emerald-50 px-4 py-3 text-[13px] text-emerald-800">
          {{ envio.resumo.criados }} cliente(s) criado(s) e {{ envio.resumo.atualizados }} atualizado(s) no banco. A planilha entrou no histórico da equipe.
        </div>

        <div class="mt-6 flex flex-wrap gap-3">
          <RouterLink to="/app/upload" class="rounded-lg border border-navy/15 px-5 py-2.5 text-sm font-medium text-navy/65 transition hover:text-navy">Enviar outra planilha</RouterLink>
          <RouterLink to="/app/dataset" class="rounded-lg border border-navy/15 px-5 py-2.5 text-sm font-medium text-navy/65 transition hover:text-navy">Relatório do dataset</RouterLink>
          <RouterLink to="/app" class="rounded-lg bg-ocean px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-navy">Ver dashboard</RouterLink>
        </div>
      </div>
    </div>

    <SemPlanilhas v-else-if="!upload.temDados" texto="Envie uma planilha para ver o relatório de validação." />

    <template v-else>
      <div class="mb-3 text-[13px] font-medium text-navy/50">
        <RouterLink to="/app/upload" class="font-semibold hover:text-ocean">Upload de planilha</RouterLink> / Relatório de validação
      </div>

      <div class="flex flex-wrap items-end justify-between gap-6">
        <div>
          <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Relatório de validação</h1>
          <p class="mt-1.5 text-sm text-navy/60">
            <span class="font-semibold text-navy">{{ upload.nomeArquivo }}</span> · validado em {{ dataHora(upload.dataValidacao) }}
          </p>
        </div>
        <div class="flex flex-wrap gap-3">
          <button
            type="button" @click="corrigirEReenviar"
            class="rounded-lg border border-navy/15 bg-white px-5 py-2.5 text-sm font-medium text-navy/70 transition hover:text-navy"
          >Corrigir e reenviar</button>
          <button
            type="button" @click="enviar" :disabled="!upload.podeEnviar || upload.importando"
            :title="upload.podeEnviar ? '' : 'Corrija todas as inconsistências para liberar o envio'"
            class="rounded-lg bg-ocean px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-navy disabled:cursor-not-allowed disabled:opacity-50"
          >{{ upload.importando ? 'Enviando…' : upload.podeEnviar ? `Enviar ${upload.totalRegistros} registro(s)` : 'Envio bloqueado' }}</button>
        </div>
      </div>

      <div v-if="upload.erroImportacao" role="alert" class="mt-6 rounded-xl border border-red-500/30 bg-red-50 px-6 py-4 text-[13px] font-semibold text-red-800">
        {{ upload.erroImportacao }}
      </div>

      <div v-if="upload.errosEstrutura.length" class="mt-6 rounded-xl border border-red-500/30 bg-red-50 px-6 py-5">
        <h2 class="text-sm font-bold text-red-800">A planilha não segue o modelo e não pode ser enviada</h2>
        <ul class="mt-2 space-y-1 text-[13px] leading-relaxed text-red-900/80">
          <li v-for="e in upload.errosEstrutura" :key="e.campo">· {{ e.descricao }}</li>
        </ul>
        <p class="mt-3 text-[13px] text-red-900/70">Colunas esperadas: {{ COLUNAS_MODELO.join(', ') }}.</p>
      </div>
      <div v-else-if="upload.totalErros" class="mt-6 rounded-xl border border-red-500/30 bg-red-50 px-6 py-5">
        <h2 class="text-sm font-bold text-red-800">
          A planilha tem {{ upload.totalErros }} inconsistência(s) em {{ upload.registrosComErro }} linha(s) e não pode ser enviada
        </h2>
        <p class="mt-1.5 text-[13px] leading-relaxed text-red-900/80">
          Só planilhas 100% consistentes entram no sistema. Corrija as linhas listadas abaixo no arquivo e envie de novo.
        </p>
      </div>

      <div class="mt-6 grid gap-px overflow-hidden rounded-xl border border-navy/10 bg-navy/10 sm:grid-cols-2 lg:grid-cols-4">
        <div class="bg-white px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Total de registros</div>
          <div class="mt-1 text-2xl font-bold text-navy">{{ upload.totalRegistros }}</div>
        </div>
        <div class="bg-white px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Registros válidos</div>
          <div class="mt-1 text-2xl font-bold text-emerald-700">{{ upload.registrosValidos }}</div>
        </div>
        <div class="bg-white px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Registros com erro</div>
          <div class="mt-1 text-2xl font-bold" :class="upload.registrosComErro ? 'text-red-700' : 'text-navy'">{{ upload.registrosComErro }}</div>
          <div class="text-[12px] text-navy/50">{{ upload.totalErros }} erro(s) no total</div>
        </div>
        <div class="bg-white px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Correções automáticas</div>
          <div class="mt-1 text-2xl font-bold text-ocean">{{ upload.totalAvisos }}</div>
          <div class="text-[12px] text-navy/50">não impedem o envio</div>
        </div>
      </div>

      <div class="mt-4 rounded-xl border border-navy/10 bg-white px-5 py-4">
        <div class="flex justify-between text-[13px] font-medium text-navy/70">
          Registros aprovados<span class="font-bold text-navy">{{ porcentagem(upload.percentualValidos) }}</span>
        </div>
        <div class="mt-2 h-2 overflow-hidden rounded-full bg-red-600/15">
          <div class="h-full rounded-full bg-emerald-600" :style="{ width: upload.percentualValidos + '%' }"></div>
        </div>
      </div>

      <section class="mt-4 overflow-hidden rounded-xl border border-navy/10 bg-white">
        <h2 class="border-b border-navy/10 px-6 py-4 text-[15px] font-bold text-navy">Resumo da validação</h2>
        <div class="overflow-x-auto">
          <table class="w-full min-w-[480px] text-left text-sm">
            <thead class="border-b border-navy/10 text-xs uppercase tracking-wide text-navy/50">
              <tr>
                <th class="px-6 py-3 font-semibold">Validação realizada</th>
                <th class="px-6 py-3 text-right font-semibold">Quantidade</th>
                <th class="px-6 py-3 font-semibold">Situação</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in resumo" :key="item.tipo" @click="verTipo(item)"
                class="border-b border-navy/5 last:border-0"
                :class="item.aba && item.quantidade ? 'cursor-pointer hover:bg-mist' : ''"
              >
                <td class="px-6 py-3 font-medium text-navy">{{ item.tipo }}</td>
                <td class="px-6 py-3 text-right font-bold text-navy">{{ item.quantidade }}</td>
                <td class="px-6 py-3">
                  <span class="rounded-md px-2.5 py-1 text-xs font-semibold" :class="SITUACAO[item.situacao].cls">{{ SITUACAO[item.situacao].label }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section id="detalhes" class="mt-4 scroll-mt-24 rounded-xl border border-navy/10 bg-white">
        <div class="flex flex-wrap items-center gap-6 border-b border-navy/10 px-6 pt-4">
          <button
            v-for="t in [{ id: 'erros', label: `Erros (${upload.totalErros})` }, { id: 'avisos', label: `Correções automáticas (${upload.totalAvisos})` }]"
            :key="t.id" type="button" @click="trocarAba(t.id)"
            class="-mb-px border-b-2 px-0.5 pb-3 text-sm font-semibold transition"
            :class="aba === t.id ? 'border-navy text-navy' : 'border-transparent text-navy/50 hover:text-navy/80'"
          >{{ t.label }}</button>
        </div>

        <div v-if="tiposDaAba.length > 1" class="flex flex-wrap gap-2 px-6 pt-4">
          <button
            type="button" @click="filtroTipo = null"
            class="rounded-full px-3 py-1 text-[12px] font-semibold transition"
            :class="!filtroTipo ? 'bg-navy text-white' : 'bg-mist text-navy/65 hover:text-navy'"
          >Todos</button>
          <button
            v-for="t in tiposDaAba" :key="t.tipo" type="button" @click="filtroTipo = t.tipo"
            class="rounded-full px-3 py-1 text-[12px] font-semibold transition"
            :class="filtroTipo === t.tipo ? 'bg-navy text-white' : 'bg-mist text-navy/65 hover:text-navy'"
          >{{ t.tipo }} · {{ t.quantidade }}</button>
        </div>

        <p v-if="!filtrados.length" class="px-6 py-10 text-center text-sm text-navy/55">
          {{ aba === 'erros' ? 'Nenhum erro encontrado. A planilha pode ser enviada.' : 'Nenhuma correção foi necessária.' }}
        </p>

        <div v-else class="overflow-x-auto pt-2">
          <table class="w-full min-w-[720px] text-left text-sm">
            <thead class="border-b border-navy/10 text-xs uppercase tracking-wide text-navy/50">
              <tr>
                <th class="w-24 px-6 py-3 font-semibold">Linha</th>
                <th class="px-4 py-3 font-semibold">Campo</th>
                <th class="px-4 py-3 font-semibold">Tipo</th>
                <th class="px-6 py-3 font-semibold">Descrição</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, i) in visiveis" :key="i" class="border-b border-navy/5 last:border-0">
                <td class="px-6 py-3 font-semibold text-navy">{{ linhaDoItem(item) }}</td>
                <td class="px-4 py-3"><code class="text-[12px] text-navy/70">{{ item.campo }}</code></td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <span
                    class="rounded-md px-2.5 py-1 text-xs font-semibold"
                    :class="aba === 'erros' ? 'bg-red-600/10 text-red-700' : 'bg-sky/15 text-ocean'"
                  >{{ item.tipo }}</span>
                </td>
                <td class="px-6 py-3 text-navy/70">{{ item.descricao }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="filtrados.length > visiveis.length" class="border-t border-navy/10 px-6 py-3 text-center">
            <button type="button" @click="limite += LIMITE" class="text-sm font-semibold text-ocean hover:text-navy">
              Mostrar mais ({{ filtrados.length - visiveis.length }} restantes)
            </button>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>
