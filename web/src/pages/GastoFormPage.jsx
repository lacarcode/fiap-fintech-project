import { useCallback, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { listarCategorias } from '../api/categorias';
import { listarContas } from '../api/contas';
import { atualizarGasto, buscarGasto, criarGasto } from '../api/gastos';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import GastoForm from '../components/gastos/GastoForm';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';

export default function GastoFormPage() {
  const { id } = useParams();
  const editando = Boolean(id);
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const buscar = useCallback(
    () => Promise.all([listarCategorias('GASTO'), listarContas(usuario.id), id ? buscarGasto(id) : null]),
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
      if (editando) await atualizarGasto(id, corpo);
      else await criarGasto(corpo);
      navigate('/gastos', { state: { mensagem: editando ? 'Gasto atualizado com sucesso.' : 'Gasto cadastrado com sucesso.' } });
    } catch (e) {
      setErroEnvio(e);
      setEnviando(false);
    }
  }

  if (carregando) return <Carregando />;
  if (erro) return <Alerta>{erro}</Alerta>;
  const [categorias, contas, gasto] = dados;
  const contasAtivas = contas.filter((c) => c.ativa);

  return (
    <div className="space-y-4">
      <PageHeader titulo={editando ? 'Editar gasto' : 'Novo gasto'} />
      {contasAtivas.length === 0 && (
        <Alerta>Cadastre uma conta bancária ativa em <Link to="/contas" className="underline">Contas</Link> antes de registrar gastos.</Alerta>
      )}
      {erroEnvio && <Alerta detalhes={erroEnvio.detalhes}>{erroEnvio.message}</Alerta>}
      <GastoForm
        inicial={gasto ? { ...gasto, valor: String(gasto.valor), comprovante: gasto.comprovante ?? '' } : undefined}
        categorias={categorias}
        contas={contasAtivas}
        onSubmit={salvar}
        enviando={enviando}
      />
    </div>
  );
}
