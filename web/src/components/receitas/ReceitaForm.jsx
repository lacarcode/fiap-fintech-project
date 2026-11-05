import { useState } from 'react';
import { Link } from 'react-router-dom';
import { hojeISO } from '../../utils/formatar';
import Botao from '../Botao';
import Campo from '../Campo';

export const ORIGENS = [['SALARIO', 'Salário'], ['FREELANCE', 'Freelance'], ['INVESTIMENTO', 'Investimento'], ['OUTROS', 'Outros']];
export const RECORRENCIAS = [['UNICA', 'Única'], ['MENSAL', 'Mensal'], ['ANUAL', 'Anual']];

const VAZIO = {
  origem: '', descricao: '', valor: '', dtRecebimento: hojeISO(), recorrencia: 'UNICA',
  contaId: '', categoriaId: '', comprovante: '',
};

export default function ReceitaForm({ inicial, categorias, contas, onSubmit, enviando = false }) {
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial }));
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({
      descricao: form.descricao.trim(),
      valor: Number(form.valor),
      dtRecebimento: form.dtRecebimento,
      origem: form.origem || null,
      recorrencia: form.recorrencia,
      categoriaId: Number(form.categoriaId),
      contaId: Number(form.contaId),
      comprovante: (form.comprovante ?? '').trim() || null,
    });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-2">
      <Campo as="select" label="Origem" name="origem" value={form.origem} onChange={alterar}>
        <option value="">Não informada</option>
        {ORIGENS.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
      </Campo>
      <Campo label="Descrição" name="descricao" value={form.descricao} onChange={alterar} required maxLength={300} />
      <Campo label="Valor (R$)" name="valor" type="number" min="0.01" step="0.01" value={form.valor} onChange={alterar} required />
      <Campo label="Data de recebimento" name="dtRecebimento" type="date" value={form.dtRecebimento} onChange={alterar} required />
      <Campo as="select" label="Recorrência" name="recorrencia" value={form.recorrencia} onChange={alterar}>
        {RECORRENCIAS.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
      </Campo>
      <Campo as="select" label="Conta destino" name="contaId" value={form.contaId} onChange={alterar} required>
        <option value="">Selecione…</option>
        {contas.map((c) => <option key={c.id} value={c.id}>{c.nmBanco} · {c.nrConta}</option>)}
      </Campo>
      <Campo as="select" label="Categoria" name="categoriaId" value={form.categoriaId} onChange={alterar} required>
        <option value="">Selecione…</option>
        {categorias.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
      </Campo>
      <Campo label="Comprovante (URL, opcional)" name="comprovante" value={form.comprovante ?? ''} onChange={alterar} maxLength={500} />
      <div className="flex justify-end gap-2 sm:col-span-2">
        <Link to="/receitas" className="rounded-lg px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100">Cancelar</Link>
        <Botao type="submit" disabled={enviando}>{enviando ? 'Salvando…' : 'Salvar'}</Botao>
      </div>
    </form>
  );
}
