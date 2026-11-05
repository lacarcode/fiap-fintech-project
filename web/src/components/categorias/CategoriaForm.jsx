import { useState } from 'react';
import Botao from '../Botao';
import Campo from '../Campo';

const VAZIO = { nome: '', tipo: 'GASTO' };

export default function CategoriaForm({ inicial, onSubmit, onCancelar, enviando = false }) {
  const editando = Boolean(inicial);
  const [form, setForm] = useState(() => ({ ...VAZIO, ...inicial }));
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({ nome: form.nome.trim(), tipo: form.tipo });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-3 sm:items-end">
      <Campo label="Nome" name="nome" value={form.nome} onChange={alterar} required maxLength={80} />
      <Campo as="select" label="Tipo" name="tipo" value={form.tipo} onChange={alterar}>
        <option value="GASTO">Gasto</option>
        <option value="RECEITA">Receita</option>
      </Campo>
      <div className="flex justify-end gap-2">
        {editando && <Botao variante="secundario" onClick={onCancelar}>Cancelar</Botao>}
        <Botao type="submit" disabled={enviando}>
          {enviando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Adicionar categoria'}
        </Botao>
      </div>
    </form>
  );
}
