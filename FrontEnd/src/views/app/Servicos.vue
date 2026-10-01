<script setup>
import { onMounted, reactive, ref } from 'vue';
import Modal from '../../components/ui/Modal.vue';
import api, { mensagemErro } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';
import { useUiStore } from '../../stores/uiStore';
import { botaoPerigo, botaoPrimario, botaoSecundario, cabecalhoTabela, campo, cartao, rotulo } from '../../utils/estilos';

const auth = useAuthStore();
const ui = useUiStore();

const servicos = ref([]);
const carregando = ref(true);
const erro = ref('');

const form = ref(null);
const dados = reactive({ nome: '', categoria: '' });
const salvando = ref(false);
const erroForm = ref('');

async function carregar() {
  carregando.value = true;
  erro.value = '';
  try {
    servicos.value = (await api.get('/api/servicos')).data;
  } catch (e) {
    erro.value = mensagemErro(e, 'Não foi possível carregar os serviços.');
  } finally {
    carregando.value = false;
  }
}

function abrir(s) {
  erroForm.value = '';
  form.value = { id: s?.id ?? null, totalContratos: s?.totalContratos ?? 0 };
  Object.assign(dados, { nome: s?.nome || '', categoria: s?.categoria || '' });
}

async function salvar() {
  salvando.value = true;
  erroForm.value = '';
  const corpo = { nome: dados.nome.trim(), categoria: dados.categoria.trim() || null };
  try {
    if (form.value.id) await api.put(`/api/servicos/${form.value.id}`, corpo);
    else await api.post('/api/servicos', corpo);
    ui.notificar(form.value.id ? 'Serviço atualizado.' : `${corpo.nome} foi cadastrado.`);
    form.value = null;
    await carregar();
  } catch (e) {
    erroForm.value = mensagemErro(e);
  } finally {
    salvando.value = false;
  }
}

async function excluir() {
  const ok = await ui.confirmar({
    titulo: `Excluir ${dados.nome}?`,
    mensagem: 'Só é possível excluir serviços sem contratos.',
    botao: 'Excluir serviço',
    perigo: true,
  });
  if (!ok) return;
  try {
    await api.delete(`/api/servicos/${form.value.id}`);
    ui.notificar(`${dados.nome} foi excluído.`);
    form.value = null;
    await carregar();
  } catch (e) {
    erroForm.value = mensagemErro(e);
  }
}

onMounted(carregar);
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Serviços</h1>
        <p class="mt-1.5 text-sm text-navy/60">{{ servicos.length }} serviço(s) no catálogo.</p>
      </div>
      <button v-if="auth.podeEditar" type="button" :class="botaoPrimario" @click="abrir(null)">Novo serviço</button>
    </div>

    <div :class="cartao" class="mt-6 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[520px] border-collapse">
          <thead>
            <tr :class="cabecalhoTabela">
              <th class="px-6 py-3.5 text-left">Serviço</th>
              <th class="px-4 py-3.5 text-left">Categoria</th>
              <th class="px-4 py-3.5 text-right">Contratos</th>
              <th v-if="auth.podeEditar" class="px-6 py-3.5"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="carregando && !servicos.length"><td colspan="4" class="px-6 py-8 text-center text-sm text-navy/55">Carregando…</td></tr>
            <tr v-else-if="erro"><td colspan="4" class="px-6 py-8 text-center text-sm font-medium text-red-700">{{ erro }}</td></tr>
            <tr v-else-if="!servicos.length"><td colspan="4" class="px-6 py-10 text-center text-sm text-navy/55">Nenhum serviço. Eles também são criados ao importar uma planilha.</td></tr>
            <template v-else>
            <tr v-for="s in servicos" :key="s.id" class="border-b border-navy/5 last:border-0 hover:bg-mist">
              <td class="px-6 py-3.5 text-sm font-semibold text-navy">{{ s.nome }}</td>
              <td class="px-4 py-3.5 text-sm text-navy/65">{{ s.categoria || '—' }}</td>
              <td class="px-4 py-3.5 text-right text-sm font-semibold text-navy">{{ s.totalContratos }}</td>
              <td v-if="auth.podeEditar" class="px-6 py-3.5 text-right">
                <button type="button" class="text-[13px] font-semibold text-ocean hover:text-navy" @click="abrir(s)">Gerenciar</button>
              </td>
            </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <Modal :aberto="!!form" :titulo="form?.id ? 'Editar serviço' : 'Novo serviço'" @fechar="form = null">
      <form class="flex flex-col gap-4" @submit.prevent="salvar">
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Nome</span>
          <input v-model="dados.nome" required maxlength="150" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Categoria</span>
          <input v-model="dados.categoria" maxlength="100" placeholder="Ex.: Cloud, Suporte, Segurança" :class="campo" />
        </label>
        <p v-if="form?.totalContratos" class="text-[13px] text-navy/60">Usado em {{ form.totalContratos }} contrato(s).</p>
        <p v-if="erroForm" class="text-sm font-medium text-red-700">{{ erroForm }}</p>
        <div class="flex flex-wrap items-center gap-3">
          <button v-if="form?.id" type="button" :class="botaoPerigo" @click="excluir">Excluir</button>
          <div class="ml-auto flex gap-3">
            <button type="button" :class="botaoSecundario" @click="form = null">Cancelar</button>
            <button type="submit" :disabled="salvando" :class="botaoPrimario">{{ salvando ? 'Salvando…' : 'Salvar' }}</button>
          </div>
        </div>
      </form>
    </Modal>
  </div>
</template>
