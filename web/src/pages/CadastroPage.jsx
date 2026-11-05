import { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { criarUsuario } from '../api/usuarios';
import Alerta from '../components/Alerta';
import Botao from '../components/Botao';
import Campo from '../components/Campo';
import { useAuth } from '../contexts/AuthContext';

export default function CadastroPage() {
  const { usuario, entrar } = useAuth();
  const [form, setForm] = useState({ nome: '', email: '', senha: '', confirmacao: '' });
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  if (usuario) return <Navigate to="/" replace />;

  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function enviar(e) {
    e.preventDefault();
    if (form.senha !== form.confirmacao) {
      setErro({ message: 'As senhas não conferem.', detalhes: [] });
      return;
    }
    setEnviando(true);
    setErro(null);
    try {
      await criarUsuario({ nome: form.nome, email: form.email, senha: form.senha });
      await entrar(form.email, form.senha);
    } catch (err) {
      setErro(err);
      setEnviando(false);
    }
  }

  return (
    <main className="grid min-h-screen place-items-center px-4">
      <form onSubmit={enviar} className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-brand">Criar conta</h1>
          <p className="text-sm text-slate-500">Cadastre-se para começar.</p>
        </div>
        {erro && <Alerta detalhes={erro.detalhes}>{erro.message}</Alerta>}
        <Campo label="Nome" name="nome" value={form.nome} onChange={alterar} required maxLength={150} autoComplete="name" />
        <Campo label="E-mail" name="email" type="email" value={form.email} onChange={alterar} required autoComplete="email" />
        <Campo label="Senha" name="senha" type="password" value={form.senha} onChange={alterar} required minLength={6} autoComplete="new-password" />
        <Campo label="Confirmar senha" name="confirmacao" type="password" value={form.confirmacao} onChange={alterar} required minLength={6} autoComplete="new-password" />
        <Botao type="submit" disabled={enviando} className="w-full">{enviando ? 'Cadastrando…' : 'Cadastrar'}</Botao>
        <p className="text-center text-sm text-slate-600">
          Já tem conta? <Link to="/login" className="font-medium text-brand hover:underline">Entrar</Link>
        </p>
      </form>
    </main>
  );
}
