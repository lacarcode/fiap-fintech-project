import { useCallback, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { excluirGasto, listarGastos } from '../api/gastos';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import TabelaGastos from '../components/gastos/TabelaGastos';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';
import { formatarMoeda } from '../utils/formatar';

export default function GastosPage() {
  const { usuario } = useAuth();
  const location = useLocation();
  const buscar = useCallback(() => listarGastos(usuario.id), [usuario.id]);
  const { dados: gastos, erro, carregando, setDados } = useDados(buscar);
  const [paraExcluir, setParaExcluir] = useState(null);
  const [excluindo, setExcluindo] = useState(false);
  const [erroExclusao, setErroExclusao] = useState(null);
  const [mensagem, setMensagem] = useState(location.state?.mensagem ?? null);

  async function confirmarExclusao() {
    setExcluindo(true);
    setErroExclusao(null);
    setMensagem(null);
    try {
      await excluirGasto(paraExcluir.id);
      setDados(gastos.filter((g) => g.id !== paraExcluir.id));
      setMensagem('Gasto excluído com sucesso.');
    } catch (e) {
      setErroExclusao(e.message);
    } finally {
      setExcluindo(false);
      setParaExcluir(null);
    }
  }

  const total = (gastos ?? []).reduce((soma, g) => soma + Number(g.valor), 0);

  return (
    <div className="space-y-4">
      <PageHeader
        titulo="Gastos"
        descricao={gastos ? `${gastos.length} gasto(s) · total ${formatarMoeda(total)}` : 'Seus gastos'}
        acao={<Link to="/gastos/novo" className="rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-claro">Novo gasto</Link>}
      />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroExclusao && <Alerta>{erroExclusao}</Alerta>}
      {carregando && <Carregando />}
      {erro && <Alerta>{erro}</Alerta>}
      {gastos && <TabelaGastos gastos={gastos} onExcluir={setParaExcluir} />}
      <ConfirmarExclusao
        aberto={Boolean(paraExcluir)}
        mensagem={`Excluir o gasto "${paraExcluir?.descricao}"? Esta ação não pode ser desfeita.`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setParaExcluir(null)}
        processando={excluindo}
      />
    </div>
  );
}
