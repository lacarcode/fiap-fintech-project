import { Link } from 'react-router-dom';
import { formatarData, formatarMoeda, formatarPercentual } from '../../utils/formatar';

const COLUNAS = ['Nome', 'Tipo', 'Aplicado', 'Taxa a.a.', 'Rendimento est./ano', 'Vencimento', 'Status', 'Ações'];
const COR_STATUS = { ATIVO: 'bg-emerald-50 text-emerald-700', RESGATADO: 'bg-slate-100 text-slate-600', VENCIDO: 'bg-amber-50 text-amber-700' };

export default function TabelaInvestimentos({ investimentos, onExcluir }) {
  if (investimentos.length === 0) {
    return <p className="rounded-2xl bg-white p-6 text-sm text-slate-500 ring-1 ring-slate-200">Nenhum investimento cadastrado ainda.</p>;
  }
  return (
    <div className="overflow-x-auto rounded-2xl bg-white ring-1 ring-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-500">
          <tr>{COLUNAS.map((c) => <th key={c} className="px-4 py-3 font-medium">{c}</th>)}</tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {investimentos.map((i) => (
            <tr key={i.id}>
              <td className="px-4 py-3">{i.nomeInvestimento}<span className="block text-xs text-slate-400">{i.instituicao ?? i.conta}</span></td>
              <td className="px-4 py-3">{i.tipo}</td>
              <td className="whitespace-nowrap px-4 py-3 font-medium">{formatarMoeda(i.valorAplicado)}</td>
              <td className="px-4 py-3">{formatarPercentual(i.taxaRentabilidade)}</td>
              <td className="whitespace-nowrap px-4 py-3 text-emerald-700">{i.rendimentoEstimadoAnual == null ? '—' : formatarMoeda(i.rendimentoEstimadoAnual)}</td>
              <td className="whitespace-nowrap px-4 py-3">{formatarData(i.dtVencimento)}</td>
              <td className="px-4 py-3"><span className={`rounded-full px-2 py-1 text-xs font-medium ${COR_STATUS[i.status]}`}>{i.status}</span></td>
              <td className="whitespace-nowrap px-4 py-3">
                <Link to={`/investimentos/${i.id}/editar`} className="text-brand hover:underline">Editar</Link>
                <button type="button" onClick={() => onExcluir(i)} className="ml-3 text-red-600 hover:underline">Excluir</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
