<script setup>
import { reactive } from 'vue';
import { NIVEIS, SEGMENTOS, UFS } from '../../utils/dominio';
import { botaoPrimario, botaoSecundario, campo, rotulo } from '../../utils/estilos';

const props = defineProps({
  inicial: { type: Object, default: null },
  consultores: { type: Array, default: () => [] },
  salvando: { type: Boolean, default: false },
  erro: { type: String, default: '' },
});
const emit = defineEmits(['salvar', 'cancelar']);

const c = props.inicial || {};
const dados = reactive({
  codigo: c.codigo || '',
  nome: c.nome || '',
  email: c.email || '',
  segmento: c.segmento || '',
  nivel: c.nivel || '',
  faturamentoAnual: c.faturamentoAnual ?? '',
  dataContratacao: c.dataContratacao || '',
  cidade: c.cidade || '',
  uf: c.uf || '',
  consultorId: c.consultor?.id ?? '',
});

const hoje = new Date().toISOString().slice(0, 10);
const ouNulo = (v) => (v === '' || v === null || v === undefined ? null : v);

function enviar() {
  emit('salvar', {
    codigo: dados.codigo.trim().toUpperCase(),
    nome: dados.nome.trim(),
    email: dados.email.trim(),
    segmento: ouNulo(dados.segmento),
    nivel: ouNulo(dados.nivel),
    faturamentoAnual: dados.faturamentoAnual === '' ? null : Number(dados.faturamentoAnual),
    dataContratacao: ouNulo(dados.dataContratacao),
    cidade: ouNulo(dados.cidade.trim()),
    uf: ouNulo(dados.uf),
    consultorId: dados.consultorId === '' ? null : Number(dados.consultorId),
  });
}
</script>

<template>
  <form class="grid gap-4 sm:grid-cols-2" @submit.prevent="enviar">
    <label class="flex flex-col gap-1.5">
      <span :class="rotulo">Código</span>
      <input v-model="dados.codigo" required pattern="[Cc][Tt][Ii]\d{3,}" title="CTI seguido de números, ex.: CTI001" placeholder="CTI001" :class="campo" />
    </label>
    <label class="flex flex-col gap-1.5">
      <span :class="rotulo">Nome</span>
      <input v-model="dados.nome" required maxlength="150" :class="campo" />
    </label>
    <label class="flex flex-col gap-1.5 sm:col-span-2">
      <span :class="rotulo">E-mail</span>
      <input v-model="dados.email" type="email" required :class="campo" />
    </label>
    <label class="flex flex-col gap-1.5">
      <span :class="rotulo">Segmento</span>
      <select v-model="dados.segmento" :class="campo">
        <option value="">Não informado</option>
        <option v-for="s in SEGMENTOS" :key="s" :value="s">{{ s }}</option>
      </select>
    </label>
    <label class="flex flex-col gap-1.5">
      <span :class="rotulo">Nível</span>
      <select v-model="dados.nivel" :class="campo">
        <option value="">Não informado</option>
        <option v-for="n in NIVEIS" :key="n" :value="n">{{ n }}</option>
      </select>
    </label>
    <label class="flex flex-col gap-1.5">
      <span :class="rotulo">Faturamento anual (R$)</span>
      <input v-model="dados.faturamentoAnual" type="number" min="0" step="0.01" :class="campo" />
    </label>
    <label class="flex flex-col gap-1.5">
      <span :class="rotulo">Data de contratação</span>
      <input v-model="dados.dataContratacao" type="date" :max="hoje" :class="campo" />
    </label>
    <label class="flex flex-col gap-1.5">
      <span :class="rotulo">Cidade</span>
      <input v-model="dados.cidade" :class="campo" />
    </label>
    <label class="flex flex-col gap-1.5">
      <span :class="rotulo">UF</span>
      <select v-model="dados.uf" :class="campo">
        <option value="">—</option>
        <option v-for="u in UFS" :key="u" :value="u">{{ u }}</option>
      </select>
    </label>
    <label class="flex flex-col gap-1.5 sm:col-span-2">
      <span :class="rotulo">Consultor responsável</span>
      <select v-model="dados.consultorId" :class="campo">
        <option value="">Sem consultor</option>
        <option v-for="co in consultores" :key="co.id" :value="co.id">{{ co.nome }}</option>
      </select>
    </label>

    <p v-if="erro" class="text-sm font-medium text-red-700 sm:col-span-2">{{ erro }}</p>
    <div class="flex justify-end gap-3 sm:col-span-2">
      <button type="button" :class="botaoSecundario" @click="emit('cancelar')">Cancelar</button>
      <button type="submit" :disabled="salvando" :class="botaoPrimario">{{ salvando ? 'Salvando…' : 'Salvar' }}</button>
    </div>
  </form>
</template>
