
export function moeda(valor) {
  if (typeof valor !== 'number') return '—';
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 });
}

export function moedaCompacta(valor) {
  if (typeof valor !== 'number') return '—';
  if (valor >= 1e6) return `R$ ${(valor / 1e6).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} M`;
  if (valor >= 1e3) return `R$ ${Math.round(valor / 1e3).toLocaleString('pt-BR')} mil`;
  return moeda(valor);
}

export function data(valor) {
  if (!valor) return '—';
  // "2025-05-01" (sem hora) é lido como data local; new Date() o trataria como UTC e mostraria o dia anterior no Brasil.
  const soData = typeof valor === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(valor);
  const d = soData ? new Date(...valor.split('-').map((p, i) => Number(p) - (i === 1 ? 1 : 0))) : new Date(valor);
  return d.toLocaleDateString('pt-BR');
}

/** "há 3 dias", "há 2 meses"... para datas passadas. */
export function tempoDecorrido(valor) {
  if (!valor) return '';
  const dias = Math.floor((Date.now() - new Date(valor).getTime()) / 86400000);
  if (dias < 1) return 'hoje';
  if (dias === 1) return 'ontem';
  if (dias < 30) return `há ${dias} dias`;
  const meses = Math.floor(dias / 30);
  if (meses < 12) return meses === 1 ? 'há 1 mês' : `há ${meses} meses`;
  const anos = Math.floor(meses / 12);
  return anos === 1 ? 'há 1 ano' : `há ${anos} anos`;
}

export function dataHora(valor) {
  if (!valor) return '—';
  const d = new Date(valor);
  return `${d.toLocaleDateString('pt-BR')} · ${d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}`;
}

export function porcentagem(valor) {
  return `${valor.toFixed(1).replace('.', ',')}%`;
}
