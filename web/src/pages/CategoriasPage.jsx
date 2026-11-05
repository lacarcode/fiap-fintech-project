import { useState } from 'react';
import { atualizarCategoria, criarCategoria, excluirCategoria, listarCategorias } from '../api/categorias';
import Alerta from '../components/Alerta';
import CategoriaForm from '../components/categorias/CategoriaForm';
import Carregando from '../components/Carregando';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import PageHeader from '../components/PageHeader';
import { useDados } from '../hooks/useDados';

const buscarTodas = () => listarCategorias();

export default function CategoriasPage() {
  const { dados: categorias, erro, carregando, setDados } = useDados(buscarTodas);
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
      if (emEdicao) {
        const atualizada = await atualizarCategoria(emEdicao.id, valores);
        setDados(categorias.map((c) => (c.id === atualizada.id ? atualizada : c)));
        setMensagem('Categoria atualizada com sucesso.');
      } else {
        setDados([...categorias, await criarCategoria(valores)]);
        setMensagem('Categoria cadastrada com sucesso.');
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
      await excluirCategoria(paraExcluir.id);
      setDados(categorias.filter((c) => c.id !== paraExcluir.id));
      setMensagem('Categoria excluída.');
    } catch (e) {
      setErroAcao(e); // 409: categoria em uso
    } finally {
      setProcessando(false);
      setParaExcluir(null);
    }
  }

  const grupos = [['RECEITA', 'Categorias de receita'], ['GASTO', 'Categorias de gasto']];

  return (
    <div className="space-y-4">
      <PageHeader titulo="Categorias" descricao="Compartilhadas por todos os usuários" />
      {mensagem && <Alerta tipo="sucesso">{mensagem}</Alerta>}
      {erroAcao && <Alerta detalhes={erroAcao.detalhes}>{erroAcao.message}</Alerta>}
      <CategoriaForm key={emEdicao?.id ?? 'nova'} inicial={emEdicao ?? undefined} onSubmit={salvar} onCancelar={() => setEmEdicao(null)} enviando={processando} />
      {carregando && <Carregando />}
      {erro && <Alerta>{erro}</Alerta>}
      {categorias && (
        <div className="grid gap-4 md:grid-cols-2">
          {grupos.map(([tipo, titulo]) => (
            <section key={tipo} className="rounded-2xl bg-white p-5 ring-1 ring-slate-200">
              <h2 className="mb-2 font-semibold">{titulo}</h2>
              <ul className="divide-y divide-slate-100 text-sm">
                {categorias.filter((c) => c.tipo === tipo).map((c) => (
                  <li key={c.id} className="flex items-center justify-between py-2">
                    <span>{c.nome}</span>
                    <span className="whitespace-nowrap">
                      <button type="button" onClick={() => setEmEdicao(c)} className="text-brand hover:underline">Editar</button>
                      <button type="button" onClick={() => setParaExcluir(c)} className="ml-3 text-red-600 hover:underline">Excluir</button>
                    </span>
                  </li>
                ))}
              </ul>
            </section>
          ))}
        </div>
      )}
      <ConfirmarExclusao
        aberto={Boolean(paraExcluir)}
        mensagem={`Excluir a categoria "${paraExcluir?.nome}"?`}
        onConfirmar={confirmarExclusao}
        onCancelar={() => setParaExcluir(null)}
        processando={processando}
      />
    </div>
  );
}
