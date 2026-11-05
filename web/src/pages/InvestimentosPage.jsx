import { useCallback, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { excluirInvestimento, listarInvestimentos } from '../api/investimentos';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import TabelaInvestimentos from '../components/investimentos/TabelaInvestimentos';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';

export default function InvestimentosPage() {
  const { usuario } = useAuth();
  const location = useLocation();
  const buscar = useCallback(() => listarInvestimentos(usuario.id), [usuario.id]);
  const { dados: investimentos, erro, carregando, setDados } = useDados(buscar);
  const [paraExcluir, setParaExcluir] = useState(null);
  const [excluindo, setExcluindo] = useState(false);
  const [erroExclusao, setErroExclusao] = useState(null);
  const [mensagem, setMensagem] = useState(location.state?.mensagem ?? null);

  async function confirmarExclusao() {
    setExcluindo(true);
    setErroExclusao(null);
    setMensagem(null);
    try {
      await excluirInvestimento(paraExcluir.id);
      setDados(investimentos.filter((i) => i.id !== paraExcluir.id));
      setMensagem('Investimento excluído com sucesso.');
    } catch (e) {
      setErroExclusao(e.message);
    } finally {
      setExcluindo(false);
      setParaExcluir(null);
    }
  }

  return (
    <div className="space-y-4">
      <PageHeader
        titulo="Investimentos"
        descricao="Consulte, cadastre, edite e exclua suas aplicações"
        acao={<Link to="/investimentos/novo" className="rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-claro">Novo investimento</Link>}
      />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroExclusao && <Alerta>{erroExclusao}</Alerta>}
      {carregando && <Carregando />}
      {erro && <Alerta>{erro}</Alerta>}
      {investimentos && <TabelaInvestimentos investimentos={investimentos} onExcluir={setParaExcluir} />}
      <ConfirmarExclusao
        aberto={Boolean(paraExcluir)}
        mensagem={`Excluir o investimento "${paraExcluir?.nomeInvestimento}"? Esta ação não pode ser desfeita.`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setParaExcluir(null)}
        processando={excluindo}
      />
    </div>
  );
}
