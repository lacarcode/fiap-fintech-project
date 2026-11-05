import { Link } from 'react-router-dom';
import { formatarData, formatarMoeda } from '../../utils/formatar';
import { ORIGENS, RECORRENCIAS } from './ReceitaForm';

const COLUNAS = ['Data', 'Descrição', 'Origem', 'Categoria', 'Conta', 'Recorrência', 'Valor', 'Ações'];
const rotulo = (lista, valor) => lista.find(([v]) => v === valor)?.[1] ?? '—';

export default function TabelaReceitas({ receitas, onExcluir }) {
  if (receitas.length === 0) {
    return <p className="rounded-2xl bg-white p-6 text-sm text-slate-500 ring-1 ring-slate-200">Nenhuma receita cadastrada ainda.</p>;
  }
  return (
    <div className="overflow-x-auto rounded-2xl bg-white ring-1 ring-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-500">
          <tr>{COLUNAS.map((c) => <th key={c} className="px-4 py-3 font-medium">{c}</th>)}</tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {receitas.map((r) => (
            <tr key={r.id}>
              <td className="whitespace-nowrap px-4 py-3">{formatarData(r.dtRecebimento)}</td>
              <td className="px-4 py-3">{r.descricao}</td>
              <td className="px-4 py-3">{rotulo(ORIGENS, r.origem)}</td>
              <td className="px-4 py-3">{r.categoria}</td>
              <td className="px-4 py-3">{r.conta}</td>
              <td className="px-4 py-3">{rotulo(RECORRENCIAS, r.recorrencia)}</td>
              <td className="whitespace-nowrap px-4 py-3 font-medium text-emerald-700">{formatarMoeda(r.valor)}</td>
              <td className="whitespace-nowrap px-4 py-3">
                <Link to={`/receitas/${r.id}/editar`} className="text-brand hover:underline">Editar</Link>
                <button type="button" onClick={() => onExcluir(r)} className="ml-3 text-red-600 hover:underline">Excluir</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
