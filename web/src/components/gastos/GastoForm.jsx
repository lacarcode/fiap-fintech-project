import { useState } from 'react';
import { Link } from 'react-router-dom';
import { hojeISO } from '../../utils/formatar';
import Botao from '../Botao';
import Campo from '../Campo';

const VAZIO = { descricao: '', valor: '', dtGasto: hojeISO(), tipo: 'VARIAVEL', categoriaId: '', contaId: '', comprovante: '' };

export default function GastoForm({ inicial, categorias, contas, onSubmit, enviando = false }) {
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial }));
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({
      descricao: form.descricao.trim(),
      valor: Number(form.valor),
      dtGasto: form.dtGasto,
      tipo: form.tipo,
      categoriaId: Number(form.categoriaId),
      contaId: Number(form.contaId),
      comprovante: form.comprovante.trim() || null,
    });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-2">
      <Campo className="sm:col-span-2" label="Descrição" name="descricao" value={form.descricao} onChange={alterar} required maxLength={300} />
      <Campo label="Valor (R$)" name="valor" type="number" min="0.01" step="0.01" value={form.valor} onChange={alterar} required />
      <Campo label="Data" name="dtGasto" type="date" value={form.dtGasto} onChange={alterar} required />
      <Campo as="select" label="Categoria" name="categoriaId" value={form.categoriaId} onChange={alterar} required>
        <option value="">Selecione…</option>
        {categorias.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
      </Campo>
      <Campo as="select" label="Conta" name="contaId" value={form.contaId} onChange={alterar} required>
        <option value="">Selecione…</option>
        {contas.map((c) => <option key={c.id} value={c.id}>{c.nmBanco} · {c.nrConta}</option>)}
      </Campo>
      <Campo as="select" label="Tipo" name="tipo" value={form.tipo} onChange={alterar}>
        <option value="FIXO">Fixo</option>
        <option value="VARIAVEL">Variável</option>
      </Campo>
      <Campo label="Comprovante (URL, opcional)" name="comprovante" value={form.comprovante} onChange={alterar} maxLength={500} />
      <div className="flex justify-end gap-2 sm:col-span-2">
        <Link to="/gastos" className="rounded-lg px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100">Cancelar</Link>
        <Botao type="submit" disabled={enviando}>{enviando ? 'Salvando…' : 'Salvar'}</Botao>
      </div>
    </form>
  );
}
