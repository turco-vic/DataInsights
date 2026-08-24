<script setup>
import { RouterLink, RouterView, useRoute } from 'vue-router';
import AppLogo from '../components/AppLogo.vue';

const route = useRoute();
const nav = [
    { to: '/app', label: 'Dashboard' },
    { to: '/app/upload', label: 'Upload de planilha' },
    { to: '/app/planilhas', label: 'Planilhas enviadas' },
    { to: '/app/dataset', label: 'Relatório do dataset' },
    { to: '/app/usuarios', label: 'Usuários' },
    { to: '/app/configuracoes', label: 'Configurações' },
];
const ativo = (to) => route.path === to;
</script>

<template>
    <div class="flex min-h-screen bg-mist font-sans text-navy">
        <aside
            class="sticky top-0 flex h-screen w-[252px] flex-none flex-col justify-between border-r border-navy/10 bg-white py-6">
            <div>
                <RouterLink to="/" class="block px-[22px] pb-[26px]">
                    <AppLogo />
                </RouterLink>
                <div class="mb-2.5 px-3.5 text-[11px] font-bold tracking-[0.08em] text-navy/40">NAVEGAÇÃO</div>
                <nav class="flex flex-col gap-0.5 px-3">
                    <RouterLink v-for="n in nav" :key="n.to" :to="n.to"
                        class="flex items-center gap-3 rounded-[10px] px-3 py-[11px] text-[14.5px] hover:bg-navy/5"
                        :class="ativo(n.to) ? 'bg-sky/10 font-bold text-ocean' : 'font-semibold text-navy/65'">
                        <span class="h-4 w-[3px] flex-none rounded-sm"
                            :class="ativo(n.to) ? 'bg-sky' : 'bg-transparent'"></span>{{ n.label }}
                    </RouterLink>
                </nav>
            </div>

            <div class="px-4">
                <RouterLink to="/" class="block text-[13.5px] font-semibold text-navy/55 hover:text-sky">Sair da conta
                </RouterLink>
            </div>
        </aside>

        <div class="flex min-w-0 flex-1 flex-col">
            <header
                class="sticky top-0 z-10 flex items-center gap-6 border-b border-navy/10 bg-mist/90 px-8 py-3.5 backdrop-blur">
                <div
                    class="flex max-w-[420px] flex-1 items-center gap-2.5 rounded-[10px] border border-navy/15 bg-white px-3.5 py-2.5">
                    <span class="text-sm text-navy/40">⌕</span>
                    <input type="text" placeholder="Buscar planilhas, colunas ou clientes"
                        class="min-w-0 flex-1 border-none bg-transparent text-sm text-navy outline-none" />
                </div>
                <div class="ml-auto flex items-center gap-[18px]">
                    <RouterLink to="/app/upload"
                        class="rounded-full bg-ocean px-5 py-2.5 text-sm font-bold text-white transition hover:bg-sky">
                        Nova planilha</RouterLink>
                    <div class="flex items-center gap-2.5">
                        <div
                            class="flex h-[34px] w-[34px] items-center justify-center rounded-full bg-navy text-[12.5px] font-extrabold text-white">
                            AR</div>
                        <div>
                            <div class="text-[13.5px] font-bold leading-tight text-navy">Ana Ribeiro</div>
                            <div class="text-[11.5px] text-navy/50">Administrador</div>
                        </div>
                    </div>
                </div>
            </header>

            <main class="max-w-[1240px] flex-1 p-8">
                <RouterView />
            </main>
        </div>
    </div>
</template>
