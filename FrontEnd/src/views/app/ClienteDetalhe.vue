<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import Icon from '../../components/Icon.vue';
import Modal from '../../components/ui/Modal.vue';
import ClienteFormulario from '../../components/forms/ClienteFormulario.vue';
import api, { mensagemErro } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';
import { useUiStore } from '../../stores/uiStore';
import { EVENTOS } from '../../utils/dominio';
import { botaoPerigo, botaoPrimario, botaoSecundario, cabecalhoTabela, campo, cartao, rotulo } from '../../utils/estilos';
import { data as dataCurta, dataHora, moeda } from '../../utils/formato';

const props = defineProps({ id: { type: String, required: true } });

const router = useRouter();
const auth = useAuthStore();
const ui = useUiStore();

const cliente = ref(null);
const contratos = ref([]);
const insights = ref([]);
const atividade = ref([]);
const servicos = ref([]);
const consultores = ref([]);
const carregando = ref(true);
const erro = ref('');

const aba = ref('contratos');
const abas = computed(() => [
  { id: 'contratos', label: `Contratos (${contratos.value.length})` },
  { id: 'insights', label: `Insights (${insights.value.length})` },
  { id: 'atividade', label: 'Atividade' },
]);

async function carregar() {
  carregando.value = true;
  erro.value = '';
  try {
    const [c, ct, ins, tel] = await Promise.all([
      api.get(`/api/clientes/${props.id}`),
      api.get('/api/contratos', { params: { clienteId: props.id } }),
      api.get('/api/insights', { params: { clienteId: props.id } }),
      api.get('/api/telemetrias', { params: { clienteId: props.id } }),
    ]);
    cliente.value = c.data;
    contratos.value = ct.data.sort((a, b) => Number(b.ativo) - Number(a.ativo) || String(b.dataInicio).localeCompare(String(a.dataInicio)));
    insights.value = ins.data;
    atividade.value = tel.data;
  } catch (e) {
    erro.value = mensagemErro(e, 'Não foi possível carregar o cliente.');
  } finally {
    carregando.value = false;
  }
}

async function carregarListasDeApoio() {
  if (servicos.value.length && consultores.value.length) return;
  const [s, c] = await Promise.all([api.get('/api/servicos'), api.get('/api/consultores')]);
  servicos.value = s.data;
  consultores.value = c.data;
}

watch(() => props.id, carregar, { immediate: true });

// ----- Cliente -----
const editandoCliente = ref(false);
const salvando = ref(false);
const erroForm = ref('');

async function abrirEdicaoCliente() {
  erroForm.value = '';
  await carregarListasDeApoio().catch(() => {});
  editandoCliente.value = true;
}

async function salvarCliente(corpo) {
  salvando.value = true;
  erroForm.value = '';
  try {
    cliente.value = (await api.put(`/api/clientes/${props.id}`, corpo)).data;
    editandoCliente.value = false;
    ui.notificar('Cliente atualizado.');
  } catch (e) {
    erroForm.value = mensagemErro(e);
  } finally {
    salvando.value = false;
  }
}

async function excluirCliente() {
  const ok = await ui.confirmar({
    titulo: `Excluir ${cliente.value.nome}?`,
    mensagem: `Os ${contratos.value.length} contrato(s), ${insights.value.length} insight(s) e o histórico de atividade deste cliente também serão apagados.`,
    botao: 'Excluir cliente',
    perigo: true,
  });
  if (!ok) return;
  try {
    await api.delete(`/api/clientes/${props.id}`);
    ui.notificar(`${cliente.value.nome} foi excluído.`);
    router.push('/app/clientes');
  } catch (e) {
    ui.notificar(mensagemErro(e), 'erro');
  }
}

// ----- Contratos -----
const formContrato = ref(null);
const contrato = reactive({ servicoId: '', dataInicio: '', dataFim: '', valor: '', ativo: true });

async function abrirContrato(existente) {
  erroForm.value = '';
  await carregarListasDeApoio().catch(() => {});
  Object.assign(contrato, {
    servicoId: existente?.servico?.id ?? '',
    dataInicio: existente?.dataInicio || new Date().toISOString().slice(0, 10),
    dataFim: existente?.dataFim || '',
    valor: existente?.valor ?? '',
    ativo: existente ? existente.ativo !== false : true,
  });
  formContrato.value = { id: existente?.id ?? null };
}

function corpoContrato(dados) {
  return {
    clienteId: Number(props.id),
    servicoId: Number(dados.servicoId),
    dataInicio: dados.dataInicio,
    dataFim: dados.dataFim || null,
    valor: dados.valor === '' || dados.valor === null ? null : Number(dados.valor),
    ativo: dados.ativo,
  };
}

async function salvarContrato() {
  salvando.value = true;
  erroForm.value = '';
  try {
    if (formContrato.value.id) await api.put(`/api/contratos/${formContrato.value.id}`, corpoContrato(contrato));
    else await api.post('/api/contratos', corpoContrato(contrato));
    formContrato.value = null;
    ui.notificar('Contrato salvo.');
    await carregar();
  } catch (e) {
    erroForm.value = mensagemErro(e);
  } finally {
    salvando.value = false;
  }
}

async function encerrarContrato(c) {
  const ok = await ui.confirmar({
    titulo: `Encerrar ${c.servico?.nome}?`,
    mensagem: 'O contrato fica inativo com data de fim hoje. Ele continua no histórico do cliente.',
    botao: 'Encerrar contrato',
  });
  if (!ok) return;
  const hoje = new Date().toISOString().slice(0, 10);
  try {
    await api.put(`/api/contratos/${c.id}`, corpoContrato({
      servicoId: c.servico.id, dataInicio: c.dataInicio, dataFim: c.dataInicio > hoje ? c.dataInicio : hoje, valor: c.valor ?? '', ativo: false,
    }));
    ui.notificar('Contrato encerrado.');
    await carregar();
  } catch (e) {
    ui.notificar(mensagemErro(e), 'erro');
  }
}

async function excluirContrato(c) {
  const ok = await ui.confirmar({
    titulo: `Excluir o contrato de ${c.servico?.nome}?`,
    mensagem: 'Os insights ligados a este contrato também serão apagados. Para manter o histórico, prefira encerrar.',
    botao: 'Excluir contrato',
    perigo: true,
  });
  if (!ok) return;
  try {
    await api.delete(`/api/contratos/${c.id}`);
    ui.notificar('Contrato excluído.');
    await carregar();
  } catch (e) {
    ui.notificar(mensagemErro(e), 'erro');
  }
}

// ----- Insights -----
const formInsight = ref(false);
const insight = reactive({ tipo: '', descricao: '', contratoId: '' });

function abrirInsight() {
  erroForm.value = '';
  Object.assign(insight, { tipo: '', descricao: '', contratoId: '' });
  formInsight.value = true;
}

async function salvarInsight() {
  salvando.value = true;
  erroForm.value = '';
  try {
    await api.post('/api/insights', {
      tipo: insight.tipo.trim(),
      descricao: insight.descricao.trim(),
      clienteId: Number(props.id),
      contratoId: insight.contratoId === '' ? null : Number(insight.contratoId),
    });
    formInsight.value = false;
    ui.notificar('Insight registrado.');
    await carregar();
  } catch (e) {
    erroForm.value = mensagemErro(e);
  } finally {
    salvando.value = false;
  }
}

async function excluirInsight(i) {
  const ok = await ui.confirmar({ titulo: `Excluir o insight "${i.tipo}"?`, mensagem: i.descricao, botao: 'Excluir insight', perigo: true });
  if (!ok) return;
  try {
    await api.delete(`/api/insights/${i.id}`);
    ui.notificar('Insight excluído.');
    await carregar();
  } catch (e) {
    ui.notificar(mensagemErro(e), 'erro');
  }
}

const valorTotalAtivo = computed(() =>
  contratos.value.filter((c) => c.ativo && typeof c.valor === 'number').reduce((t, c) => t + c.valor, 0),
);
</script>

<template>
  <div>
    <div class="mb-3 text-[13px] font-medium text-navy/50">
      <RouterLink to="/app/clientes" class="font-semibold hover:text-ocean">Clientes</RouterLink>
      <span v-if="cliente"> / {{ cliente.nome }}</span>
    </div>

    <p v-if="carregando && !cliente" class="text-sm text-navy/55">Carregando cliente…</p>
    <div v-else-if="erro" :class="cartao" class="px-6 py-8 text-center">
      <p class="text-sm font-medium text-red-700">{{ erro }}</p>
      <RouterLink to="/app/clientes" class="mt-4 inline-block text-sm font-semibold text-ocean hover:text-navy">Voltar para Clientes</RouterLink>
    </div>

    <template v-else-if="cliente">
      <div class="flex flex-wrap items-start justify-between gap-6">
        <div>
          <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">{{ cliente.nome }}</h1>
          <div class="mt-2 flex flex-wrap items-center gap-2 text-[13px] text-navy/60">
            <span class="font-semibold text-navy/75">{{ cliente.codigo }}</span>
            <span v-if="cliente.nivel" class="rounded-full bg-sky/10 px-2.5 py-0.5 text-xs font-bold text-ocean">Nível {{ cliente.nivel }}</span>
            <span v-if="cliente.segmento" class="rounded-full bg-navy/5 px-2.5 py-0.5 text-xs font-semibold text-navy/70">{{ cliente.segmento }}</span>
          </div>
        </div>
        <div v-if="auth.podeEditar" class="flex gap-2">
          <button type="button" :class="botaoSecundario" @click="abrirEdicaoCliente">Editar</button>
          <button type="button" :class="botaoPerigo" @click="excluirCliente">Excluir</button>
        </div>
      </div>

      <div class="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <div :class="cartao" class="px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Faturamento anual</div>
          <div class="mt-2 text-xl font-bold text-navy">{{ moeda(cliente.faturamentoAnual) }}</div>
        </div>
        <div :class="cartao" class="px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Contratos ativos</div>
          <div class="mt-2 text-xl font-bold text-navy">{{ contratos.filter((c) => c.ativo).length }}</div>
          <div v-if="valorTotalAtivo" class="mt-1 text-[13px] text-navy/55">{{ moeda(valorTotalAtivo) }} em contratos</div>
        </div>
        <div :class="cartao" class="px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Consultor</div>
          <div class="mt-2 text-base font-semibold text-navy">{{ cliente.consultor?.nome || 'Sem consultor' }}</div>
        </div>
        <div :class="cartao" class="px-5 py-4">
          <div class="text-xs font-semibold uppercase tracking-wide text-navy/50">Cliente desde</div>
          <div class="mt-2 text-base font-semibold text-navy">{{ dataCurta(cliente.dataContratacao) }}</div>
        </div>
      </div>

      <dl :class="cartao" class="mt-4 grid gap-x-6 gap-y-3 px-6 py-5 text-sm sm:grid-cols-3">
        <div><dt class="text-xs font-semibold text-navy/50">E-mail</dt><dd class="mt-0.5 break-all text-navy">{{ cliente.email || '—' }}</dd></div>
        <div><dt class="text-xs font-semibold text-navy/50">Cidade</dt><dd class="mt-0.5 text-navy">{{ cliente.cidade || '—' }}</dd></div>
        <div><dt class="text-xs font-semibold text-navy/50">UF</dt><dd class="mt-0.5 text-navy">{{ cliente.uf || '—' }}</dd></div>
      </dl>

      <div class="my-6 flex gap-6 overflow-x-auto border-b border-navy/10">
        <button
          v-for="t in abas" :key="t.id" type="button" @click="aba = t.id"
          class="-mb-px whitespace-nowrap border-b-2 px-0.5 pb-3 text-sm font-semibold transition"
          :class="aba === t.id ? 'border-navy text-navy' : 'border-transparent text-navy/50 hover:text-navy/80'"
        >{{ t.label }}</button>
      </div>

      <!-- Contratos -->
      <section v-if="aba === 'contratos'">
        <div v-if="auth.podeEditar" class="mb-3 flex justify-end">
          <button type="button" :class="botaoPrimario" @click="abrirContrato(null)">Novo contrato</button>
        </div>
        <div :class="cartao" class="overflow-hidden">
          <div class="overflow-x-auto">
            <table class="w-full min-w-[720px] border-collapse">
              <thead>
                <tr :class="cabecalhoTabela">
                  <th class="px-6 py-3.5 text-left">Serviço</th>
                  <th class="px-4 py-3.5 text-left">Início</th>
                  <th class="px-4 py-3.5 text-left">Fim</th>
                  <th class="px-4 py-3.5 text-right">Valor</th>
                  <th class="px-4 py-3.5 text-left">Situação</th>
                  <th v-if="auth.podeEditar" class="px-6 py-3.5"></th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!contratos.length">
                  <td colspan="6" class="px-6 py-8 text-center text-sm text-navy/55">Nenhum contrato.</td>
                </tr>
                <tr v-for="c in contratos" :key="c.id" class="border-b border-navy/5 last:border-0">
                  <td class="px-6 py-3.5 text-sm font-semibold text-navy">{{ c.servico?.nome }}</td>
                  <td class="px-4 py-3.5 text-sm text-navy/65">{{ dataCurta(c.dataInicio) }}</td>
                  <td class="px-4 py-3.5 text-sm text-navy/65">{{ dataCurta(c.dataFim) }}</td>
                  <td class="whitespace-nowrap px-4 py-3.5 text-right text-sm text-navy">{{ moeda(c.valor) }}</td>
                  <td class="px-4 py-3.5">
                    <span class="rounded-full px-2.5 py-1 text-xs font-bold" :class="c.ativo ? 'bg-emerald-600/10 text-emerald-700' : 'bg-navy/10 text-navy/60'">
                      {{ c.ativo ? 'Ativo' : 'Encerrado' }}
                    </span>
                  </td>
                  <td v-if="auth.podeEditar" class="whitespace-nowrap px-6 py-3.5 text-right text-[13px] font-semibold">
                    <button type="button" class="text-ocean hover:text-navy" @click="abrirContrato(c)">Editar</button>
                    <button v-if="c.ativo" type="button" class="ml-4 text-navy/60 hover:text-navy" @click="encerrarContrato(c)">Encerrar</button>
                    <button type="button" class="ml-4 text-red-700/80 hover:text-red-700" @click="excluirContrato(c)">Excluir</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <!-- Insights -->
      <section v-else-if="aba === 'insights'">
        <div v-if="auth.podeEditar" class="mb-3 flex justify-end">
          <button type="button" :class="botaoPrimario" @click="abrirInsight">Novo insight</button>
        </div>
        <p v-if="!insights.length" :class="cartao" class="px-6 py-8 text-center text-sm text-navy/55">Nenhum insight para este cliente.</p>
        <div class="grid gap-3 md:grid-cols-2">
          <article v-for="i in insights" :key="i.id" :class="cartao" class="flex gap-4 px-5 py-4">
            <span class="w-1 flex-none rounded-full bg-ocean"></span>
            <div class="min-w-0 flex-1">
              <div class="flex items-start justify-between gap-3">
                <h3 class="text-sm font-bold text-navy">{{ i.tipo }}</h3>
                <button v-if="auth.podeEditar" type="button" aria-label="Excluir insight" class="flex-none text-navy/40 hover:text-red-700" @click="excluirInsight(i)">
                  <Icon name="trash" class="h-4 w-4" />
                </button>
              </div>
              <p class="mt-1 text-[13px] leading-relaxed text-navy/70">{{ i.descricao }}</p>
              <p v-if="i.contrato" class="mt-2 text-[12px] text-navy/50">Contrato: {{ i.contrato.servico?.nome }}</p>
            </div>
          </article>
        </div>
      </section>

      <!-- Atividade -->
      <section v-else>
        <p v-if="!atividade.length" :class="cartao" class="px-6 py-8 text-center text-sm text-navy/55">Nenhuma atividade registrada para este cliente.</p>
        <ol v-else :class="cartao" class="divide-y divide-navy/5">
          <li v-for="t in atividade" :key="t.id" class="flex flex-wrap items-baseline gap-x-4 gap-y-1 px-6 py-3.5">
            <span class="w-36 flex-none text-[12.5px] text-navy/50">{{ dataHora(t.dataHora) }}</span>
            <span class="text-sm font-semibold text-navy">{{ EVENTOS[t.evento] || t.evento }}</span>
            <span v-if="t.mensagem" class="text-[13px] text-navy/65">{{ t.mensagem }}</span>
          </li>
        </ol>
      </section>
    </template>

    <Modal :aberto="editandoCliente" titulo="Editar cliente" largura="max-w-2xl" @fechar="editandoCliente = false">
      <ClienteFormulario :inicial="cliente" :consultores="consultores" :salvando="salvando" :erro="erroForm"
        @salvar="salvarCliente" @cancelar="editandoCliente = false" />
    </Modal>

    <Modal :aberto="!!formContrato" :titulo="formContrato?.id ? 'Editar contrato' : 'Novo contrato'" @fechar="formContrato = null">
      <form class="grid gap-4 sm:grid-cols-2" @submit.prevent="salvarContrato">
        <label class="flex flex-col gap-1.5 sm:col-span-2">
          <span :class="rotulo">Serviço</span>
          <select v-model="contrato.servicoId" required :class="campo">
            <option value="" disabled>Escolha um serviço</option>
            <option v-for="s in servicos" :key="s.id" :value="s.id">{{ s.nome }}</option>
          </select>
          <RouterLink v-if="!servicos.length" to="/app/servicos" class="text-[12px] font-semibold text-ocean">Cadastre um serviço primeiro</RouterLink>
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Início</span>
          <input v-model="contrato.dataInicio" type="date" required :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Fim</span>
          <input v-model="contrato.dataFim" type="date" :min="contrato.dataInicio" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Valor (R$)</span>
          <input v-model="contrato.valor" type="number" min="0" step="0.01" :class="campo" />
        </label>
        <label class="flex items-center gap-2.5 self-end pb-2.5 text-sm font-medium text-navy/75">
          <input v-model="contrato.ativo" type="checkbox" class="h-4 w-4 accent-ocean" /> Contrato ativo
        </label>
        <p v-if="erroForm" class="text-sm font-medium text-red-700 sm:col-span-2">{{ erroForm }}</p>
        <div class="flex justify-end gap-3 sm:col-span-2">
          <button type="button" :class="botaoSecundario" @click="formContrato = null">Cancelar</button>
          <button type="submit" :disabled="salvando" :class="botaoPrimario">{{ salvando ? 'Salvando…' : 'Salvar' }}</button>
        </div>
      </form>
    </Modal>

    <Modal :aberto="formInsight" titulo="Novo insight" @fechar="formInsight = false">
      <form class="flex flex-col gap-4" @submit.prevent="salvarInsight">
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Tipo</span>
          <input v-model="insight.tipo" required maxlength="255" list="tipos-insight" placeholder="Ex.: Oportunidade, Risco, Renovação" :class="campo" />
          <datalist id="tipos-insight">
            <option value="Oportunidade" /><option value="Risco" /><option value="Renovação" /><option value="Revisar nível" />
          </datalist>
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Descrição</span>
          <textarea v-model="insight.descricao" required maxlength="500" rows="4" :class="campo"></textarea>
          <span class="text-right text-[11px] text-navy/45">{{ insight.descricao.length }}/500</span>
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Contrato relacionado</span>
          <select v-model="insight.contratoId" :class="campo">
            <option value="">Nenhum</option>
            <option v-for="c in contratos" :key="c.id" :value="c.id">{{ c.servico?.nome }} · desde {{ dataCurta(c.dataInicio) }}</option>
          </select>
        </label>
        <p v-if="erroForm" class="text-sm font-medium text-red-700">{{ erroForm }}</p>
        <div class="flex justify-end gap-3">
          <button type="button" :class="botaoSecundario" @click="formInsight = false">Cancelar</button>
          <button type="submit" :disabled="salvando" :class="botaoPrimario">{{ salvando ? 'Salvando…' : 'Salvar' }}</button>
        </div>
      </form>
    </Modal>
  </div>
</template>
