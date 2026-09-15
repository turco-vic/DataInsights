<script setup>
import { computed } from 'vue';
import StatusBadge from '../../components/StatusBadge.vue';
import { useAuthStore } from '../../stores/authStore';

// Sem backend não existe cadastro de usuários: a lista mostra quem está logado
// neste navegador. Quando a API existir, vira GET /api/usuarios.
const auth = useAuthStore();
const usuarios = computed(() =>
  auth.usuario ? [{ ...auth.usuario, acesso: 'Sessão atual' }] : [],
);

const papeis = [
  { t: 'Administrador', d: 'Gerencia usuários, integrações e todas as planilhas da empresa.', c: 'text-navy' },
  { t: 'Analista', d: 'Envia planilhas, cria dashboards e compartilha relatórios.', c: 'text-ocean' },
  { t: 'Leitor', d: 'Visualiza dashboards já publicados, sem editar dados.', c: 'text-sky' },
];

function convidar() {
  // TODO: abrir formulário de convite (backend envia o e-mail).
  alert('O convite de usuários ficará disponível quando o backend estiver conectado.');
}
function gerenciar(usuario) {
  // TODO: abrir gestão de permissões do usuário.
  alert(`Gestão de ${usuario.nome} ficará disponível quando o backend estiver conectado.`);
}
</script>

<template>
  <div>
    <div class="flex flex-wrap items-end justify-between gap-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-navy sm:text-3xl">Usuários</h1>
        <p class="mt-1.5 text-sm text-navy/60">{{ usuarios.length }} pessoa(s) com acesso neste navegador</p>
      </div>
      <button type="button" @click="convidar" class="rounded-lg bg-ocean px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-navy">Convidar usuário</button>
    </div>

    <div class="mt-6 overflow-hidden rounded-xl border border-navy/10 bg-white">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[720px] border-collapse">
          <thead>
            <tr class="border-b border-navy/10 text-[11px] font-semibold uppercase tracking-wide text-navy/50">
              <th class="px-6 py-3.5 text-left">Pessoa</th>
              <th class="px-4 py-3.5 text-left">Papel</th>
              <th class="px-4 py-3.5 text-left">Último acesso</th>
              <th class="px-6 py-3.5"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in usuarios" :key="u.email" class="border-b border-navy/5 last:border-0 hover:bg-mist">
              <td class="px-6 py-3.5">
                <div class="flex items-center gap-3">
                  <div class="flex h-9 w-9 flex-none items-center justify-center rounded-full bg-navy/5 text-xs font-bold text-navy">{{ u.inicial }}</div>
                  <div>
                    <div class="text-sm font-semibold text-navy">{{ u.nome }}</div>
                    <div class="text-[12.5px] text-navy/55">{{ u.email }}</div>
                  </div>
                </div>
              </td>
              <td class="px-4 py-3.5"><StatusBadge :status="u.papel" /></td>
              <td class="px-4 py-3.5 text-[13px] text-navy/60">{{ u.acesso }}</td>
              <td class="px-6 py-3.5 text-right"><button type="button" @click="gerenciar(u)" class="whitespace-nowrap text-[13px] font-semibold text-ocean hover:text-navy">Gerenciar</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="mt-4 grid gap-4 sm:grid-cols-3">
      <div v-for="p in papeis" :key="p.t" class="rounded-xl border border-navy/10 bg-white px-5 py-4">
        <div class="text-[13px] font-bold" :class="p.c">{{ p.t }}</div>
        <p class="mt-2 text-[13px] leading-relaxed text-navy/60">{{ p.d }}</p>
      </div>
    </div>
  </div>
</template>
