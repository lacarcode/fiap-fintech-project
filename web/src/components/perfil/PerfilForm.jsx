import { useState } from 'react';
import Botao from '../Botao';
import Campo from '../Campo';

export default function PerfilForm({ inicial, onSubmit, enviando = false }) {
  const [form, setForm] = useState({ nome: inicial.nome, email: inicial.email, avatar: inicial.avatar ?? '', senha: '' });
  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  function enviar(e) {
    e.preventDefault();
    onSubmit({
      nome: form.nome.trim(),
      email: form.email.trim(),
      avatar: form.avatar.trim() || null,
      senha: form.senha || null,
      ativo: true,
    });
  }

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-2xl bg-white p-6 ring-1 ring-slate-200 sm:grid-cols-2">
      <Campo label="Nome" name="nome" value={form.nome} onChange={alterar} required maxLength={150} />
      <Campo label="E-mail" name="email" type="email" value={form.email} onChange={alterar} required />
      <Campo label="Avatar (URL, opcional)" name="avatar" value={form.avatar} onChange={alterar} maxLength={500} />
      <Campo label="Nova senha (opcional)" name="senha" type="password" value={form.senha} onChange={alterar} minLength={6} autoComplete="new-password" />
      <div className="flex justify-end sm:col-span-2">
        <Botao type="submit" disabled={enviando}>{enviando ? 'Salvando…' : 'Salvar alterações'}</Botao>
      </div>
    </form>
  );
}
