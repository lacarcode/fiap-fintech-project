import { useCallback, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { excluirReceita, listarReceitas } from '../api/receitas';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import PageHeader from '../components/PageHeader';
import TabelaReceitas from '../components/receitas/TabelaReceitas';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';
import { formatarMoeda } from '../utils/formatar';

export default function ReceitasPage() {
  const { usuario } = useAuth();
  const location = useLocation();
  const buscar = useCallback(() => listarReceitas(usuario.id), [usuario.id]);
  const { dados: receitas, erro, carregando, setDados } = useDados(buscar);
  const [paraExcluir, setParaExcluir] = useState(null);
  const [excluindo, setExcluindo] = useState(false);
  const [erroExclusao, setErroExclusao] = useState(null);
  const [mensagem, setMensagem] = useState(location.state?.mensagem ?? null);

  async function confirmarExclusao() {
    setExcluindo(true);
    setErroExclusao(null);
    setMensagem(null);
    try {
      await excluirReceita(paraExcluir.id);
      setDados(receitas.filter((r) => r.id !== paraExcluir.id));
      setMensagem('Receita excluída com sucesso.');
    } catch (e) {
      setErroExclusao(e.message);
    } finally {
      setExcluindo(false);
      setParaExcluir(null);
    }
  }

  const total = (receitas ?? []).reduce((soma, r) => soma + Number(r.valor), 0);

  return (
    <div className="space-y-4">
      <PageHeader
        titulo="Receitas"
        descricao={receitas ? `${receitas.length} receita(s) · total ${formatarMoeda(total)}` : 'Suas receitas'}
        acao={<Link to="/receitas/novo" className="rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-claro">Nova receita</Link>}
      />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroExclusao && <Alerta>{erroExclusao}</Alerta>}
      {carregando && <Carregando />}
      {erro && <Alerta>{erro}</Alerta>}
      {receitas && <TabelaReceitas receitas={receitas} onExcluir={setParaExcluir} />}
      <ConfirmarExclusao
        aberto={Boolean(paraExcluir)}
        mensagem={`Excluir a receita "${paraExcluir?.descricao}"? Esta ação não pode ser desfeita.`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setParaExcluir(null)}
        processando={excluindo}
      />
    </div>
  );
}
