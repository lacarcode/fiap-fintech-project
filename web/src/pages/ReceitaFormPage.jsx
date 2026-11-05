import { useCallback, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { listarCategorias } from '../api/categorias';
import { listarContas } from '../api/contas';
import { atualizarReceita, buscarReceita, criarReceita } from '../api/receitas';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import PageHeader from '../components/PageHeader';
import ReceitaForm from '../components/receitas/ReceitaForm';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';

export default function ReceitaFormPage() {
  const { id } = useParams();
  const editando = Boolean(id);
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const buscar = useCallback(
    () => Promise.all([listarCategorias('RECEITA'), listarContas(usuario.id), id ? buscarReceita(id) : null]),
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
      if (editando) await atualizarReceita(id, corpo);
      else await criarReceita(corpo);
      navigate('/receitas', { state: { mensagem: editando ? 'Receita atualizada com sucesso.' : 'Receita cadastrada com sucesso.' } });
    } catch (e) {
      setErroEnvio(e);
      setEnviando(false);
    }
  }

  if (carregando) return <Carregando />;
  if (erro) return <Alerta>{erro}</Alerta>;
  const [categorias, contas, receita] = dados;
  const contasAtivas = contas.filter((c) => c.ativa);

  return (
    <div className="space-y-4">
      <PageHeader titulo={editando ? 'Editar receita' : 'Cadastrar nova receita'} />
      {contasAtivas.length === 0 && (
        <Alerta>Cadastre uma conta bancária ativa em <Link to="/contas" className="underline">Contas</Link> antes de registrar receitas.</Alerta>
      )}
      {erroEnvio && <Alerta detalhes={erroEnvio.detalhes}>{erroEnvio.message}</Alerta>}
      <ReceitaForm
        inicial={receita ? { ...receita, valor: String(receita.valor), origem: receita.origem ?? '', comprovante: receita.comprovante ?? '' } : undefined}
        categorias={categorias}
        contas={contasAtivas}
        onSubmit={salvar}
        enviando={enviando}
      />
    </div>
  );
}
