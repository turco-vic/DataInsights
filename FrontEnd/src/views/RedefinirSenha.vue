<script setup>
import { ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import Icon from '../components/Icon.vue';
import { mensagemErro } from '../services/api';
import { authService } from '../services/auth';

const route = useRoute();
const token = typeof route.query.token === 'string' ? route.query.token : '';

const senha = ref('');
const confirmacao = ref('');
const enviando = ref(false);
const concluido = ref(false);
const mensagem = ref('');

const campo = 'w-full rounded-lg border border-navy/15 bg-mist p-3 text-[15px] text-navy outline-none transition focus:border-sky focus:bg-white focus:ring-4 focus:ring-sky/10';

async function redefinir() {
  mensagem.value = '';
  if (senha.value !== confirmacao.value) {
    mensagem.value = 'A confirmação não é igual à nova senha.';
    return;
  }
  enviando.value = true;
  try {
    await authService.redefinirSenha(token, senha.value);
    concluido.value = true;
  } catch (error) {
    mensagem.value = mensagemErro(error, 'Não foi possível redefinir a senha.');
  } finally {
    enviando.value = false;
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-mist px-6 py-14 text-navy">
    <div class="w-full max-w-[460px]">
      <div class="rounded-2xl border border-navy/10 bg-white p-8 shadow-[0_40px_80px_-60px_rgba(20,35,63,0.5)] sm:p-10">
        <div class="flex h-11 w-11 items-center justify-center rounded-xl bg-sky/15 text-ocean">
          <Icon :name="concluido ? 'check' : 'mail'" class="h-5 w-5" />
        </div>

        <template v-if="!token">
          <h1 class="mt-6 text-[26px] font-bold tracking-tight text-navy">Link incompleto</h1>
          <p class="mt-2 text-[15px] leading-relaxed text-navy/60">Abra o link exatamente como chegou no e-mail, ou peça um novo.</p>
          <RouterLink to="/recuperar-senha" class="mt-7 inline-block rounded-lg bg-ocean px-6 py-3 text-sm font-semibold text-white transition hover:bg-navy">Pedir novo link</RouterLink>
        </template>

        <template v-else-if="concluido">
          <h1 class="mt-6 text-[26px] font-bold tracking-tight text-navy">Senha redefinida</h1>
          <p class="mt-2 text-[15px] leading-relaxed text-navy/60">Pronto. Entre com a nova senha.</p>
          <RouterLink to="/login" class="mt-7 inline-block rounded-lg bg-ocean px-6 py-3 text-sm font-semibold text-white transition hover:bg-navy">Ir para o login</RouterLink>
        </template>

        <template v-else>
          <h1 class="mt-6 text-[26px] font-bold tracking-tight text-navy">Criar nova senha</h1>
          <p class="mt-2 text-[15px] leading-relaxed text-navy/60">Escolha uma senha com pelo menos 6 caracteres.</p>
          <form class="mt-7 flex flex-col gap-4" @submit.prevent="redefinir">
            <label class="flex flex-col gap-2">
              <span class="text-[13px] font-semibold text-navy">Nova senha</span>
              <input v-model="senha" type="password" required minlength="6" autocomplete="new-password" :class="campo" />
            </label>
            <label class="flex flex-col gap-2">
              <span class="text-[13px] font-semibold text-navy">Confirme a nova senha</span>
              <input v-model="confirmacao" type="password" required minlength="6" autocomplete="new-password" :class="campo" />
            </label>
            <button type="submit" :disabled="enviando" class="w-full rounded-lg bg-ocean px-6 py-3.5 text-base font-semibold text-white transition hover:bg-navy disabled:opacity-60">
              {{ enviando ? 'Salvando…' : 'Salvar nova senha' }}
            </button>
            <p v-if="mensagem" class="text-sm font-medium text-red-700">
              {{ mensagem }}
              <RouterLink to="/recuperar-senha" class="font-semibold text-ocean hover:text-navy">Pedir novo link</RouterLink>
            </p>
          </form>
        </template>
      </div>
    </div>
  </div>
</template>
