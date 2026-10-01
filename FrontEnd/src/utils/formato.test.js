import { describe, expect, it } from 'vitest';
import { data, moeda, moedaCompacta, porcentagem, tempoDecorrido } from './formato';

describe('formato', () => {
  it('mostra datas sem hora no dia certo, sem voltar um dia por causa do fuso', () => {
    expect(data('2025-05-01')).toBe('01/05/2025');
    expect(data(null)).toBe('—');
  });

  it('formata moeda e porcentagem', () => {
    expect(moeda(1500).replace(/\s/g, ' ')).toBe('R$ 1.500');
    expect(moeda('x')).toBe('—');
    expect(moedaCompacta(2_500_000)).toBe('R$ 2,5 M');
    expect(moedaCompacta(12_000)).toBe('R$ 12 mil');
    expect(porcentagem(12.345)).toBe('12,3%');
  });

  it('descreve há quanto tempo algo aconteceu', () => {
    const dias = (n) => new Date(Date.now() - n * 86400000).toISOString();
    expect(tempoDecorrido(dias(0))).toBe('hoje');
    expect(tempoDecorrido(dias(1))).toBe('ontem');
    expect(tempoDecorrido(dias(10))).toBe('há 10 dias');
    expect(tempoDecorrido(dias(95))).toBe('há 3 meses');
    expect(tempoDecorrido(dias(800))).toBe('há 2 anos');
  });
});
