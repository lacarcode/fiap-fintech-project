export default function CardResumo({ titulo, valor, detalhe, destaque = false }) {
  return (
    <div className={`rounded-2xl p-5 ring-1 ${destaque ? 'bg-brand text-white ring-brand' : 'bg-white ring-slate-200'}`}>
      <p className={`text-sm ${destaque ? 'text-white/80' : 'text-slate-500'}`}>{titulo}</p>
      <p className="mt-1 text-2xl font-bold">{valor}</p>
      {detalhe && <p className={`mt-1 text-xs ${destaque ? 'text-white/70' : 'text-slate-400'}`}>{detalhe}</p>}
    </div>
  );
}
