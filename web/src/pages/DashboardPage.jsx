import { useCallback } from 'react';
import { Link } from 'react-router-dom';
import { listarGastos } from '../api/gastos';
import { listarReceitas } from '../api/receitas';
import { buscarResumo } from '../api/usuarios';
import Alerta from '../components/Alerta';
import CardResumo from '../components/CardResumo';
import Carregando from '../components/Carregando';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../contexts/AuthContext';
import { useDados } from '../hooks/useDados';
import { formatarData, formatarMoeda } from '../utils/formatar';

export default function DashboardPage() {
  const { usuario } = useAuth();
  const buscar = useCallback(
    () => Promise.all([buscarResumo(usuario.id), listarReceitas(usuario.id), listarGastos(usuario.id)]),
    [usuario.id],
  );
  const { dados, erro, carregando } = useDados(buscar);

  if (carregando) return <Carregando />;
  if (erro) return <Alerta>{erro}</Alerta>;

  const [resumo, receitas, gastos] = dados;
  const [ano, mes] = resumo.mes.split('-');
  // Últimas transações: receitas e gastos juntos, mais recentes primeiro
  const transacoes = [
    ...receitas.map((r) => ({ id: `r${r.id}`, data: r.dtRecebimento, descricao: r.descricao, categoria: r.categoria, valor: r.valor, entrada: true })),
    ...gastos.map((g) => ({ id: `g${g.id}`, data: g.dtGasto, descricao: g.descricao, categoria: g.categoria, valor: g.valor, entrada: false })),
  ].sort((a, b) => b.data.localeCompare(a.data)).slice(0, 6);
  const maiorCategoria = Math.max(1, ...resumo.gastosPorCategoria.map((c) => Number(c.total)));

  return (
    <div className="space-y-8">
      <PageHeader titulo={`Olá, ${usuario.nome.split(' ')[0]}!`} descricao={`Resumo financeiro de ${mes}/${ano}`} />
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <CardResumo destaque titulo="Saldo do mês" valor={formatarMoeda(resumo.saldo)} detalhe="Receitas − gastos" />
        <CardResumo titulo="Receitas no mês" valor={formatarMoeda(resumo.totalReceitas)} />
        <CardResumo titulo="Gastos no mês" valor={formatarMoeda(resumo.totalGastos)} />
        <CardResumo titulo="Total investido" valor={formatarMoeda(resumo.totalInvestido)} detalhe={`Rendimento estimado: ${formatarMoeda(resumo.rendimentoEstimadoAnual)}/ano`} />
      </section>
      <div className="grid gap-4 lg:grid-cols-2">
        <section className="rounded-2xl bg-white p-5 ring-1 ring-slate-200">
          <h2 className="mb-3 font-semibold">Gastos por categoria</h2>
          {resumo.gastosPorCategoria.length === 0 ? (
            <p className="text-sm text-slate-500">Nenhum gasto neste mês.</p>
          ) : (
            <ul className="space-y-3">
              {resumo.gastosPorCategoria.map((c) => (
                <li key={c.categoria}>
                  <div className="flex justify-between text-sm"><span>{c.categoria}</span><span className="font-medium">{formatarMoeda(c.total)}</span></div>
                  <div className="mt-1 h-2 rounded-full bg-slate-100">
                    <div className="h-2 rounded-full bg-brand" style={{ width: `${(Number(c.total) / maiorCategoria) * 100}%` }} />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
        <section className="rounded-2xl bg-white p-5 ring-1 ring-slate-200">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="font-semibold">Últimas transações</h2>
            <span className="space-x-3 text-sm">
              <Link to="/receitas" className="text-brand hover:underline">Receitas</Link>
              <Link to="/gastos" className="text-brand hover:underline">Gastos</Link>
            </span>
          </div>
          {transacoes.length === 0 ? (
            <p className="text-sm text-slate-500">Nenhuma transação registrada.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {transacoes.map((t) => (
                <li key={t.id} className="flex justify-between gap-4 py-2 text-sm">
                  <span>{t.descricao} <span className="text-slate-400">· {t.categoria} · {formatarData(t.data)}</span></span>
                  <span className={`whitespace-nowrap font-medium ${t.entrada ? 'text-emerald-700' : 'text-red-600'}`}>
                    {t.entrada ? '+' : '-'}{formatarMoeda(t.valor)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
