<script setup>
import { ref } from 'vue';
import { RouterLink } from 'vue-router';
import Icon from '../components/Icon.vue';

const email = ref('');
const enviado = ref(false);

function enviar() {
  // TODO: backend dispara o e-mail de recuperação.
  enviado.value = true;
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-mist px-6 py-14 text-navy">
    <div class="w-full max-w-[460px]">
      <RouterLink to="/login" class="mb-7 inline-flex items-center gap-2 text-sm font-medium text-navy/60 hover:text-ocean">
        <Icon name="arrow-left" class="h-4 w-4" /> Voltar ao login
      </RouterLink>
      <div class="rounded-2xl border border-navy/10 bg-white p-8 shadow-[0_40px_80px_-60px_rgba(20,35,63,0.5)] sm:p-10">
        <div class="flex h-11 w-11 items-center justify-center rounded-xl bg-sky/15 text-ocean">
          <Icon name="mail" class="h-5 w-5" />
        </div>

        <template v-if="!enviado">
          <h1 class="mt-6 text-[26px] font-bold tracking-tight text-navy">Recuperar senha</h1>
          <p class="mt-2 text-[15px] leading-relaxed text-navy/60">Informe seu e-mail corporativo. Enviaremos um link para redefinir a senha, válido por 30 minutos.</p>
          <form class="mt-7 flex flex-col gap-4" @submit.prevent="enviar">
            <label class="flex flex-col gap-2">
              <span class="text-[13px] font-semibold text-navy">E-mail corporativo</span>
              <input v-model="email" type="email" required placeholder="nome@empresa.com"
                class="w-full rounded-lg border border-navy/15 bg-mist p-3 text-[15px] text-navy outline-none transition focus:border-sky focus:bg-white focus:ring-4 focus:ring-sky/10" />
            </label>
            <button type="submit" class="w-full rounded-lg bg-ocean px-6 py-3.5 text-base font-semibold text-white transition hover:bg-navy">Enviar link de recuperação</button>
          </form>
        </template>

        <template v-else>
          <h1 class="mt-6 text-[26px] font-bold tracking-tight text-navy">Verifique seu e-mail</h1>
          <p class="mt-2 text-[15px] leading-relaxed text-navy/60">
            Se <span class="font-semibold text-navy">{{ email }}</span> estiver cadastrado, enviamos um link para redefinir a senha. Ele expira em 30 minutos.
          </p>
          <RouterLink to="/login" class="mt-7 inline-block rounded-lg bg-ocean px-6 py-3 text-sm font-semibold text-white transition hover:bg-navy">Voltar ao login</RouterLink>
        </template>

        <div class="mt-6 border-t border-navy/10 pt-5 text-sm text-navy/60">
          Não recebeu? Verifique a caixa de spam ou
          <button type="button" @click="enviado = false" class="font-semibold text-ocean hover:text-navy">tente outro e-mail</button>.
        </div>
      </div>
    </div>
  </div>
</template>
