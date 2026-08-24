<script setup>
import { RouterLink } from 'vue-router';
import { arquivos } from '../../data/mock';

const filtros = ['Todos', 'Processados', 'Processando', 'Com erros'];
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-3xl font-extrabold tracking-[-0.03em] text-navy">Planilhas enviadas</h1>
        <p class="mt-2 text-[15px] text-navy/60">37 arquivos processados nos últimos 12 meses</p>
      </div>
      <RouterLink to="/app/upload" class="rounded-full bg-ocean px-6 py-3 text-sm font-bold text-white hover:bg-sky">Enviar planilha</RouterLink>
    </div>

    <div class="my-[22px] flex flex-wrap items-center gap-2">
      <span
        v-for="(f, i) in filtros" :key="f"
        class="rounded-full px-[15px] py-2 text-[12.5px]"
        :class="i === 0 ? 'bg-navy font-bold text-white' : 'border border-navy/15 bg-white font-semibold text-navy/65'"
      >{{ f }}</span>
    </div>

    <div class="overflow-hidden rounded-2xl border border-navy/10 bg-white">
      <div class="overflow-x-auto"><table class="w-full min-w-[720px] border-collapse">
        <thead>
          <tr class="border-b border-navy/10 text-[11.5px] font-bold tracking-[0.06em] text-navy/50">
            <th class="px-6 py-3.5 text-left">ARQUIVO</th>
            <th class="px-4 py-3.5 text-left">ENVIADO POR</th>
            <th class="px-4 py-3.5 text-left">DATA</th>
            <th class="px-4 py-3.5 text-right">LINHAS</th>
            <th class="px-4 py-3.5 text-right">STATUS</th>
            <th class="px-6 py-3.5"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in arquivos" :key="a.nome" class="border-b border-navy/5 hover:bg-mist">
            <td class="px-6 py-[15px] text-sm font-bold text-navy">{{ a.nome }}</td>
            <td class="px-4 py-[15px] text-sm text-navy/65">{{ a.autor }}</td>
            <td class="px-4 py-[15px] text-[13.5px] text-navy/55">{{ a.data }}</td>
            <td class="whitespace-nowrap px-4 py-[15px] text-right text-sm font-semibold text-navy">{{ a.linhas }}</td>
            <td class="px-4 py-[15px] text-right"><span class="whitespace-nowrap rounded-full px-2.5 py-[5px] text-xs font-bold" :class="a.badge">{{ a.status }}</span></td>
            <td class="px-6 py-[15px] text-right"><RouterLink to="/app/dataset" class="whitespace-nowrap text-[13.5px] font-bold text-ocean">Abrir</RouterLink></td>
          </tr>
        </tbody>
      </table></div>
    </div>
  </div>
</template>
