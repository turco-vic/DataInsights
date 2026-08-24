<script setup>
import { RouterLink } from 'vue-router';
</script>

<template>
    <div class="flex min-h-screen items-center justify-center bg-mist px-6 py-14 font-sans text-navy">
        <div class="w-full max-w-[460px]">
            <RouterLink to="/login"
                class="mb-7 inline-flex items-center gap-2 text-sm font-semibold text-navy/60 hover:text-sky">
                <span class="text-base leading-none">←</span> Voltar ao login
            </RouterLink>
            <div
                class="rounded-[20px] border border-navy/10 bg-white p-10 shadow-[0_40px_80px_-60px_rgba(0,16,68,0.5)]">
                <div
                    class="flex h-[46px] w-[46px] items-center justify-center rounded-xl bg-sky/15 text-[19px] font-extrabold text-ocean">
                    ✉</div>
                <h1 class="mt-6 text-[27px] font-extrabold tracking-[-0.03em] text-navy">Recuperar senha</h1>
                <p class="mt-2.5 text-[15px] leading-relaxed text-navy/60">Informe seu e-mail corporativo. Enviaremos um
                    link para redefinir a senha, válido por 30 minutos.</p>
                <form class="mt-7 flex flex-col gap-[18px]" @submit.prevent>
                    <label class="flex flex-col gap-2">
                        <span class="text-[13px] font-bold text-navy">E-mail corporativo</span>
                        <input type="email" placeholder="nome@empresa.com"
                            class="w-full rounded-[11px] border border-navy/15 bg-mist p-3.5 text-[15px] text-navy outline-none transition focus:border-sky focus:bg-white" />
                    </label>
                    <button type="submit"
                        class="w-full rounded-full bg-ocean px-6 py-4 text-base font-bold text-white shadow-[0_12px_28px_-12px_rgba(0,75,133,0.75)] transition hover:-translate-y-0.5 hover:bg-sky">Enviar
                        link de recuperação</button>
                </form>
                <div class="mt-6 border-t border-navy/10 pt-5 text-sm text-navy/60">Não recebeu? Verifique a caixa de
                    spam ou <a href="#" class="font-bold">tente outro e-mail</a>.</div>
            </div>
        </div>
    </div>
</template>
