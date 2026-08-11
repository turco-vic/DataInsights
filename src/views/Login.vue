<script setup>
</script>

<template>
    <div class="grid min-h-screen grid-cols-1 bg-mist font-sans text-navy lg:grid-cols-2">
        <div class="flex min-h-screen flex-col justify-between bg-navy px-[60px] py-14">
            <button type="button" @click="$emit('voltar')"
                class="inline-flex items-center gap-2.5 text-sm font-semibold text-white/70 hover:text-white">
                <a href="" class="text-sm font-semibold text-ocean hover:text-sky">←</a>
            </button>

            <div class="max-w-[440px]">
                <h2
                    class="text-[clamp(32px,3.4vw,42px)] font-extrabold leading-tight tracking-[-0.03em] text-white text-balance">
                    Suas planilhas já viraram painéis.
                </h2>
                <p class="mt-[18px] text-[17px] leading-relaxed text-white/65">
                    Entre para acompanhar indicadores, comparar períodos e enviar novos arquivos.
                </p>
                <div class="mt-11 flex flex-wrap gap-10">
                    <div>
                        <div class="text-[28px] font-extrabold tracking-tight text-white">37</div>
                        <div class="mt-1 text-[13px] font-semibold text-white/55">planilhas processadas</div>
                    </div>
                    <div>
                        <div class="text-[28px] font-extrabold tracking-tight text-sky">12s</div>
                        <div class="mt-1 text-[13px] font-semibold text-white/55">tempo médio de análise</div>
                    </div>
                </div>
            </div>

            <div class="text-[13px] text-white/40">© 2026 DataInsights</div>
        </div>

        <div class="flex items-center justify-center px-10 py-14">
            <div class="w-full max-w-[420px]">
                <span class="text-xl font-bold text-[#1A2A4F]">DataInsights</span>
                <h1 class="text-[32px] font-extrabold tracking-[-0.03em] text-navy">Entrar na plataforma</h1>
                <p class="mt-2.5 text-base leading-relaxed text-navy/60">Use seu e-mail.</p>

                <form class="mt-[34px] flex flex-col gap-5" @submit.prevent="entrar">
                    <label class="flex flex-col gap-2">
                        <span class="text-[13px] font-bold text-navy">E-mail</span>
                        <input v-model="email" type="email" placeholder="nome@email.com"
                            class="w-full rounded-xl border border-navy/15 bg-white px-4 py-[15px] text-base text-navy outline-none transition focus:border-sky focus:ring-4 focus:ring-sky/15" />
                    </label>

                    <label class="flex flex-col gap-2">
                        <span class="flex items-center justify-between text-[13px] font-bold text-navy">
                            Senha
                            <button type="button" @click="mostrarSenha = !mostrarSenha"
                                class="text-xs font-bold text-ocean hover:text-sky">
                                {{ mostrarSenha ? 'Ocultar' : 'Mostrar' }}
                            </button>
                        </span>
                        <input v-model="senha" :type="mostrarSenha ? 'text' : 'password'" placeholder="••••••••"
                            class="w-full rounded-xl border border-navy/15 bg-white px-4 py-[15px] text-base text-navy outline-none transition focus:border-sky focus:ring-4 focus:ring-sky/15" />
                    </label>

                    <div class="flex flex-wrap items-center justify-between gap-4">
                        <label class="flex cursor-pointer items-center gap-2.5 text-sm font-semibold text-navy/70">
                            <input v-model="manterConectado" type="checkbox" class="h-4 w-4 accent-ocean" /> Manter
                            conectado
                        </label>
                        <a href="#recuperar" class="text-sm font-semibold text-ocean hover:text-sky">Esqueci minha
                            senha</a>
                    </div>

                    <button type="submit"
                        class="w-full rounded-full bg-ocean px-6 py-[17px] text-base font-bold tracking-tight text-white shadow-[0_12px_28px_-12px_rgba(0,75,133,0.75)] transition hover:-translate-y-0.5 hover:bg-sky">
                        Entrar
                    </button>
                </form>

                <div class="mt-[26px] border-t border-navy/10 pt-[22px] text-sm leading-relaxed text-navy/60">
                    Ainda não tem acesso?
                    <a href="#contato" class="font-bold text-ocean hover:text-sky">Crie sua conta</a> ou
                    <a href="#contato" class="font-bold text-ocean hover:text-sky">fale com o suporte</a>
                </div>
            </div>
        </div>
    </div>
</template>
