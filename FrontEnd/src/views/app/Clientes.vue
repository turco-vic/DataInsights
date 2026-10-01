<script setup>
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Modal from '../../components/ui/Modal.vue';
import ClienteFormulario from '../../components/forms/ClienteFormulario.vue';
import api, { mensagemErro } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';
import { useUiStore } from '../../stores/uiStore';
import { NIVEIS, SEGMENTOS, UFS } from '../../utils/dominio';
import { botaoPrimario, cabecalhoTabela, campo, cartao } from '../../utils/estilos';
import { moeda } from '../../utils/formato';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const ui = useUiStore();

const clientes = ref([]);
const consultores = ref([]);
const carregando = ref(true);
const erro = ref('');

// Os filtros podem chegar pela URL (ex.: links do Dashboard e de Consultores).
const daUrl = (chave) => (route.query[chave] ? String(route.query[chave]) : '');
const filtros = reactive({
  busca: daUrl('busca'),
  segmento: daUrl('segmento'),
  nivel: daUrl('nivel'),
  uf: daUrl('uf'),
  consultorId: daUrl('consultorId'),
});

const modalAberto = ref(false);
const salvando = ref(false);
const erroForm = ref('');

let temporizador = null;
let ultimaCarga = 0;

async function carregar() {
  const id = ++ultimaCarga;
  carregando.value = true;
  erro.value = '';
  const params = Object.fromEntries(Object.entries(filtros).filter(([, v]) => v !== ''));
  try {
    const { data } = await api.get('/api/clientes', { params });
    if (id === ultimaCarga) clientes.value = data;
  } catch (e) {
    if (id === ultimaCarga) erro.value = mensagemErro(e, 'Não foi possível carregar os clientes.');
  } finally {
    if (id === ultimaCarga) carregando.value = false;
  }
}

watch(filtros, () => {
  clearTimeout(temporizador);
  temporizador = setTimeout(carregar, 250);
});

function limparFiltros() {
  Object.assign(filtros, { busca: '', segmento: '', nivel: '', uf: '', consultorId: '' });
  if (Object.keys(route.query).length) router.replace({ query: {} });
}

const temFiltro = () => Object.values(filtros).some((v) => v !== '');

async function criar(corpo) {
  salvando.value = true;
  erroForm.value = '';
  try {
    const { data } = await api.post('/api/clientes', corpo);
    modalAberto.value = false;
    ui.notificar(`${data.nome} foi cadastrado.`);
    router.push(`/app/clientes/${data.id}`);
  } catch (e) {
    erroForm.value = mensagemErro(e);
  } finally {
    salvando.value = false;
  }
}

onMounted(async () => {
  carregar();
  try {
    consultores.value = (await api.get('/api/consultores')).data;
  } catch {
    consultores.value = [];
  }
});
onBeforeUnmount(() => clearTimeout(temporizador));
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Clientes</h1>
        <p class="mt-1.5 text-sm text-navy/60">{{ clientes.length }} cliente(s){{ temFiltro() ? ' com os filtros atuais' : ' na carteira' }}</p>
      </div>
      <button v-if="auth.podeEditar" type="button" :class="botaoPrimario" @click="erroForm = ''; modalAberto = true">Novo cliente</button>
    </div>

    <div class="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
      <label class="lg:col-span-2">
        <span class="sr-only">Buscar</span>
        <input v-model="filtros.busca" type="search" placeholder="Nome, código ou e-mail" :class="campo" />
      </label>
      <label>
        <span class="sr-only">Segmento</span>
        <select v-model="filtros.segmento" :class="campo">
          <option value="">Todos os segmentos</option>
          <option v-for="s in SEGMENTOS" :key="s" :value="s">{{ s }}</option>
        </select>
      </label>
      <label>
        <span class="sr-only">Nível</span>
        <select v-model="filtros.nivel" :class="campo">
          <option value="">Todos os níveis</option>
          <option v-for="n in NIVEIS" :key="n" :value="n">Nível {{ n }}</option>
        </select>
      </label>
      <label>
        <span class="sr-only">UF</span>
        <select v-model="filtros.uf" :class="campo">
          <option value="">Todas as UFs</option>
          <option v-for="u in UFS" :key="u" :value="u">{{ u }}</option>
        </select>
      </label>
      <label class="sm:col-span-2 lg:col-span-2">
        <span class="sr-only">Consultor</span>
        <select v-model="filtros.consultorId" :class="campo">
          <option value="">Todos os consultores</option>
          <option v-for="c in consultores" :key="c.id" :value="String(c.id)">{{ c.nome }}</option>
        </select>
      </label>
      <button v-if="temFiltro()" type="button" class="justify-self-start text-sm font-semibold text-ocean hover:text-navy" @click="limparFiltros">Limpar filtros</button>
    </div>

    <div class="mt-4 overflow-hidden" :class="cartao">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[820px] border-collapse">
          <thead>
            <tr :class="cabecalhoTabela">
              <th class="px-6 py-3.5 text-left">Cliente</th>
              <th class="px-4 py-3.5 text-left">Segmento</th>
              <th class="px-4 py-3.5 text-center">Nível</th>
              <th class="px-4 py-3.5 text-left">Consultor</th>
              <th class="px-4 py-3.5 text-left">UF</th>
              <th class="px-6 py-3.5 text-right">Faturamento anual</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="carregando && !clientes.length">
              <td colspan="6" class="px-6 py-8 text-center text-sm text-navy/55">Carregando clientes…</td>
            </tr>
            <tr v-else-if="erro">
              <td colspan="6" class="px-6 py-8 text-center text-sm font-medium text-red-700">{{ erro }}</td>
            </tr>
            <tr v-else-if="!clientes.length">
              <td colspan="6" class="px-6 py-10 text-center text-sm text-navy/55">
                {{ temFiltro() ? 'Nenhum cliente com esses filtros.' : 'Nenhum cliente ainda. Envie uma planilha ou cadastre o primeiro.' }}
              </td>
            </tr>
            <template v-else>
              <tr
                v-for="c in clientes" :key="c.id"
                class="cursor-pointer border-b border-navy/5 last:border-0 hover:bg-mist"
                @click="router.push(`/app/clientes/${c.id}`)"
              >
                <td class="px-6 py-3.5">
                  <RouterLink :to="`/app/clientes/${c.id}`" class="text-sm font-semibold text-navy hover:text-ocean" @click.stop>{{ c.nome }}</RouterLink>
                  <div class="text-[12.5px] text-navy/55">{{ c.codigo }} · {{ c.email || 'sem e-mail' }}</div>
                </td>
                <td class="px-4 py-3.5 text-sm text-navy/65">{{ c.segmento || '—' }}</td>
                <td class="px-4 py-3.5 text-center">
                  <span v-if="c.nivel" class="rounded-full bg-sky/10 px-2.5 py-1 text-xs font-bold text-ocean">{{ c.nivel }}</span>
                  <span v-else class="text-navy/40">—</span>
                </td>
                <td class="px-4 py-3.5 text-sm text-navy/65">{{ c.consultor?.nome || '—' }}</td>
                <td class="px-4 py-3.5 text-sm text-navy/65">{{ c.uf || '—' }}</td>
                <td class="whitespace-nowrap px-6 py-3.5 text-right text-sm font-semibold text-navy">{{ moeda(c.faturamentoAnual) }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <Modal :aberto="modalAberto" titulo="Novo cliente" largura="max-w-2xl" @fechar="modalAberto = false">
      <ClienteFormulario :consultores="consultores" :salvando="salvando" :erro="erroForm" @salvar="criar" @cancelar="modalAberto = false" />
    </Modal>
  </div>
</template>
