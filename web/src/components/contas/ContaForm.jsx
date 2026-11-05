import { useState } from 'react';
import Botao from '../Botao';
import Campo from '../Campo';

export const TIPOS_CONTA = [['CORRENTE', 'Corrente'], ['POUPANCA', 'Poupança'], ['PAGAMENTO', 'Pagamento'], ['SALARIO', 'Salário'], ['INVESTIMENTO', 'Investimento']];
const VAZIO = { nmBanco: '', tipoConta: 'CORRENTE', nrConta: '', ativa: true };

/** Sem `inicial` cria; com `inicial` edita (e mostra "Conta ativa" e "Cancelar"). */
export default function ContaForm({ inicial, onSubmit, onCancelar, enviando = false }) {
  const editando = Boolean(inicial);
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial }));
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({ nmBanco: form.nmBanco.trim(), tipoConta: form.tipoConta, nrConta: form.nrConta.trim(), ativa: form.ativa });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-3">
      <Campo label="Banco" name="nmBanco" value={form.nmBanco} onChange={alterar} required maxLength={100} />
      <Campo as="select" label="Tipo de conta" name="tipoConta" value={form.tipoConta} onChange={alterar}>
        {TIPOS_CONTA.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
      </Campo>
      <Campo label="Número da conta" name="nrConta" value={form.nrConta} onChange={alterar} required maxLength={20} />
      {editando && (
        <label htmlFor="ativa" className="flex items-center gap-2 text-sm text-slate-700">
          <input id="ativa" name="ativa" type="checkbox" checked={form.ativa} onChange={alterar} className="h-4 w-4 accent-brand" />
          Conta ativa
        </label>
      )}
      <div className="flex justify-end gap-2 sm:col-span-3">
        {editando && <Botao variante="secundario" onClick={onCancelar}>Cancelar</Botao>}
        <Botao type="submit" disabled={enviando}>
          {enviando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Adicionar conta'}
        </Botao>
      </div>
    </form>
  );
}
