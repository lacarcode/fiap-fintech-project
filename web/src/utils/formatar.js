const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export const formatarMoeda = (valor) => moeda.format(Number(valor ?? 0));

export const formatarData = (iso) => (iso ? new Date(`${iso}T00:00:00`).toLocaleDateString('pt-BR') : '—');

/** 0.125 -> "12,5%" */
export const formatarPercentual = (fracao) =>
  fracao == null ? '—' : `${(Number(fracao) * 100).toLocaleString('pt-BR', { maximumFractionDigits: 2 })}%`;

/** Data local de hoje em YYYY-MM-DD (formato do <input type="date"> e da API). */
export const hojeISO = () => new Date().toLocaleDateString('en-CA');
