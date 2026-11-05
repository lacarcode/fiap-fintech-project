import { useCallback, useState } from 'react';
import { atualizarUsuario, buscarUsuario, excluirUsuario } from '../api/usuarios';
import Alerta from '../components/Alerta';
import Botao from '../components/Botao';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import PageHeader from '../components/PageHeader';
import PerfilForm from '../components/perfil/PerfilForm';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';
import { formatarData } from '../utils/formatar';

export default function PerfilPage() {
  const { usuario, atualizarSessao, sair } = useAuth();
  const buscar = useCallback(() => buscarUsuario(usuario.id), [usuario.id]);
  const { dados: perfil, erro, carregando, setDados } = useDados(buscar);
  const [mensagem, setMensagem] = useState(null);
  const [erroAcao, setErroAcao] = useState(null);
  const [salvando, setSalvando] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [excluindo, setExcluindo] = useState(false);

  async function salvar(valores) {
    setSalvando(true);
    setMensagem(null);
    setErroAcao(null);
    try {
      const atualizado = await atualizarUsuario(usuario.id, valores);
      setDados(atualizado);
      atualizarSessao(atualizado);
      setMensagem('Perfil atualizado com sucesso.');
    } catch (e) {
      setErroAcao(e);
    } finally {
      setSalvando(false);
    }
  }

  async function excluirConta() {
    setExcluindo(true);
    try {
      await excluirUsuario(usuario.id);
      sair(); // RotaProtegida leva para /login
    } catch (e) {
      setErroAcao(e);
      setExcluindo(false);
      setConfirmando(false);
    }
  }

  if (carregando) return <Carregando />;
  if (erro) return <Alerta>{erro}</Alerta>;

  return (
    <div className="space-y-6">
      <PageHeader titulo="Meu perfil" descricao={`Cliente desde ${formatarData(perfil.dtCadastro)}`} />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroAcao && <Alerta detalhes={erroAcao.detalhes}>{erroAcao.message}</Alerta>}
      <PerfilForm key={perfil.email} inicial={perfil} onSubmit={salvar} enviando={salvando} />
      <section className="rounded-2xl bg-white p-6 ring-1 ring-red-200">
        <h2 className="font-semibold text-red-700">Excluir conta</h2>
        <p className="mt-1 text-sm text-slate-600">Remove seu cadastro, contas bancárias, receitas, gastos e investimentos.</p>
        <Botao variante="perigo" className="mt-4" onClick={() => setConfirmando(true)}>Excluir minha conta</Botao>
      </section>
      <ConfirmarExclusao
        aberto={confirmando}
        titulo="Excluir sua conta?"
        mensagem="Todos os seus dados serão apagados permanentemente."
        onConfirmar={excluirConta}
        onCancelar={() => setConfirmando(false)}
        processando={excluindo}
      />
    </div>
  );
}
