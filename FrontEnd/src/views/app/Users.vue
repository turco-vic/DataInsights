<script setup>
import { onMounted, reactive, ref } from 'vue';
import StatusBadge from '../../components/StatusBadge.vue';
import Modal from '../../components/ui/Modal.vue';
import api, { mensagemErro } from '../../services/api';
import { iniciais, paraUsuario } from '../../services/auth';
import { useAuthStore } from '../../stores/authStore';
import { useUiStore } from '../../stores/uiStore';
import { PAPEIS } from '../../utils/dominio';
import { botaoPerigo, botaoPrimario, botaoSecundario, cabecalhoTabela, campo, rotulo } from '../../utils/estilos';
import { dataHora } from '../../utils/formato';

const auth = useAuthStore();
const ui = useUiStore();

const usuarios = ref([]);
const carregando = ref(true);
const erroLista = ref('');

// null = formulário fechado; { id: null } = novo usuário; { id: n } = edição.
const form = ref(null);
const dados = reactive({ nome: '', email: '', senha: '', papel: 'LEITOR', empresa: '', cargo: '' });
const salvando = ref(false);
const erroForm = ref('');

const papeisDeUsuario = ['admin', 'analista', 'leitor'];

function ehVoce(u) {
  return auth.usuario?.tipo !== 'consultor' && u.id === auth.usuario?.id;
}

async function carregar() {
  carregando.value = true;
  erroLista.value = '';
  try {
    const { data } = await api.get('/api/usuarios');
    usuarios.value = data.sort((a, b) => a.nome.localeCompare(b.nome));
  } catch (error) {
    erroLista.value = mensagemErro(error, 'Não foi possível carregar os usuários.');
  } finally {
    carregando.value = false;
  }
}

function abrir(usuario) {
  form.value = { id: usuario?.id ?? null };
  Object.assign(dados, {
    nome: usuario?.nome || '',
    email: usuario?.email || '',
    senha: '',
    papel: usuario?.papel || 'LEITOR',
    empresa: usuario?.empresa || '',
    cargo: usuario?.cargo || '',
  });
  erroForm.value = '';
}

async function salvar() {
  salvando.value = true;
  erroForm.value = '';
  const corpo = {
    nome: dados.nome.trim(),
    email: dados.email.trim(),
    senha: dados.senha || null,
    papel: dados.papel,
    empresa: dados.empresa.trim() || null,
    cargo: dados.cargo.trim() || null,
  };
  try {
    if (form.value.id) {
      const { data } = await api.put(`/api/usuarios/${form.value.id}`, corpo);
      if (ehVoce(data)) auth.atualizarUsuario({ ...paraUsuario(data), prefs: auth.usuario.prefs, inicial: iniciais(data.nome) });
      ui.notificar('Usuário atualizado.');
    } else {
      await api.post('/api/usuarios', corpo);
      ui.notificar(`${corpo.nome} agora tem acesso à plataforma.`);
    }
    form.value = null;
    await carregar();
  } catch (error) {
    erroForm.value = mensagemErro(error);
  } finally {
    salvando.value = false;
  }
}

async function excluir() {
  const nome = dados.nome;
  const confirmou = await ui.confirmar({
    titulo: `Excluir ${nome}?`,
    mensagem: 'A pessoa perde o acesso à plataforma. Essa ação não pode ser desfeita.',
    botao: 'Excluir usuário',
    perigo: true,
  });
  if (!confirmou) return;
  salvando.value = true;
  erroForm.value = '';
  try {
    await api.delete(`/api/usuarios/${form.value.id}`);
    ui.notificar(`${nome} foi excluído(a).`);
    form.value = null;
    await carregar();
  } catch (error) {
    erroForm.value = mensagemErro(error);
  } finally {
    salvando.value = false;
  }
}

onMounted(carregar);
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Usuários</h1>
        <p class="mt-1.5 text-sm text-navy/60">{{ usuarios.length }} pessoa(s) com acesso à plataforma</p>
      </div>
      <button type="button" :class="botaoPrimario" @click="abrir(null)">Novo usuário</button>
    </div>

    <div class="mt-6 overflow-hidden rounded-xl border border-navy/10 bg-white">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[680px] border-collapse">
          <thead>
            <tr :class="cabecalhoTabela">
              <th class="px-6 py-3.5 text-left">Pessoa</th>
              <th class="px-4 py-3.5 text-left">Papel</th>
              <th class="px-4 py-3.5 text-left">Último acesso</th>
              <th class="px-6 py-3.5"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="carregando && !usuarios.length">
              <td colspan="4" class="px-6 py-6 text-center text-sm text-navy/55">Carregando usuários…</td>
            </tr>
            <tr v-else-if="erroLista">
              <td colspan="4" class="px-6 py-6 text-center text-sm font-medium text-red-700">{{ erroLista }}</td>
            </tr>
            <template v-else>
              <tr v-for="u in usuarios" :key="u.id" class="border-b border-navy/5 last:border-0 hover:bg-mist">
                <td class="px-6 py-3.5">
                  <div class="flex items-center gap-3">
                    <div class="flex h-9 w-9 flex-none items-center justify-center rounded-full bg-navy/5 text-xs font-bold text-navy">{{ iniciais(u.nome) }}</div>
                    <div>
                      <div class="text-sm font-semibold text-navy">
                        {{ u.nome }}
                        <span v-if="ehVoce(u)" class="ml-1.5 rounded-full bg-sky/10 px-2 py-0.5 text-[11px] font-semibold text-ocean">você</span>
                      </div>
                      <div class="text-[12.5px] text-navy/55">{{ u.email }}<span v-if="u.empresa"> · {{ u.empresa }}</span></div>
                    </div>
                  </div>
                </td>
                <td class="px-4 py-3.5"><StatusBadge :status="(u.papel || '').toLowerCase()" /></td>
                <td class="px-4 py-3.5 text-[13px] text-navy/60">{{ u.ultimoAcesso ? dataHora(u.ultimoAcesso) : 'Nunca entrou' }}</td>
                <td class="px-6 py-3.5 text-right"><button type="button" @click="abrir(u)" class="whitespace-nowrap text-[13px] font-semibold text-ocean hover:text-navy">Gerenciar</button></td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <div class="mt-4 grid gap-4 sm:grid-cols-3">
      <div v-for="p in papeisDeUsuario" :key="p" class="rounded-xl border border-navy/10 bg-white px-5 py-4">
        <div class="text-[13px] font-bold" :class="PAPEIS[p].cor">{{ PAPEIS[p].rotulo }}</div>
        <p class="mt-2 text-[13px] leading-relaxed text-navy/60">{{ PAPEIS[p].descricao }}</p>
      </div>
    </div>

    <Modal :aberto="!!form" :titulo="form?.id ? 'Editar usuário' : 'Novo usuário'" @fechar="form = null">
      <form class="grid gap-4 sm:grid-cols-2" @submit.prevent="salvar">
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Nome</span>
          <input v-model="dados.nome" type="text" required maxlength="150" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">E-mail</span>
          <input v-model="dados.email" type="email" required :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Papel</span>
          <select v-model="dados.papel" :class="campo">
            <option v-for="p in papeisDeUsuario" :key="p" :value="p.toUpperCase()">{{ PAPEIS[p].rotulo }}</option>
          </select>
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Senha</span>
          <input v-model="dados.senha" type="password" minlength="6" autocomplete="new-password" :required="!form?.id"
            :placeholder="form?.id ? 'Deixe vazio para manter' : 'Mínimo de 6 caracteres'" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Empresa</span>
          <input v-model="dados.empresa" type="text" maxlength="150" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Cargo</span>
          <input v-model="dados.cargo" type="text" maxlength="100" :class="campo" />
        </label>
        <p class="text-[12.5px] text-navy/55 sm:col-span-2">{{ PAPEIS[dados.papel.toLowerCase()]?.descricao }}</p>
        <p v-if="erroForm" class="text-sm font-medium text-red-700 sm:col-span-2">{{ erroForm }}</p>
        <div class="flex flex-wrap items-center gap-3 sm:col-span-2">
          <button v-if="form?.id && !ehVoce(form)" type="button" :disabled="salvando" :class="botaoPerigo" @click="excluir">Excluir usuário</button>
          <div class="ml-auto flex gap-3">
            <button type="button" :class="botaoSecundario" @click="form = null">Cancelar</button>
            <button type="submit" :disabled="salvando" :class="botaoPrimario">{{ salvando ? 'Salvando…' : 'Salvar' }}</button>
          </div>
        </div>
      </form>
    </Modal>
  </div>
</template>
