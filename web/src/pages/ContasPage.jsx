import { useCallback, useState } from 'react';
import { atualizarConta, criarConta, excluirConta, listarContas } from '../api/contas';
import Alerta from '../components/Alerta';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import ContaForm, { TIPOS_CONTA } from '../components/contas/ContaForm';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';

const rotuloTipo = (valor) => TIPOS_CONTA.find(([v]) => v === valor)?.[1] ?? valor;

export default function ContasPage() {
  const { usuario } = useAuth();
  const buscar = useCallback(() => listarContas(usuario.id), [usuario.id]);
  const { dados: contas, erro, carregando, setDados } = useDados(buscar);
  const [emEdicao, setEmEdicao] = useState(null);
  const [paraExcluir, setParaExcluir] = useState(null);
  const [processando, setProcessando] = useState(false);
  const [mensagem, setMensagem] = useState(null);
  const [erroAcao, setErroAcao] = useState(null);

  async function salvar(valores) {
    setProcessando(true);
    setErroAcao(null);
    setMensagem(null);
    try {
      const corpo = { ...valores, usuarioId: usuario.id };
      if (emEdicao) {
        const atualizada = await atualizarConta(emEdicao.id, corpo);
        setDados(contas.map((c) => (c.id === atualizada.id ? atualizada : c)));
        setMensagem('Conta atualizada com sucesso.');
      } else {
        setDados([...contas, await criarConta(corpo)]);
        setMensagem('Conta cadastrada com sucesso.');
      }
      setEmEdicao(null);
    } catch (e) {
      setErroAcao(e);
    } finally {
      setProcessando(false);
    }
  }

  async function confirmarExclusao() {
    setProcessando(true);
    setErroAcao(null);
    try {
      await excluirConta(paraExcluir.id);
      setDados(contas.filter((c) => c.id !== paraExcluir.id));
      setMensagem('Conta excluída.');
    } catch (e) {
      setErroAcao(e); // 409: conta com lançamentos -> a mensagem da API sugere desativar
    } finally {
      setProcessando(false);
      setParaExcluir(null);
    }
  }

  return (
    <div className="space-y-4">
      <PageHeader titulo="Contas bancárias" descricao="Contas usadas nas receitas, gastos e investimentos" />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroAcao && <Alerta detalhes={erroAcao.detalhes}>{erroAcao.message}</Alerta>}
      <ContaForm key={emEdicao?.id ?? 'nova'} inicial={emEdicao ?? undefined} onSubmit={salvar} onCancelar={() => setEmEdicao(null)} enviando={processando} />
      {carregando && <Carregando />}
      {erro && <Alerta>{erro}</Alerta>}
      {contas && (contas.length === 0 ? (
        <p className="rounded-2xl bg-white p-6 text-sm text-slate-500 ring-1 ring-slate-200">Nenhuma conta cadastrada.</p>
      ) : (
        <div className="overflow-x-auto rounded-2xl bg-white ring-1 ring-slate-200">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-slate-500">
              <tr>{['Banco', 'Número', 'Tipo', 'Situação', 'Ações'].map((c) => <th key={c} className="px-4 py-3 font-medium">{c}</th>)}</tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {contas.map((c) => (
                <tr key={c.id}>
                  <td className="px-4 py-3">{c.nmBanco}</td>
                  <td className="px-4 py-3">{c.nrConta}</td>
                  <td className="px-4 py-3">{rotuloTipo(c.tipoConta)}</td>
                  <td className="px-4 py-3">{c.ativa ? 'Ativa' : 'Inativa'}</td>
                  <td className="whitespace-nowrap px-4 py-3">
                    <button type="button" onClick={() => setEmEdicao(c)} className="text-brand hover:underline">Editar</button>
                    <button type="button" onClick={() => setParaExcluir(c)} className="ml-3 text-red-600 hover:underline">Excluir</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}
      <ConfirmarExclusao
        aberto={Boolean(paraExcluir)}
        mensagem={`Excluir a conta ${paraExcluir?.nmBanco} · ${paraExcluir?.nrConta}?`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setParaExcluir(null)}
        processando={processando}
      />
    </div>
  );
}
