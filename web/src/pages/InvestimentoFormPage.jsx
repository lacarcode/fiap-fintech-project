import { useCallback, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { listarContas } from '../api/contas';
import { atualizarInvestimento, buscarInvestimento, criarInvestimento } from '../api/investimentos';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import InvestimentoForm from '../components/investimentos/InvestimentoForm';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';

/** Resposta da API -> valores do formulário (taxa em fração vira % a.a.). */
function paraFormulario(i) {
  return {
    ...i,
    instituicao: i.instituicao ?? '',
    valorAplicado: String(i.valorAplicado),
    taxa: i.taxaRentabilidade == null ? '' : String(Number((Number(i.taxaRentabilidade) * 100).toFixed(2))),
    dtVencimento: i.dtVencimento ?? '',
  };
}

export default function InvestimentoFormPage() {
  const { id } = useParams();
  const editando = Boolean(id);
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const buscar = useCallback(
    () => Promise.all([listarContas(usuario.id), id ? buscarInvestimento(id) : null]),
    [usuario.id, id],
  );
  const { dados, erro, carregando } = useDados(buscar);
  const [erroEnvio, setErroEnvio] = useState(null);
  const [enviando, setEnviando] = useState(false);

  async function salvar(valores) {
    setEnviando(true);
    setErroEnvio(null);
    try {
      const corpo = { ...valores, usuarioId: usuario.id };
      if (editando) await atualizarInvestimento(id, corpo);
      else await criarInvestimento(corpo);
      navigate('/investimentos', { state: { mensagem: editando ? 'Investimento atualizado com sucesso.' : 'Investimento cadastrado com sucesso.' } });
    } catch (e) {
      setErroEnvio(e);
      setEnviando(false);
    }
  }

  if (carregando) return <Carregando />;
  if (erro) return <Alerta>{erro}</Alerta>;
  const [contas, investimento] = dados;
  const contasAtivas = contas.filter((c) => c.ativa);

  return (
    <div className="space-y-4">
      <PageHeader titulo={editando ? 'Editar investimento' : 'Novo investimento'} />
      {contasAtivas.length === 0 && (
        <Alerta>Cadastre uma conta bancária ativa em <Link to="/contas" className="underline">Contas</Link> antes de registrar investimentos.</Alerta>
      )}
      {erroEnvio && <Alerta detalhes={erroEnvio.detalhes}>{erroEnvio.message}</Alerta>}
      <InvestimentoForm
        inicial={investimento ? paraFormulario(investimento) : undefined}
        contas={contasAtivas}
        editando={editando}
        onSubmit={salvar}
        enviando={enviando}
      />
    </div>
  );
}
