<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import Icon from '../Icon.vue';
import api from '../../services/api';
import { useAuthStore } from '../../stores/authStore';
import { usePlanilhasStore } from '../../stores/planilhasStore';
import { dataHora } from '../../utils/formato';

const router = useRouter();
const auth = useAuthStore();
const planilhas = usePlanilhasStore();

const EDITORES = ['admin', 'analista'];
const PAGINAS = [
  { titulo: 'Dashboard', to: '/app' },
  { titulo: 'Upload de planilha', to: '/app/upload', papeis: EDITORES },
  { titulo: 'Relatório de validação', to: '/app/relatorio', papeis: EDITORES },
  { titulo: 'Planilhas enviadas', to: '/app/planilhas' },
  { titulo: 'Relatório do dataset', to: '/app/dataset' },
  { titulo: 'Clientes', to: '/app/clientes' },
  { titulo: 'Consultores', to: '/app/consultores' },
  { titulo: 'Serviços', to: '/app/servicos' },
  { titulo: 'Insights', to: '/app/insights' },
  { titulo: 'Usuários', to: '/app/usuarios', papeis: ['admin'] },
  { titulo: 'Telemetria', to: '/app/telemetria', papeis: ['admin'] },
  { titulo: 'Configurações', to: '/app/configuracoes' },
];

const campo = ref(null);
const termo = ref('');
const aberto = ref(false);
const indice = ref(0);
const clientes = ref([]);
const buscando = ref(false);
let temporizador = null;
let ultimaBusca = 0;

const normalizar = (texto) => String(texto || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();

const resultados = computed(() => {
  const t = normalizar(termo.value);
  if (!t) return [];
  const doCliente = clientes.value.slice(0, 6).map((c) => ({
    grupo: 'Clientes', titulo: c.nome, sub: `${c.codigo} · ${c.email || 'sem e-mail'}`, to: `/app/clientes/${c.id}`,
  }));
  const daPlanilha = planilhas.lista
    .filter((p) => normalizar(p.nomeArquivo).includes(t))
    .slice(0, 4)
    .map((p) => ({ grupo: 'Planilhas', titulo: p.nomeArquivo, sub: dataHora(p.enviadaEm), planilhaId: p.id }));
  const daPagina = PAGINAS
    .filter((p) => (!p.papeis || p.papeis.includes(auth.papel)) && normalizar(p.titulo).includes(t))
    .slice(0, 4)
    .map((p) => ({ grupo: 'Telas', titulo: p.titulo, to: p.to }));
  return [...doCliente, ...daPlanilha, ...daPagina];
});

watch(termo, (valor) => {
  indice.value = 0;
  clearTimeout(temporizador);
  if (normalizar(valor).length < 2) {
    clientes.value = [];
    buscando.value = false;
    return;
  }
  buscando.value = true;
  temporizador = setTimeout(async () => {
    const id = ++ultimaBusca;
    try {
      const { data } = await api.get('/api/clientes', { params: { busca: valor.trim() } });
      if (id === ultimaBusca) clientes.value = data;
    } catch {
      if (id === ultimaBusca) clientes.value = [];
    } finally {
      if (id === ultimaBusca) buscando.value = false;
    }
  }, 250);
});

function abrir() {
  aberto.value = true;
  if (!planilhas.carregada && !planilhas.carregando) planilhas.carregarLista();
}

async function escolher(item) {
  if (!item) return;
  termo.value = '';
  aberto.value = false;
  campo.value?.blur();
  if (item.planilhaId) {
    await planilhas.selecionar(item.planilhaId);
    router.push('/app/dataset');
  } else {
    router.push(item.to);
  }
}

function teclado(evento) {
  const total = resultados.value.length;
  if (evento.key === 'ArrowDown' && total) {
    evento.preventDefault();
    indice.value = (indice.value + 1) % total;
  } else if (evento.key === 'ArrowUp' && total) {
    evento.preventDefault();
    indice.value = (indice.value - 1 + total) % total;
  } else if (evento.key === 'Enter') {
    evento.preventDefault();
    escolher(resultados.value[indice.value]);
  } else if (evento.key === 'Escape') {
    aberto.value = false;
    campo.value?.blur();
  }
}

function aoSairDoFoco(evento) {
  if (!evento.currentTarget.contains(evento.relatedTarget)) aberto.value = false;
}

// Atalho "/" foca a busca, como em outros sistemas.
function atalho(evento) {
  const alvo = evento.target;
  const digitando = alvo instanceof HTMLElement && (alvo.isContentEditable || ['INPUT', 'TEXTAREA', 'SELECT'].includes(alvo.tagName));
  if (evento.key === '/' && !digitando) {
    evento.preventDefault();
    campo.value?.focus();
  }
}

onMounted(() => window.addEventListener('keydown', atalho));
onBeforeUnmount(() => {
  window.removeEventListener('keydown', atalho);
  clearTimeout(temporizador);
});
</script>

<template>
  <div class="relative min-w-0 max-w-md flex-1" @focusout="aoSairDoFoco">
    <label class="flex items-center gap-2.5 rounded-lg border border-navy/15 bg-white px-3.5 py-2.5 focus-within:border-sky focus-within:ring-4 focus-within:ring-sky/10">
      <Icon name="search" class="h-4 w-4 flex-none text-navy/40" />
      <span class="sr-only">Buscar</span>
      <input
        ref="campo" v-model="termo" type="search" autocomplete="off"
        placeholder="Buscar clientes, planilhas ou telas"
        role="combobox" :aria-expanded="aberto && !!termo" aria-controls="resultados-busca"
        :aria-activedescendant="resultados.length ? `busca-${indice}` : undefined"
        class="min-w-0 flex-1 border-none bg-transparent text-sm text-navy outline-none placeholder:text-navy/40"
        @focus="abrir" @keydown="teclado"
      />
      <kbd class="hidden rounded border border-navy/15 px-1.5 text-[11px] font-semibold text-navy/40 md:block">/</kbd>
    </label>

    <div
      v-if="aberto && termo.trim()"
      id="resultados-busca" role="listbox"
      class="absolute left-0 right-0 top-full z-30 mt-2 max-h-[70vh] overflow-y-auto rounded-xl border border-navy/10 bg-white py-2 shadow-xl"
    >
      <template v-for="(r, i) in resultados" :key="r.grupo + r.titulo + i">
        <div v-if="i === 0 || resultados[i - 1].grupo !== r.grupo" class="px-4 pb-1 pt-2 text-[11px] font-bold uppercase tracking-wide text-navy/40">
          {{ r.grupo }}
        </div>
        <button
          :id="`busca-${i}`" type="button" role="option" :aria-selected="i === indice"
          class="block w-full px-4 py-2 text-left transition"
          :class="i === indice ? 'bg-ocean/10' : 'hover:bg-mist'"
          @mouseenter="indice = i" @click="escolher(r)"
        >
          <span class="block truncate text-sm font-semibold text-navy">{{ r.titulo }}</span>
          <span v-if="r.sub" class="block truncate text-[12px] text-navy/55">{{ r.sub }}</span>
        </button>
      </template>
      <p v-if="buscando" class="px-4 py-2 text-[13px] text-navy/50">Buscando clientes…</p>
      <p v-else-if="!resultados.length" class="px-4 py-2 text-[13px] text-navy/50">Nada encontrado para “{{ termo.trim() }}”.</p>
    </div>
  </div>
</template>
