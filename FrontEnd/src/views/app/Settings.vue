<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import Modal from '../../components/ui/Modal.vue';
import api, { mensagemErro } from '../../services/api';
import { paraUsuario } from '../../services/auth';
import { useAuthStore } from '../../stores/authStore';
import { useUiStore } from '../../stores/uiStore';
import { PAPEIS } from '../../utils/dominio';
import { botaoPrimario, botaoSecundario, campo, rotulo } from '../../utils/estilos';
import { data as dataCurta, tempoDecorrido } from '../../utils/formato';

const auth = useAuthStore();
const ui = useUiStore();

const perfil = ref(null);
const carregando = ref(true);
const erro = ref('');
const salvando = ref(false);

const form = reactive({
  nome: '', email: '', empresa: '', cargo: '',
  prefTratarDados: true, prefGerarDashboard: true, prefResumoSemanal: false,
});

const ehConsultor = computed(() => perfil.value?.tipo === 'CONSULTOR');

const prefs = [
  { chave: 'prefTratarDados', t: 'Tratar dados automaticamente', d: 'Remove espaços extras e padroniza textos no upload. Desligado, as divergências aparecem como erros.' },
  { chave: 'prefGerarDashboard', t: 'Abrir o dashboard ao concluir', d: 'Depois de importar uma planilha, vai direto para os gráficos.' },
  { chave: 'prefResumoSemanal', t: 'Resumo semanal por e-mail', d: 'Toda segunda-feira às 8h, com as importações da semana e os números da carteira.' },
];

function preencher(p) {
  Object.assign(form, {
    nome: p.nome || '',
    email: p.email || '',
    empresa: p.empresa || '',
    cargo: p.cargo || '',
    prefTratarDados: p.prefTratarDados,
    prefGerarDashboard: p.prefGerarDashboard,
    prefResumoSemanal: p.prefResumoSemanal,
  });
}

async function carregar() {
  carregando.value = true;
  erro.value = '';
  try {
    perfil.value = (await api.get('/api/me')).data;
    preencher(perfil.value);
  } catch (e) {
    erro.value = mensagemErro(e, 'Não foi possível carregar o perfil.');
  } finally {
    carregando.value = false;
  }
}

async function salvar() {
  salvando.value = true;
  try {
    const corpo = { ...form, empresa: form.empresa || null, cargo: form.cargo || null };
    perfil.value = (await api.put('/api/me', corpo)).data;
    preencher(perfil.value);
    auth.atualizarUsuario(paraUsuario(perfil.value));
    ui.notificar('Alterações salvas.');
  } catch (e) {
    ui.notificar(mensagemErro(e), 'erro');
  } finally {
    salvando.value = false;
  }
}

// ----- Senha -----
const trocandoSenha = ref(false);
const senha = reactive({ atual: '', nova: '', confirmacao: '' });
const erroSenha = ref('');

function abrirSenha() {
  Object.assign(senha, { atual: '', nova: '', confirmacao: '' });
  erroSenha.value = '';
  trocandoSenha.value = true;
}

async function alterarSenha() {
  if (senha.nova !== senha.confirmacao) {
    erroSenha.value = 'A confirmação não é igual à nova senha.';
    return;
  }
  salvando.value = true;
  erroSenha.value = '';
  try {
    await api.put('/api/me/senha', { senhaAtual: senha.atual, novaSenha: senha.nova });
    trocandoSenha.value = false;
    ui.notificar('Senha alterada.');
    await carregar();
  } catch (e) {
    erroSenha.value = mensagemErro(e);
  } finally {
    salvando.value = false;
  }
}

onMounted(carregar);
</script>

<template>
  <div class="max-w-[760px]">
    <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Configurações</h1>
    <p class="mt-1.5 text-sm text-navy/60">Dados da conta, preferências de análise e segurança.</p>

    <p v-if="carregando && !perfil" class="mt-6 text-sm text-navy/55">Carregando…</p>
    <p v-else-if="erro" class="mt-6 rounded-xl border border-red-500/30 bg-red-50 px-5 py-4 text-sm font-medium text-red-800">{{ erro }}</p>

    <form v-else-if="perfil" @submit.prevent="salvar">
      <div class="mt-6 rounded-xl border border-navy/10 bg-white px-7 py-6">
        <div class="mb-5 flex flex-wrap items-baseline justify-between gap-3">
          <h2 class="text-base font-bold tracking-tight text-navy">Perfil</h2>
          <span class="rounded-full bg-navy/5 px-3 py-1 text-xs font-semibold text-navy/70">
            {{ PAPEIS[perfil.papel?.toLowerCase()]?.rotulo || perfil.papel }}
          </span>
        </div>
        <div class="grid gap-4 sm:grid-cols-2">
          <label class="flex flex-col gap-2">
            <span :class="rotulo">Nome</span>
            <input v-model="form.nome" type="text" required maxlength="150" :class="campo" />
          </label>
          <label class="flex flex-col gap-2">
            <span :class="rotulo">E-mail</span>
            <input v-model="form.email" type="email" required :class="campo" />
          </label>
          <template v-if="!ehConsultor">
            <label class="flex flex-col gap-2">
              <span :class="rotulo">Empresa</span>
              <input v-model="form.empresa" type="text" maxlength="150" :class="campo" />
            </label>
            <label class="flex flex-col gap-2">
              <span :class="rotulo">Cargo</span>
              <input v-model="form.cargo" type="text" maxlength="100" placeholder="Ex.: Controladoria" :class="campo" />
            </label>
          </template>
        </div>
      </div>

      <div v-if="!ehConsultor" class="mt-4 rounded-xl border border-navy/10 bg-white px-7 py-6">
        <h2 class="text-base font-bold tracking-tight text-navy">Preferências de análise</h2>
        <div class="flex flex-col divide-y divide-navy/5">
          <div v-for="p in prefs" :key="p.chave" class="flex items-center justify-between gap-5 py-4">
            <div :id="`pref-${p.chave}`">
              <div class="text-sm font-semibold text-navy">{{ p.t }}</div>
              <div class="mt-1 text-[13px] text-navy/60">{{ p.d }}</div>
            </div>
            <button
              type="button" role="switch" :aria-checked="form[p.chave]" :aria-labelledby="`pref-${p.chave}`"
              class="flex h-[26px] w-11 flex-none rounded-full p-0.5 transition"
              :class="form[p.chave] ? 'bg-ocean justify-end' : 'bg-navy/15 justify-start'"
              @click="form[p.chave] = !form[p.chave]"
            >
              <span class="block h-5 w-5 rounded-full bg-white shadow-sm"></span>
            </button>
          </div>
        </div>
      </div>

      <div class="mt-4 rounded-xl border border-navy/10 bg-white px-7 py-6">
        <h2 class="mb-4 text-base font-bold tracking-tight text-navy">Segurança</h2>
        <div class="flex flex-wrap items-center justify-between gap-5">
          <div>
            <div class="text-sm font-semibold text-navy">Senha</div>
            <div class="mt-1 text-[13px] text-navy/60">
              <template v-if="perfil.senhaAlteradaEm">Alterada {{ tempoDecorrido(perfil.senhaAlteradaEm) }} ({{ dataCurta(perfil.senhaAlteradaEm) }}).</template>
              <template v-else>Troque a senha periodicamente.</template>
            </div>
          </div>
          <button type="button" :class="botaoSecundario" @click="abrirSenha">Alterar senha</button>
        </div>
        <div class="mt-4 border-t border-navy/5 pt-4 text-[13px] text-navy/60">
          Sessão: {{ auth.manterConectado ? 'mantida neste navegador por até 7 dias' : 'termina ao fechar o navegador' }}.
          <span v-if="perfil.ultimoAcesso"> Último login: {{ dataCurta(perfil.ultimoAcesso) }}.</span>
        </div>
      </div>

      <div class="mt-6 flex items-center gap-4">
        <button type="submit" :disabled="salvando" :class="botaoPrimario">{{ salvando ? 'Salvando…' : 'Salvar alterações' }}</button>
        <button type="button" class="text-sm font-medium text-navy/60 transition hover:text-navy" @click="preencher(perfil)">Desfazer</button>
      </div>
    </form>

    <Modal :aberto="trocandoSenha" titulo="Alterar senha" largura="max-w-md" @fechar="trocandoSenha = false">
      <form class="flex flex-col gap-4" @submit.prevent="alterarSenha">
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Senha atual</span>
          <input v-model="senha.atual" type="password" required autocomplete="current-password" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Nova senha</span>
          <input v-model="senha.nova" type="password" required minlength="6" autocomplete="new-password" :class="campo" />
        </label>
        <label class="flex flex-col gap-1.5">
          <span :class="rotulo">Confirme a nova senha</span>
          <input v-model="senha.confirmacao" type="password" required minlength="6" autocomplete="new-password" :class="campo" />
        </label>
        <p v-if="erroSenha" class="text-sm font-medium text-red-700">{{ erroSenha }}</p>
        <div class="flex justify-end gap-3">
          <button type="button" :class="botaoSecundario" @click="trocandoSenha = false">Cancelar</button>
          <button type="submit" :disabled="salvando" :class="botaoPrimario">{{ salvando ? 'Salvando…' : 'Alterar senha' }}</button>
        </div>
      </form>
    </Modal>
  </div>
</template>
