<script setup>
import { usuarios } from '../../data/mock';

const papeis = [
  { t: 'Administrador', d: 'Gerencia usuários, integrações e todas as planilhas da empresa.', c: 'text-navy' },
  { t: 'Analista', d: 'Envia planilhas, cria dashboards e compartilha relatórios.', c: 'text-ocean' },
  { t: 'Leitor', d: 'Visualiza dashboards já publicados, sem editar dados.', c: 'text-sky' },
];
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-3xl font-extrabold tracking-[-0.03em] text-navy">Usuários</h1>
        <p class="mt-2 text-[15px] text-navy/60">5 pessoas com acesso · 1 administrador</p>
      </div>
      <button type="button" class="rounded-full bg-ocean px-6 py-3 text-sm font-bold text-white hover:bg-sky">Convidar usuário</button>
    </div>

    <div class="mt-6 overflow-hidden rounded-2xl border border-navy/10 bg-white">
      <div class="overflow-x-auto"><table class="w-full min-w-[720px] border-collapse">
        <thead>
          <tr class="border-b border-navy/10 text-[11.5px] font-bold tracking-[0.06em] text-navy/50">
            <th class="px-6 py-3.5 text-left">PESSOA</th>
            <th class="px-4 py-3.5 text-left">PAPEL</th>
            <th class="px-4 py-3.5 text-left">ÚLTIMO ACESSO</th>
            <th class="px-6 py-3.5"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in usuarios" :key="u.email" class="border-b border-navy/5 hover:bg-mist">
            <td class="px-6 py-3.5">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 flex-none items-center justify-center rounded-full bg-navy/5 text-[12.5px] font-extrabold text-navy">{{ u.inicial }}</div>
                <div>
                  <div class="text-sm font-bold text-navy">{{ u.nome }}</div>
                  <div class="text-[12.5px] text-navy/55">{{ u.email }}</div>
                </div>
              </div>
            </td>
            <td class="px-4 py-3.5"><span class="whitespace-nowrap rounded-full px-2.5 py-[5px] text-xs font-bold" :class="u.badge">{{ u.papel }}</span></td>
            <td class="px-4 py-3.5 text-[13.5px] text-navy/60">{{ u.acesso }}</td>
            <td class="px-6 py-3.5 text-right"><a href="#" class="whitespace-nowrap text-[13.5px] font-bold text-ocean">Gerenciar</a></td>
          </tr>
        </tbody>
      </table></div>
    </div>

    <div class="mt-4 grid gap-4 sm:grid-cols-3">
      <div v-for="p in papeis" :key="p.t" class="rounded-2xl border border-navy/10 bg-white px-[22px] py-5">
        <div class="text-[13.5px] font-extrabold" :class="p.c">{{ p.t }}</div>
        <p class="mt-2 text-[13.5px] leading-relaxed text-navy/60">{{ p.d }}</p>
      </div>
    </div>
  </div>
</template>
