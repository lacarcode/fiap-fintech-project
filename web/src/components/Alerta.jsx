const CORES = {
  erro: 'bg-red-50 text-red-700 ring-red-200',
  sucesso: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
};

export default function Alerta({ tipo = 'erro', detalhes = [], children }) {
  return (
    <div role={tipo === 'erro' ? 'alert' : 'status'} className={`rounded-lg px-4 py-3 text-sm ring-1 ${CORES[tipo]}`}>
      <p>{children}</p>
      {detalhes.length > 0 && (
        <ul className="mt-1 list-disc pl-5">
          {detalhes.map((d) => <li key={d}>{d}</li>)}
        </ul>
      )}
    </div>
  );
}
