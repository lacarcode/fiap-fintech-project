import { useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import Alerta from '../components/Alerta';
import Botao from '../components/Botao';
import Campo from '../components/Campo';
import { useAuth } from '../contexts/AuthContext';

export default function LoginPage() {
  const { usuario, entrar } = useAuth();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', senha: '' });
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  // Ao autenticar, o estado muda e esta linha faz o redirecionamento
  if (usuario) return <Navigate to={location.state?.de ?? '/'} replace />;

  const alterar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  async function enviar(e) {
    e.preventDefault();
    setEnviando(true);
    setErro(null);
    try {
      await entrar(form.email, form.senha);
    } catch (err) {
      setErro(err.message);
      setEnviando(false);
    }
  }

  return (
    <main className="grid min-h-screen place-items-center px-4">
      <form onSubmit={enviar} className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-brand">Fintech</h1>
          <p className="text-sm text-slate-500">Entre para gerenciar suas finanças.</p>
        </div>
        {erro && <Alerta>{erro}</Alerta>}
        <Campo label="E-mail" name="email" type="email" value={form.email} onChange={alterar} required autoComplete="email" />
        <Campo label="Senha" name="senha" type="password" value={form.senha} onChange={alterar} required autoComplete="current-password" />
        <Botao type="submit" disabled={enviando} className="w-full">{enviando ? 'Entrando…' : 'Entrar'}</Botao>
        <p className="text-center text-sm text-slate-600">
          Não tem conta? <Link to="/cadastro" className="font-medium text-brand hover:underline">Cadastre-se</Link>
        </p>
      </form>
    </main>
  );
}
