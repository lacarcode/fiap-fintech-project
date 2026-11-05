const ESTILOS = {
  primario: 'bg-brand text-white hover:bg-brand-claro',
  secundario: 'bg-white text-slate-700 ring-1 ring-slate-300 hover:bg-slate-50',
  perigo: 'bg-red-600 text-white hover:bg-red-700',
};

export default function Botao({ variante = 'primario', type = 'button', className = '', ...props }) {
  return (
    <button
      type={type}
      className={`inline-flex items-center justify-center rounded-lg px-4 py-2 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-60 ${ESTILOS[variante]} ${className}`}
      {...props}
    />
  );
}
