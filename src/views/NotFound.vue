<script setup>
import { RouterLink } from 'vue-router';
</script>

<template>
    <div class="flex min-h-screen items-center justify-center bg-mist px-6 py-14 text-center font-sans text-navy">
        <div class="max-w-[480px]">
            <div class="text-[92px] font-extrabold leading-none tracking-[-0.05em] text-navy">404</div>
            <div class="mx-auto mt-[22px] h-[5px] w-16 rounded-full bg-sky"></div>
            <h1 class="mt-[26px] text-[27px] font-extrabold tracking-[-0.03em] text-navy">Página não encontrada</h1>
            <p class="mt-3 text-base leading-relaxed text-navy/60">O endereço não existe ou a planilha vinculada a ele
                foi removida.</p>
            <div class="mt-[30px] flex flex-wrap items-center justify-center gap-3.5">
                <RouterLink to="/app"
                    class="rounded-full bg-ocean px-[30px] py-[15px] text-[15px] font-bold text-white transition hover:bg-sky">
                    Ir para o dashboard</RouterLink>
                <RouterLink to="/"
                    class="border-b border-navy/25 pb-0.5 text-[15px] font-semibold text-navy hover:border-sky hover:text-sky">
                    Voltar ao site</RouterLink>
            </div>
        </div>
    </div>
</template>
