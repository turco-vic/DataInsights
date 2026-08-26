<script setup>
import { RouterLink } from 'vue-router';
import Header from '../components/Header.vue';
import Footer from '../components/Footer.vue';
import DashboardPreview from '../components/DashboardPreview.vue';

const passos = [
  { n: 1, t: 'Envie a planilha', d: 'Arraste o arquivo para a plataforma. Não é preciso preparar o layout nem renomear colunas.', bg: 'bg-navy/5', fg: 'text-navy' },
  { n: 2, t: 'O sistema trata os dados', d: 'Duplicidades, campos vazios e formatos divergentes são normalizados automaticamente.', bg: 'bg-ocean/10', fg: 'text-ocean' },
  { n: 3, t: 'Explore os dashboards', d: 'Gráficos interativos com filtros por período, unidade e setor, prontos para compartilhar.', bg: 'bg-sky/15', fg: 'text-sky' },
  { n: 4, t: 'Decida com evidência', d: 'Indicadores consolidados apoiam reuniões, relatórios e planejamento das unidades.', bg: 'bg-navy', fg: 'text-white' },
];

const numeros = [
  { v: '.xlsx', d: 'formatos aceitos: xlsx, xls e csv', c: 'text-navy' },
  { v: '0', d: 'linhas de código para configurar', c: 'text-ocean' },
  { v: '100%', d: 'acesso pelo navegador, sem instalação', c: 'text-sky' },
];

const cards = [
  { n: '01', t: 'Upload Simples', d: 'Arraste a planilha Excel e pronto. A plataforma lê, valida e trata os dados automaticamente, sem configuração.', bg: 'bg-navy' },
  { n: '02', t: 'Gráficos Automáticos', d: 'Dashboards e gráficos interativos gerados a partir dos seus dados, prontos para filtrar, comparar e compartilhar.', bg: 'bg-ocean' },
  { n: '03', t: 'Insights Estratégicos', d: 'Tendências, desvios e indicadores em destaque para apoiar decisões com base em evidências, não em suposições.', bg: 'bg-sky' },
];
</script>

<template>
  <div class="min-h-screen bg-mist text-navy">
    <Header />

    <section id="inicio"
      class="mx-auto grid max-w-[1180px] grid-cols-1 items-center gap-16 px-8 pb-24 pt-20 lg:grid-cols-2">
      <div>
        <div class="mb-6 inline-flex items-center gap-2.5 rounded-full bg-sky/10 py-1.5 pl-3 pr-3.5">
          <span class="block h-1.5 w-1.5 rounded-full bg-sky"></span>
          <span class="text-[13px] font-semibold text-ocean">Plataforma de análise de dados</span>
        </div>
        <h1 class="text-[clamp(38px,5vw,60px)] font-bold leading-[1.05] tracking-tight text-navy text-balance">
          Transforme Dados em Decisões
        </h1>
        <p class="mt-6 max-w-[480px] text-[19px] leading-relaxed text-navy/70 text-pretty">
          Envie suas planilhas Excel e receba insights instantâneos. O DataInsights trata os dados
          automaticamente e gera dashboards e gráficos interativos em segundos.
        </p>
        <div class="mt-9 flex flex-wrap items-center gap-4">
          <RouterLink to="/login"
            class="inline-flex items-center gap-2.5 rounded-lg bg-ocean px-8 py-4 text-base font-semibold text-white shadow-[0_12px_28px_-12px_rgba(37,99,168,0.75)] transition hover:-translate-y-0.5 hover:bg-navy">
            Acessar o Sistema
          </RouterLink>
          <a href="#sobre"
            class="border-b border-navy/25 pb-0.5 text-base font-medium text-navy transition hover:border-ocean hover:text-ocean">Como funciona</a>
        </div>
      </div>
      <DashboardPreview />
    </section>

    <section id="sobre" class="border-t border-navy/10 bg-white">
      <div class="mx-auto grid max-w-[1180px] grid-cols-1 items-start gap-16 px-8 pb-25 pt-24 lg:grid-cols-2">
        <div>
          <div class="mb-4 text-[13px] font-bold uppercase tracking-[0.1em] text-sky">Sobre a plataforma</div>
          <h2 class="text-[clamp(30px,3.6vw,42px)] font-bold leading-tight tracking-tight text-navy text-balance">
            Uma ponte entre a planilha e a decisão
          </h2>
          <p class="mt-5 text-lg leading-relaxed text-navy/70 text-pretty">
            O DataInsights é uma plataforma web para análise de dados operacionais. Ela recebe
            planilhas Excel, trata inconsistências, padroniza os campos e devolve dashboards interativos
            prontos para uso.
          </p>
          <p class="mt-4 text-lg leading-relaxed text-navy/70 text-pretty">
            O objetivo é eliminar o retrabalho manual entre a coleta e a apresentação dos números, para que
            as equipes gastem tempo interpretando resultados e não formatando tabelas.
          </p>
          <div class="mt-10 grid grid-cols-1 gap-6 border-t border-navy/10 pt-8 sm:grid-cols-3">
            <div v-for="n in numeros" :key="n.v">
              <div class="text-3xl font-bold tracking-tight" :class="n.c">{{ n.v }}</div>
              <div class="mt-1 text-sm font-medium text-navy/55">{{ n.d }}</div>
            </div>
          </div>
        </div>

        <div class="divide-y divide-navy/10">
          <div v-for="p in passos" :key="p.n" class="flex gap-5 py-6">
            <div class="flex h-10 w-10 flex-none items-center justify-center rounded-lg text-[15px] font-bold"
              :class="[p.bg, p.fg]">{{ p.n }}</div>
            <div>
              <h3 class="text-[19px] font-bold tracking-tight text-navy">{{ p.t }}</h3>
              <p class="mt-2 text-base leading-relaxed text-navy/60">{{ p.d }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section id="funcionalidades" class="border-y border-navy/10 bg-white">
      <div class="mx-auto max-w-[1180px] px-8 pb-26 pt-24">
        <div class="max-w-[620px]">
          <h2 class="text-[clamp(30px,3.6vw,42px)] font-bold leading-tight tracking-tight text-navy text-balance">
            Por que usar o DataInsights?
          </h2>
          <p class="mt-5 text-lg leading-relaxed text-navy/65">
            Do arquivo bruto ao painel pronto para apresentar, sem planilhas intermediárias e sem trabalho manual.
          </p>
        </div>
        <div class="mt-14 grid grid-cols-1 gap-6 md:grid-cols-3">
          <div v-for="c in cards" :key="c.n"
            class="rounded-2xl border border-navy/10 bg-mist px-8 pb-9 pt-8 transition hover:-translate-y-1 hover:border-sky/40 hover:shadow-[0_24px_48px_-32px_rgba(20,35,63,0.5)]">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl text-xl font-bold text-white" :class="c.bg">{{ c.n }}</div>
            <h3 class="mt-6 text-[22px] font-bold tracking-tight text-navy">{{ c.t }}</h3>
            <p class="mt-3 text-base leading-relaxed text-navy/65">{{ c.d }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="mx-auto max-w-[1180px] px-8 pb-24 pt-20">
      <div class="flex flex-wrap items-center justify-between gap-10 rounded-3xl bg-navy px-14 py-15">
        <div class="max-w-[560px]">
          <h2 class="text-[34px] font-bold leading-tight tracking-tight text-white text-balance">
            Sua próxima decisão começa em uma planilha
          </h2>
          <p class="mt-3.5 text-[17px] leading-relaxed text-white/70">Acesse com sua conta e envie o primeiro arquivo.</p>
        </div>
        <RouterLink to="/login"
          class="inline-flex items-center gap-2.5 rounded-lg bg-sky px-10 py-4 text-base font-semibold text-white transition hover:-translate-y-0.5 hover:bg-white hover:text-navy">
          Acessar o Sistema
        </RouterLink>
      </div>
    </section>

    <Footer />
  </div>
</template>
