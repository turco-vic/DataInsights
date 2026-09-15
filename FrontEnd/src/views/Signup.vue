<script setup>
import { ref, computed } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import AppLogo from '../components/AppLogo.vue';
import Icon from '../components/Icon.vue';
import { useAuthStore } from '../stores/authStore';

const router = useRouter();
const auth = useAuthStore();
const nome = ref('');
const empresa = ref('');
const email = ref('');
const senha = ref('');
const aceito = ref(false);

const campo = 'w-full rounded-lg border border-navy/15 bg-mist p-3 text-[15px] text-navy outline-none transition focus:border-sky focus:bg-white focus:ring-4 focus:ring-sky/10';

const forca = computed(() => {
  const s = senha.value;
  if (!s) return 0;
  let n = 1;
  if (s.length >= 8) n++;
  if (/[0-9]/.test(s) || /[^A-Za-z0-9]/.test(s)) n++;
  return n;
});
const forcaTom = ['bg-navy/15', 'bg-red-500', 'bg-amber-500', 'bg-emerald-600'];

async function criarConta() {
  if (await auth.login(email.value.trim(), senha.value, nome.value)) router.push('/app');
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-mist px-6 py-14 text-navy">
    <div class="w-full max-w-[520px]">
      <RouterLink to="/" class="mb-7 inline-flex items-center gap-2 text-sm font-medium text-navy/60 hover:text-ocean">
        <Icon name="arrow-left" class="h-4 w-4" /> Voltar ao site
      </RouterLink>
      <div class="rounded-2xl border border-navy/10 bg-white px-8 pb-8 pt-9 shadow-[0_40px_80px_-60px_rgba(20,35,63,0.5)] sm:px-10">
        <AppLogo size="text-[22px]" class="mb-6 block" />
        <h1 class="text-[28px] font-bold tracking-tight text-navy">Criar acesso</h1>
        <p class="mt-2 text-[15px] leading-relaxed text-navy/60">Leva menos de um minuto. Você poderá enviar a primeira planilha em seguida.</p>

        <form class="mt-7 flex flex-col gap-4" @submit.prevent="criarConta">
          <div class="grid gap-4 sm:grid-cols-2">
            <label class="flex flex-col gap-2">
              <span class="text-[13px] font-semibold text-navy">Nome completo</span>
              <input v-model="nome" type="text" required placeholder="Ana Ribeiro" :class="campo" />
            </label>
            <label class="flex flex-col gap-2">
              <span class="text-[13px] font-semibold text-navy">Empresa</span>
              <input v-model="empresa" type="text" placeholder="Nome da empresa" :class="campo" />
            </label>
          </div>
          <label class="flex flex-col gap-2">
            <span class="text-[13px] font-semibold text-navy">E-mail corporativo</span>
            <input v-model="email" type="email" required placeholder="nome@empresa.com" :class="campo" />
          </label>
          <label class="flex flex-col gap-2">
            <span class="text-[13px] font-semibold text-navy">Senha</span>
            <input v-model="senha" type="password" required placeholder="Mínimo de 8 caracteres" :class="campo" />
            <span class="mt-0.5 flex gap-1.5">
              <span v-for="i in 3" :key="i" class="h-1 flex-1 rounded-sm transition-colors" :class="i <= forca ? forcaTom[forca] : 'bg-navy/15'"></span>
            </span>
          </label>
          <label class="flex cursor-pointer items-start gap-2.5 text-[13px] leading-snug text-navy/70">
            <input v-model="aceito" type="checkbox" required class="mt-0.5 h-4 w-4 accent-ocean" />
            Concordo com os <a href="#termos" class="font-semibold text-ocean">termos de uso</a> e a <a href="#privacidade" class="font-semibold text-ocean">política de privacidade</a>.
          </label>
          <button type="submit" class="w-full rounded-lg bg-ocean px-6 py-3.5 text-base font-semibold text-white transition hover:bg-navy">Criar conta</button>
        </form>

        <div class="mt-6 border-t border-navy/10 pt-5 text-sm text-navy/60">
          Já tem acesso? <RouterLink to="/login" class="font-semibold text-ocean hover:text-navy">Entrar</RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>
