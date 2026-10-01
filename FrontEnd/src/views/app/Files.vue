<script setup>
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import StatusBadge from '../../components/StatusBadge.vue';
import SemPlanilhas from '../../components/SemPlanilhas.vue';
import { mensagemErro } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';
import { usePlanilhasStore } from '../../stores/planilhasStore';
import { useUiStore } from '../../stores/uiStore';
import { dataHora } from '../../utils/formato';

const router = useRouter();
const auth = useAuthStore();
const planilhas = usePlanilhasStore();
const ui = useUiStore();

const filtros = [
  { label: 'Todas', status: null },
  { label: 'Processadas', status: 'processado' },
  { label: 'Com erros', status: 'erro' },
];
const filtro = ref(null);

const lista = computed(() =>
  filtro.value ? planilhas.lista.filter((a) => a.status === filtro.value) : planilhas.lista,
);

async function abrir(planilha) {
  await planilhas.selecionar(planilha.id);
  router.push('/app/dataset');
}

async function excluir(planilha) {
  const confirmou = await ui.confirmar({
    titulo: `Excluir ${planilha.nomeArquivo}?`,
    mensagem: 'A planilha sai do histórico e do relatório do dataset. Os clientes importados por ela continuam cadastrados.',
    botao: 'Excluir planilha',
    perigo: true,
  });
  if (!confirmou) return;
  try {
    await planilhas.remover(planilha.id);
    ui.notificar(`${planilha.nomeArquivo} foi excluída do histórico.`);
  } catch (e) {
    ui.notificar(mensagemErro(e), 'erro');
  }
}

onMounted(() => planilhas.carregarLista());
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Planilhas enviadas</h1>
        <p class="mt-1.5 text-sm text-navy/60">{{ planilhas.lista.length }} planilha(s) importada(s) pela equipe</p>
      </div>
      <RouterLink v-if="auth.podeEditar" to="/app/upload" class="rounded-lg bg-ocean px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-navy">Enviar planilha</RouterLink>
    </div>

    <p v-if="planilhas.erro" class="mt-6 rounded-xl border border-red-500/30 bg-red-50 px-5 py-4 text-sm font-medium text-red-800">{{ planilhas.erro }}</p>
    <p v-else-if="planilhas.carregando && !planilhas.carregada" class="mt-6 text-sm text-navy/55">Carregando planilhas…</p>
    <SemPlanilhas v-else-if="!planilhas.temPlanilhas" :mostrar-botao="auth.podeEditar" />

    <template v-else>
      <div class="my-5 flex flex-wrap items-center gap-2">
        <button
          v-for="f in filtros" :key="f.label" type="button" @click="filtro = f.status"
          class="rounded-full px-3.5 py-1.5 text-[13px] font-medium transition"
          :class="filtro === f.status ? 'bg-navy text-white' : 'border border-navy/15 bg-white text-navy/65 hover:text-navy'"
        >{{ f.label }}</button>
      </div>

      <div class="overflow-hidden rounded-xl border border-navy/10 bg-white">
        <div class="overflow-x-auto">
          <table class="w-full min-w-[820px] border-collapse">
            <thead>
              <tr class="border-b border-navy/10 text-[11px] font-semibold uppercase tracking-wide text-navy/50">
                <th class="px-6 py-3.5 text-left">Arquivo</th>
                <th class="px-4 py-3.5 text-left">Enviado por</th>
                <th class="px-4 py-3.5 text-left">Data</th>
                <th class="px-4 py-3.5 text-right">Linhas</th>
                <th class="px-4 py-3.5 text-right">Novos / atualizados</th>
                <th class="px-4 py-3.5 text-right">Status</th>
                <th class="px-6 py-3.5"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="a in lista" :key="a.id" class="border-b border-navy/5 last:border-0 hover:bg-mist">
                <td class="px-6 py-3.5 text-sm font-semibold text-navy">
                  {{ a.nomeArquivo }}
                  <span v-if="a.id === planilhas.ativaResumo?.id" class="ml-2 rounded-full bg-sky/10 px-2 py-0.5 text-[11px] font-semibold text-ocean">no relatório</span>
                </td>
                <td class="px-4 py-3.5 text-sm text-navy/65">{{ a.autorNome || '—' }}</td>
                <td class="px-4 py-3.5 text-[13px] text-navy/55">{{ dataHora(a.enviadaEm) }}</td>
                <td class="whitespace-nowrap px-4 py-3.5 text-right text-sm font-semibold text-navy">{{ a.totalLinhas }}</td>
                <td class="whitespace-nowrap px-4 py-3.5 text-right text-sm text-navy/65">{{ a.criados }} / {{ a.atualizados }}</td>
                <td class="px-4 py-3.5 text-right"><StatusBadge :status="a.status" /></td>
                <td class="whitespace-nowrap px-6 py-3.5 text-right">
                  <button type="button" @click="abrir(a)" class="text-[13px] font-semibold text-ocean hover:text-navy">Abrir</button>
                  <button v-if="auth.podeEditar" type="button" @click="excluir(a)" class="ml-4 text-[13px] font-semibold text-red-700/80 hover:text-red-700">Excluir</button>
                </td>
              </tr>
              <tr v-if="!lista.length">
                <td colspan="7" class="px-6 py-10 text-center text-sm text-navy/50">Nenhuma planilha neste filtro.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
