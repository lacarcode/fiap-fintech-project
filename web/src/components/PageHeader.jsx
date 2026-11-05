export default function PageHeader({ titulo, descricao, acao }) {
  return (
    <header className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">{titulo}</h1>
        {descricao && <p className="text-sm text-slate-500">{descricao}</p>}
      </div>
      {acao}
    </header>
  );
}
