import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { login } from '../api/auth';

const AuthContext = createContext(null);
const CHAVE = 'fintech.usuario';

function lerUsuarioSalvo() {
  try {
    return JSON.parse(localStorage.getItem(CHAVE));
  } catch {
    return null;
  }
}

function salvar(usuario) {
  try {
    if (usuario) localStorage.setItem(CHAVE, JSON.stringify(usuario));
    else localStorage.removeItem(CHAVE);
  } catch {
    // localStorage indisponível (aba anônima): a sessão vale só até recarregar a página
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(lerUsuarioSalvo);

  const atualizarSessao = useCallback((novo) => {
    setUsuario(novo);
    salvar(novo);
  }, []);

  const entrar = useCallback(async (email, senha) => {
    const autenticado = await login(email, senha);
    atualizarSessao(autenticado);
    return autenticado;
  }, [atualizarSessao]);

  const sair = useCallback(() => atualizarSessao(null), [atualizarSessao]);

  const valor = useMemo(() => ({ usuario, entrar, sair, atualizarSessao }), [usuario, entrar, sair, atualizarSessao]);
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const contexto = useContext(AuthContext);
  if (!contexto) throw new Error('useAuth precisa estar dentro de <AuthProvider>');
  return contexto;
}
