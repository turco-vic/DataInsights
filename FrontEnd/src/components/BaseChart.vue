<script setup>
// Desenha UM gráfico com o Chart.js. A tela de Análise reutiliza este componente para cada gráfico.
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

const props = defineProps({
  tipo: { type: String, default: 'bar' }, // 'bar' | 'line' | 'doughnut'
  rotulos: { type: Array, default: () => [] }, // nomes (eixo X)
  valores: { type: Array, default: () => [] }, // números (altura das barras)
  titulo: { type: String, default: '' }, // nome da série (aparece no tooltip)
  horizontal: Boolean, // true = barras deitadas
  formatar: { type: Function, default: null }, // formata o número no eixo e no tooltip (ex.: moeda)
});

const canvas = ref(null);
let grafico = null;
// Mesmas cores do tema (style.css).
const CORES = ['#2563a8', '#16233f', '#4a9fe0', '#8fb8e6', '#5b6b85', '#c9d6e8'];

function desenhar() {
  grafico?.destroy();
  const rosca = props.tipo === 'doughnut';
  const texto = (v) => (props.formatar ? props.formatar(v) : v.toLocaleString('pt-BR'));
  grafico = new Chart(canvas.value, {
    type: props.tipo,
    data: {
      labels: props.rotulos,
      datasets: [{
        label: props.titulo,
        data: props.valores,
        backgroundColor: rosca ? CORES : '#2563a8',
        borderColor: rosca ? '#ffffff' : '#2563a8',
        borderRadius: rosca ? 0 : 4,
        tension: 0.3,
      }],
    },
    options: {
      indexAxis: props.horizontal ? 'y' : 'x',
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: rosca, position: 'right' },
        tooltip: { callbacks: { label: (c) => `${c.dataset.label || c.label}: ${texto(c.raw)}` } },
      },
      scales: rosca ? {} : {
        [props.horizontal ? 'x' : 'y']: { beginAtZero: true, ticks: { callback: (v) => texto(v), precision: 0 } },
      },
    },
  });
}

onMounted(desenhar);
watch(() => [props.rotulos, props.valores], desenhar);
onBeforeUnmount(() => grafico?.destroy());
</script>

<template>
  <div class="h-64">
    <canvas ref="canvas"></canvas>
  </div>
</template>
