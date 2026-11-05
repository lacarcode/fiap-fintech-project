import { useState } from 'react';
import { Link } from 'react-router-dom';
import { hojeISO } from '../../utils/formatar';
import Botao from '../Botao';
import Campo from '../Campo';

export const TIPOS_INVESTIMENTO = [
  ['CDB', 'CDB'], ['LCI', 'LCI'], ['LCA', 'LCA'], ['TESOURO', 'Tesouro Direto'], ['ACOES', 'Ações'],
  ['FII', 'Fundo imobiliário'], ['FUNDO', 'Fundo de investimento'], ['POUPANCA', 'Poupança'], ['CRIPTO', 'Cripto'], ['OUTROS', 'Outros'],
];

const VAZIO = {
  nomeInvestimento: '', tipo: 'CDB', instituicao: '', valorAplicado: '', taxa: '',
  dtAplicacao: hojeISO(), dtVencimento: '', contaId: '', status: 'ATIVO',
};

export default function InvestimentoForm({ inicial, contas, editando = false, onSubmit, enviando = false }) {
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial }));
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({
      nomeInvestimento: form.nomeInvestimento.trim(),
      tipo: form.tipo,
      instituicao: form.instituicao.trim() || null,
      valorAplicado: Number(form.valorAplicado),
      // % a.a. -> fração com 4 casas (evita 0.10750000000000001)
      taxaRentabilidade: form.taxa === '' ? null : Number((Number(form.taxa) / 100).toFixed(4)),
      dtAplicacao: form.dtAplicacao,
      dtVencimento: form.dtVencimento || null,
      contaId: Number(form.contaId),
      status: editando ? form.status : null,
    });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-2">
      <Campo label="Nome" name="nomeInvestimento" value={form.nomeInvestimento} onChange={alterar} required maxLength={150} />
      <Campo as="select" label="Tipo" name="tipo" value={form.tipo} onChange={alterar}>
        {TIPOS_INVESTIMENTO.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
      </Campo>
      <Campo label="Instituição (opcional)" name="instituicao" value={form.instituicao} onChange={alterar} maxLength={100} />
      <Campo as="select" label="Conta" name="contaId" value={form.contaId} onChange={alterar} required>
        <option value="">Selecione…</option>
        {contas.map((c) => <option key={c.id} value={c.id}>{c.nmBanco} · {c.nrConta}</option>)}
      </Campo>
      <Campo label="Valor aplicado (R$)" name="valorAplicado" type="number" min="0.01" step="0.01" value={form.valorAplicado} onChange={alterar} required />
      <Campo label="Rentabilidade (% a.a.)" name="taxa" type="number" min="0" step="0.01" value={form.taxa} onChange={alterar} />
      <Campo label="Data de aplicação" name="dtAplicacao" type="date" value={form.dtAplicacao} onChange={alterar} required />
      <Campo label="Vencimento (opcional)" name="dtVencimento" type="date" min={form.dtAplicacao} value={form.dtVencimento} onChange={alterar} />
      {editando && (
        <Campo as="select" label="Status" name="status" value={form.status} onChange={alterar}>
          <option value="ATIVO">Ativo</option>
          <option value="RESGATADO">Resgatado</option>
          <option value="VENCIDO">Vencido</option>
        </Campo>
      )}
      <div className="flex justify-end gap-2 sm:col-span-2">
        <Link to="/investimentos" className="rounded-lg px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100">Cancelar</Link>
        <Botao type="submit" disabled={enviando}>{enviando ? 'Salvando…' : 'Salvar'}</Botao>
      </div>
    </form>
  );
}
