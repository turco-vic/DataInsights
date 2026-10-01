<script setup>
import { onMounted, reactive, ref } from 'vue';
import { RouterLink } from 'vue-router';
import Modal from '../../components/ui/Modal.vue';
import api, { mensagemErro } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';
import { useUiStore } from '../../stores/uiStore';
import { botaoPerigo, botaoPrimario, botaoSecundario, cabecalhoTabela, campo, cartao, rotulo } from '../../utils/estilos';

const auth = useAuthStore();
const ui = useUiStore();

const consultores = ref([]);
const carregando = ref(true);
const erro = ref('');

const form = ref(null);
const dados = reactive({ nome: '', matricula: '', email: '', senha: '' });
const salvando = ref(false);
const erroForm = ref('');

async function carregar() {
  carregando.value = true;
  erro.value = '';
  try {
    consultores.value = (await api.get('/api/consultores')).data;
  } catch (e) {
    erro.value = mensagemErro(e, 'Não foi possível carregar os consultores.');
  } finally {
    carregando.value = false;
  }
}

function abrir(c) {
  erroForm.value = '';
  form.value = { id: c?.id ?? null, tinhaAcesso: Boolean(c?.email) };
  Object.assign(dados, { nome: c?.nome || '', matricula: c?.matricula || '', email: c?.email || '', senha: '' });
}

async function salvar() {
  salvando.value = true;
  erroForm.value = '';
  const corpo = {
    nome: dados.nome.trim(),
    matricula: dados.matricula.trim() || null,
    email: dados.email.trim() || null,
    senha: dados.senha || null,
  };
  try {
    if (form.value.id) await api.put(`/api/consultores/${form.value.id}`, corpo);
    else await api.post('/api/consultores', corpo);
    ui.notificar(form.value.id ? 'Consultor atualizado.' : `${corpo.nome} foi cadastrado.`);
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
    mensagem: 'Só é possível excluir consultores sem clientes. Transfira os clientes antes, se houver.',
    botao: 'Excluir consultor',
    perigo: true,
  });
  if (!ok) return;
  try {
    await api.delete(`/api/consultores/${form.value.id}`);
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
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Consultores</h1>
        <p class="mt-1.5 text-sm text-navy/60">{{ consultores.length }} consultor(es). Os que têm e-mail e senha podem entrar na plataforma para consultar a carteira.</p>
      </div>
      <button v-if="auth.podeEditar" type="button" :class="botaoPrimario" @click="abrir(null)">Novo consultor</button>
    </div>

    <div :class="cartao" class="mt-6 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[640px] border-collapse">
          <thead>
            <tr :class="cabecalhoTabela">
              <th class="px-6 py-3.5 text-left">Consultor</th>
              <th class="px-4 py-3.5 text-left">Matrícula</th>
              <th class="px-4 py-3.5 text-left">Acesso</th>
              <th class="px-4 py-3.5 text-right">Clientes</th>
              <th v-if="auth.podeEditar" class="px-6 py-3.5"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="carregando && !consultores.length"><td colspan="5" class="px-6 py-8 text-center text-sm text-navy/55">Carregando…</td></tr>
            <tr v-else-if="erro"><td colspan="5" class="px-6 py-8 text-center text-sm font-medium text-red-700">{{ erro }}</td></tr>
            <tr v-else-if="!consultores.length"><td colspan="5" class="px-6 py-10 text-center text-sm text-navy/55">Nenhum consultor. Eles também são criados ao importar uma planilha.</td></tr>
            <template v-else>
            <tr v-for="c in consultores" :key="c.id" class="border-b border-navy/5 last:border-0 hover:bg-mist">
              <td class="px-6 py-3.5">
                <div class="text-sm font-semibold text-navy">{{ c.nome }}</div>
                <div class="text-[12.5px] text-navy/55">{{ c.email || 'sem e-mail' }}</div>
              </td>
              <td class="px-4 py-3.5 text-sm text-navy/65">{{ c.matricula || '—' }}</td>
              <td class="px-4 py-3.5">
                <span class="rounded-full px-2.5 py-1 text-xs font-bold" :class="c.email ? 'bg-emerald-600/10 text-emerald-700' : 'bg-navy/10 text-navy/60'">
                  {{ c.email ? 'Pode entrar' : 'Sem acesso' }}
                </span>
              </td>
              <td class="px-4 py-3.5 text-right text-sm font-semibold">
                <RouterLink v-if="c.totalClientes" :to="{ path: '/app/clientes', query: { consultorId: c.id } }" class="text-ocean hover:text-navy">{{ c.totalClientes }}</RouterLink>
                <span v-else class="text-navy/40">0</span>
              </td>
              <td v-if="auth.podeEditar" class="px-6 py-3.5 text-right">
                <button type="button" class="text-[13px] font-semibold text-ocean hover:text-navy" @click="abrir(c)">Gerenciar</button>
              </td>
            </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <Modal :aberto="!!form" :titulo="form?.id ? 'Editar consultor' : 'Novo consultor'" @fechar="form = null">
      <form class="grid gap-4 sm:grid-cols-2" @submit.prevent="salvar">
        <label class="flex flex-col gap-1.5 sm:col-span-2">
          <span :class="rotulo">Nome</span>
          <input v-model="dados.nome" required maxlength="150" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Matrícula</span>
          <input v-model="dados.matricula" maxlength="50" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">E-mail</span>
          <input v-model="dados.email" type="email" :required="!!dados.senha" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5 sm:col-span-2">
          <span :class="rotulo">Senha de acesso</span>
          <input v-model="dados.senha" type="password" minlength="6" autocomplete="new-password"
            :placeholder="form?.tinhaAcesso ? 'Deixe vazio para manter a atual' : 'Opcional: só se o consultor for entrar na plataforma'" :class="campo" />
        </label>
        <p v-if="erroForm" class="text-sm font-medium text-red-700 sm:col-span-2">{{ erroForm }}</p>
        <div class="flex flex-wrap items-center gap-3 sm:col-span-2">
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
