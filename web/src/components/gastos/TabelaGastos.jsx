import { Link } from 'react-router-dom';
import { formatarData, formatarMoeda } from '../../utils/formatar';

const COLUNAS = ['Data', 'Descrição', 'Categoria', 'Conta', 'Tipo', 'Valor', 'Ações'];

export default function TabelaGastos({ gastos, onExcluir }) {
  if (gastos.length === 0) {
    return <p className="rounded-2xl bg-white p-6 text-sm text-slate-500 ring-1 ring-slate-200">Nenhum gasto cadastrado ainda.</p>;
  }
  return (
    <div className="overflow-x-auto rounded-2xl bg-white ring-1 ring-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-500">
          <tr>{COLUNAS.map((c) => <th key={c} className="px-4 py-3 font-medium">{c}</th>)}</tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {gastos.map((g) => (
            <tr key={g.id}>
              <td className="whitespace-nowrap px-4 py-3">{formatarData(g.dtGasto)}</td>
              <td className="px-4 py-3">{g.descricao}</td>
              <td className="px-4 py-3">{g.categoria}</td>
              <td className="px-4 py-3">{g.conta}</td>
              <td className="px-4 py-3">{g.tipo === 'FIXO' ? 'Fixo' : 'Variável'}</td>
              <td className="whitespace-nowrap px-4 py-3 font-medium">{formatarMoeda(g.valor)}</td>
              <td className="whitespace-nowrap px-4 py-3">
                <Link to={`/gastos/${g.id}/editar`} className="text-brand hover:underline">Editar</Link>
                <button type="button" onClick={() => onExcluir(g)} className="ml-3 text-red-600 hover:underline">Excluir</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
