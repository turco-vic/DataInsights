<script setup>
</script>

<template>
    <header class="sticky top-0 z-20 border-b border-navy/10 bg-mist/90 backdrop-blur">
        <div class="mx-auto flex max-w-[1180px] items-center justify-between gap-6 px-8 py-4">
            <router-link to="/"><span class="text-xl font-bold text-[#1A2A4F]">DataInsights</span></router-link>
            <nav class="flex items-center gap-8">
                <a href="#inicio" class="text-[15px] font-semibold tracking-tight text-navy hover:text-sky">Início</a>
                <a href="#sobre" class="text-[15px] font-semibold tracking-tight text-navy hover:text-sky">Sobre</a>
                <router-link to="/login"
                    class="rounded-full bg-navy px-[26px] py-[11px] text-[15px] font-bold tracking-tight text-white transition hover:-translate-y-px hover:bg-ocean">
                    Login
                </router-link>
            </nav>
        </div>
    </header>
</template>
