<template>
    <footer class="border-t border-navy/10 bg-white">
        <div class="mx-auto flex max-w-[1180px] flex-wrap items-center justify-between gap-7 px-8 py-10">
            <div class="flex flex-wrap items-center gap-[18px]">
                <span class="text-xl font-bold text-[#1A2A4F]">DataInsights</span>
                <span class="text-sm text-navy/55">© 2026 DataInsights · Todos os direitos reservados.</span>
            </div>
            <nav class="flex flex-wrap items-center gap-[26px]">
                <a v-for="l in links" :key="l.href" :href="l.href"
                    class="text-sm font-semibold text-navy/70 hover:text-sky">{{ l.t }}</a>
            </nav>
        </div>
    </footer>
</template>

<script setup>
const links = [
    { t: 'Sobre', href: '#sobre' },
    { t: 'Privacidade', href: '#privacidade' },
    { t: 'Termos de uso', href: '#termos' },
    { t: 'Contato', href: '#contato' },
];
</script>
