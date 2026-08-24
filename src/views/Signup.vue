<script setup>
import { RouterLink, useRouter } from 'vue-router';
import AppLogo from '../components/AppLogo.vue';

const router = useRouter();
const campo = 'w-full rounded-[11px] border border-navy/15 bg-mist p-3.5 text-[15px] text-navy outline-none transition focus:border-sky focus:bg-white';
</script>

<template>
    <div class="flex min-h-screen items-center justify-center bg-mist px-6 py-14 font-sans text-navy">
        <div class="w-full max-w-[520px]">
            <RouterLink to="/"
                class="mb-7 inline-flex items-center gap-2 text-sm font-semibold text-navy/60 hover:text-sky">
                <span class="text-base leading-none">←</span> Voltar ao site
            </RouterLink>
            <div
                class="rounded-[20px] border border-navy/10 bg-white px-10 pb-9 pt-10 shadow-[0_40px_80px_-60px_rgba(0,16,68,0.5)]">
                <AppLogo size="text-[22px]" class="mb-[26px] block" />
                <h1 class="text-[28px] font-extrabold tracking-[-0.03em] text-navy">Criar acesso</h1>
                <p class="mt-2.5 text-[15px] leading-relaxed text-navy/60">Leva menos de um minuto. Você poderá enviar a
                    primeira planilha em seguida.</p>

                <form class="mt-[30px] flex flex-col gap-[18px]" @submit.prevent="router.push('/app')">
                    <div class="grid gap-[18px] sm:grid-cols-2">
                        <label class="flex flex-col gap-2">
                            <span class="text-[13px] font-bold text-navy">Nome completo</span>
                            <input type="text" placeholder="Ana Ribeiro" :class="campo" />
                        </label>
                        <label class="flex flex-col gap-2">
                            <span class="text-[13px] font-bold text-navy">Empresa</span>
                            <input type="text" placeholder="Nome da empresa" :class="campo" />
                        </label>
                    </div>
                    <label class="flex flex-col gap-2">
                        <span class="text-[13px] font-bold text-navy">E-mail corporativo</span>
                        <input type="email" placeholder="nome@empresa.com" :class="campo" />
                    </label>
                    <label class="flex flex-col gap-2">
                        <span class="text-[13px] font-bold text-navy">Senha</span>
                        <input type="password" placeholder="Mínimo de 8 caracteres" :class="campo" />
                        <span class="mt-0.5 flex gap-1.5">
                            <span class="h-1 flex-1 rounded-sm bg-ocean"></span>
                            <span class="h-1 flex-1 rounded-sm bg-ocean"></span>
                            <span class="h-1 flex-1 rounded-sm bg-navy/15"></span>
                        </span>
                    </label>
                    <label class="flex cursor-pointer items-start gap-2.5 text-[13.5px] leading-snug text-navy/70">
                        <input type="checkbox" class="mt-0.5 h-4 w-4 accent-ocean" />
                        Concordo com os <a href="#" class="font-bold">termos de uso</a> e a <a href="#"
                            class="font-bold">política de privacidade</a>.
                    </label>
                    <button type="submit"
                        class="w-full rounded-full bg-ocean px-6 py-4 text-base font-bold text-white shadow-[0_12px_28px_-12px_rgba(0,75,133,0.75)] transition hover:-translate-y-0.5 hover:bg-sky">Criar
                        conta</button>
                </form>

                <div class="mt-6 border-t border-navy/10 pt-5 text-sm text-navy/60">
                    Já tem acesso? <RouterLink to="/login" class="font-bold">Entrar</RouterLink>
                </div>
            </div>
        </div>
    </div>
</template>
