<script setup>
// Traduz um enum de status/papel vindo dos dados em um selo visual.
// Mantém as classes Tailwind fora dos dados (que agora são DTOs puros).
defineProps({ status: { type: String, required: true } });

const MAPA = {
  // status de planilha / cliente
  processado: { label: 'Processado', cls: 'bg-emerald-600/10 text-emerald-700' },
  processando: { label: 'Processando', cls: 'bg-sky/15 text-ocean' },
  erro: { label: 'Com erros', cls: 'bg-red-600/10 text-red-700' },
  ativo: { label: 'Ativo', cls: 'bg-emerald-600/10 text-emerald-700' },
  revisao: { label: 'Em revisão', cls: 'bg-sky/15 text-ocean' },
  inativo: { label: 'Inativo', cls: 'bg-navy/10 text-navy/60' },
  // papéis de usuário
  admin: { label: 'Administrador', cls: 'bg-navy text-white' },
  analista: { label: 'Analista', cls: 'bg-sky/15 text-ocean' },
  leitor: { label: 'Leitor', cls: 'bg-navy/10 text-navy/65' },
};

const desconhecido = { label: '—', cls: 'bg-navy/10 text-navy/60' };
</script>

<template>
  <span
    class="inline-block whitespace-nowrap rounded-full px-2.5 py-1 text-xs font-bold"
    :class="(MAPA[status] || desconhecido).cls"
  >{{ (MAPA[status] || desconhecido).label }}</span>
</template>
