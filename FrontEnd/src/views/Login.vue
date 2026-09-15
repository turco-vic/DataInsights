<script setup>
import { ref } from 'vue';
import { RouterLink, useRouter, useRoute } from 'vue-router';
import AppLogo from '../components/AppLogo.vue';
import Icon from '../components/Icon.vue';
import { useAuthStore } from '../stores/authStore';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const email = ref('');
const senha = ref('');
const manterConectado = ref(true);
const mostrarSenha = ref(false);
const entrando = ref(false);
const mensagem = ref('');

async function entrar() {
  mensagem.value = '';
  entrando.value = true;
  const ok = await auth.login(email.value.trim(), senha.value);
  entrando.value = false;

  if (ok) router.push(route.query.redirect || '/app');
  else mensagem.value = 'Preencha e-mail e senha.';
}
</script>

<template>
  <div class="grid min-h-screen grid-cols-1 bg-mist text-navy lg:grid-cols-2">
    <!-- painel institucional -->
    <div class="hidden min-h-screen flex-col justify-between bg-navy px-15 py-14 lg:flex">
      <button type="button" @click="router.push('/')"
        class="inline-flex items-center gap-2 text-sm font-medium text-white/70 transition hover:text-white">
        <Icon name="arrow-left" class="h-4 w-4" /> Voltar ao site
      </button>

      <div class="max-w-md">
        <div class="mb-7 inline-flex items-center gap-2.5 rounded-full bg-sky/20 py-1.5 pl-3 pr-3.5">
          <span class="block h-1.5 w-1.5 rounded-full bg-sky"></span>
          <span class="text-[13px] font-semibold text-sky">Acesso restrito</span>
        </div>
        <h2 class="text-[clamp(30px,3.2vw,40px)] font-bold leading-tight tracking-tight text-white text-balance">
          Suas planilhas já viraram painéis.
        </h2>
        <p class="mt-4 text-[17px] leading-relaxed text-white/65">
          Entre para acompanhar indicadores, comparar períodos e enviar novos arquivos.
        </p>
        <div class="mt-10 flex flex-wrap gap-10">
          <div>
            <div class="text-[28px] font-bold tracking-tight text-white">37</div>
            <div class="mt-1 text-[13px] font-medium text-white/55">planilhas processadas</div>
          </div>
          <div>
            <div class="text-[28px] font-bold tracking-tight text-sky">12s</div>
            <div class="mt-1 text-[13px] font-medium text-white/55">tempo médio de análise</div>
          </div>
        </div>
      </div>

      <div class="text-[13px] text-white/40">© 2026 DataInsights</div>
    </div>

    <!-- formulário -->
    <div class="flex items-center justify-center px-6 py-14 sm:px-10">
      <div class="w-full max-w-105">
        <RouterLink to="/" class="mb-8 inline-flex items-center gap-2 text-sm font-medium text-navy/60 hover:text-ocean lg:hidden">
          <Icon name="arrow-left" class="h-4 w-4" /> Voltar ao site
        </RouterLink>
        <AppLogo size="text-2xl" class="mb-8 block" />
        <h1 class="text-3xl font-bold tracking-tight text-navy">Entrar na plataforma</h1>
        <p class="mt-2 text-[15px] leading-relaxed text-navy/60">Use seu e-mail corporativo.</p>

        <form class="mt-8 flex flex-col gap-5" @submit.prevent="entrar">
          <label class="flex flex-col gap-2">
            <span class="text-[13px] font-semibold text-navy">E-mail corporativo</span>
            <input v-model="email" type="email" required placeholder="nome@empresa.com"
              class="w-full rounded-lg border border-navy/15 bg-white px-4 py-3 text-base text-navy outline-none transition focus:border-sky focus:ring-4 focus:ring-sky/15" />
          </label>

          <label class="flex flex-col gap-2">
            <span class="flex items-center justify-between text-[13px] font-semibold text-navy">
              Senha
              <button type="button" @click="mostrarSenha = !mostrarSenha" class="text-xs font-semibold text-ocean hover:text-navy">
                {{ mostrarSenha ? 'Ocultar' : 'Mostrar' }}
              </button>
            </span>
            <input v-model="senha" :type="mostrarSenha ? 'text' : 'password'" required placeholder="••••••••"
              class="w-full rounded-lg border border-navy/15 bg-white px-4 py-3 text-base text-navy outline-none transition focus:border-sky focus:ring-4 focus:ring-sky/15" />
          </label>

          <div class="flex flex-wrap items-center justify-between gap-4">
            <label class="flex cursor-pointer items-center gap-2.5 text-sm font-medium text-navy/70">
              <input v-model="manterConectado" type="checkbox" class="h-4 w-4 accent-ocean" /> Manter conectado
            </label>
            <RouterLink to="/recuperar-senha" class="text-sm font-semibold text-ocean hover:text-navy">Esqueci minha senha</RouterLink>
          </div>

          <button type="submit" :disabled="entrando"
            class="w-full rounded-lg bg-ocean px-6 py-3.5 text-base font-semibold text-white transition hover:bg-navy disabled:opacity-60">
            {{ entrando ? 'Entrando…' : 'Entrar' }}
          </button>

          <p v-if="mensagem" class="text-sm font-medium text-red-700">{{ mensagem }}</p>
        </form>

        <div class="mt-6 border-t border-navy/10 pt-5 text-sm leading-relaxed text-navy/60">
          Ainda não tem acesso?
          <RouterLink to="/cadastro" class="font-semibold text-ocean hover:text-navy">Criar uma conta</RouterLink>.
        </div>
      </div>
    </div>
  </div>
</template>
