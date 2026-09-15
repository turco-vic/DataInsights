// Formatação pt-BR compartilhada pelas telas.

export function moeda(valor) {
  if (typeof valor !== 'number') return '—';
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 });
}

// R$ 78,9 M · R$ 780 mil — para cards e rótulos curtos.
export function moedaCompacta(valor) {
  if (typeof valor !== 'number') return '—';
  if (valor >= 1e6) return `R$ ${(valor / 1e6).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} M`;
  if (valor >= 1e3) return `R$ ${Math.round(valor / 1e3).toLocaleString('pt-BR')} mil`;
  return moeda(valor);
}

export function data(valor) {
  return valor ? new Date(valor).toLocaleDateString('pt-BR') : '—';
}

export function dataHora(valor) {
  if (!valor) return '—';
  const d = new Date(valor);
  return `${d.toLocaleDateString('pt-BR')} · ${d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}`;
}

export function porcentagem(valor) {
  return `${valor.toFixed(1).replace('.', ',')}%`;
}
