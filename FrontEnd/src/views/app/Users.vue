<script setup>
import { onMounted, reactive, ref } from 'vue';
import api, { mensagemErro } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

const auth = useAuthStore();
const usuarios = ref([]);
const carregando = ref(true);
const erroLista = ref('');

// null = formulário fechado; { id: null } = novo usuário; { id: n } = edição.
const form = ref(null);
const dados = reactive({ nome: '', email: '', senha: '' });
const salvando = ref(false);
const erroForm = ref('');

const campo = 'w-full rounded-lg border border-navy/15 bg-mist p-2.5 text-sm text-navy outline-none transition focus:border-sky focus:bg-white focus:ring-4 focus:ring-sky/10';

const papeis = [
  { t: 'Administrador', d: 'Gerencia usuários, integrações e todas as planilhas da empresa.', c: 'text-navy' },
  { t: 'Analista', d: 'Envia planilhas, cria dashboards e compartilha relatórios.', c: 'text-ocean' },
  { t: 'Leitor', d: 'Visualiza dashboards já publicados, sem editar dados.', c: 'text-sky' },
];

function iniciais(nome) {
  return (nome || '').split(' ').filter(Boolean).slice(0, 2).map((p) => p[0]).join('').toUpperCase();
}

function ehVoce(u) {
  return u.email === auth.usuario?.email;
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
  Object.assign(dados, { nome: usuario?.nome || '', email: usuario?.email || '', senha: '' });
  erroForm.value = '';
}

function fechar() {
  form.value = null;
}

async function salvar() {
  salvando.value = true;
  erroForm.value = '';
  const corpo = { nome: dados.nome.trim(), email: dados.email.trim(), senha: dados.senha || null };
  try {
    if (form.value.id) {
      const { data } = await api.put(`/api/usuarios/${form.value.id}`, corpo);
      if (form.value.id === auth.usuario?.id) {
        auth.atualizarUsuario({ nome: data.nome, email: data.email, inicial: iniciais(data.nome) });
      }
    } else {
      await api.post('/api/usuarios', corpo);
    }
    fechar();
    await carregar();
  } catch (error) {
    erroForm.value = mensagemErro(error);
  } finally {
    salvando.value = false;
  }
}

async function excluir() {
  if (!confirm(`Excluir o usuário ${dados.nome}? Essa ação não pode ser desfeita.`)) return;
  salvando.value = true;
  erroForm.value = '';
  try {
    await api.delete(`/api/usuarios/${form.value.id}`);
    fechar();
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
      <button type="button" @click="abrir(null)" class="rounded-lg bg-ocean px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-navy">Novo usuário</button>
    </div>

    <form v-if="form" @submit.prevent="salvar" class="mt-6 rounded-xl border border-navy/10 bg-white px-6 py-5">
      <h2 class="text-base font-bold text-navy">{{ form.id ? 'Editar usuário' : 'Novo usuário' }}</h2>
      <div class="mt-4 grid gap-4 sm:grid-cols-3">
        <label class="flex flex-col gap-1.5">
          <span class="text-[13px] font-semibold text-navy">Nome</span>
          <input v-model="dados.nome" type="text" required maxlength="150" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-[13px] font-semibold text-navy">E-mail</span>
          <input v-model="dados.email" type="email" required :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span class="text-[13px] font-semibold text-navy">Senha</span>
          <input v-model="dados.senha" type="password" minlength="6" :required="!form.id"
            :placeholder="form.id ? 'Deixe vazio para manter' : 'Mínimo de 6 caracteres'" :class="campo" />
        </label>
      </div>
      <p v-if="erroForm" class="mt-3 text-sm font-medium text-red-700">{{ erroForm }}</p>
      <div class="mt-5 flex flex-wrap items-center gap-3">
        <button type="submit" :disabled="salvando" class="rounded-lg bg-ocean px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-navy disabled:opacity-60">
          {{ salvando ? 'Salvando…' : 'Salvar' }}
        </button>
        <button type="button" @click="fechar" class="rounded-lg px-4 py-2.5 text-sm font-semibold text-navy/70 hover:text-navy">Cancelar</button>
        <button v-if="form.id && form.id !== auth.usuario?.id" type="button" :disabled="salvando" @click="excluir"
          class="ml-auto rounded-lg px-4 py-2.5 text-sm font-semibold text-red-700 transition hover:bg-red-600/10 disabled:opacity-60">
          Excluir usuário
        </button>
      </div>
    </form>

    <div class="mt-6 overflow-hidden rounded-xl border border-navy/10 bg-white">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[560px] border-collapse">
          <thead>
            <tr class="border-b border-navy/10 text-[11px] font-semibold uppercase tracking-wide text-navy/50">
              <th class="px-6 py-3.5 text-left">Pessoa</th>
              <th class="px-4 py-3.5 text-left">Sessão</th>
              <th class="px-6 py-3.5"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="carregando">
              <td colspan="3" class="px-6 py-6 text-center text-sm text-navy/55">Carregando usuários…</td>
            </tr>
            <tr v-else-if="erroLista">
              <td colspan="3" class="px-6 py-6 text-center text-sm font-medium text-red-700">{{ erroLista }}</td>
            </tr>
            <template v-else>
            <tr v-for="u in usuarios" :key="u.id" class="border-b border-navy/5 last:border-0 hover:bg-mist">
              <td class="px-6 py-3.5">
                <div class="flex items-center gap-3">
                  <div class="flex h-9 w-9 flex-none items-center justify-center rounded-full bg-navy/5 text-xs font-bold text-navy">{{ iniciais(u.nome) }}</div>
                  <div>
                    <div class="text-sm font-semibold text-navy">{{ u.nome }}</div>
                    <div class="text-[12.5px] text-navy/55">{{ u.email }}</div>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3.5 text-[13px] text-navy/60">{{ ehVoce(u) ? 'Você' : '—' }}</td>
              <td class="px-6 py-3.5 text-right"><button type="button" @click="abrir(u)" class="whitespace-nowrap text-[13px] font-semibold text-ocean hover:text-navy">Gerenciar</button></td>
            </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <div class="mt-4 grid gap-4 sm:grid-cols-3">
      <div v-for="p in papeis" :key="p.t" class="rounded-xl border border-navy/10 bg-white px-5 py-4">
        <div class="text-[13px] font-bold" :class="p.c">{{ p.t }}</div>
        <p class="mt-2 text-[13px] leading-relaxed text-navy/60">{{ p.d }}</p>
      </div>
    </div>
  </div>
</template>
