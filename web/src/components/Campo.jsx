/** Rótulo + input/select/textarea + mensagem de erro. */
export default function Campo({ label, name, as: Elemento = 'input', erro, className = '', children, ...props }) {
  return (
    <label htmlFor={name} className={`block text-sm ${className}`}>
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      <Elemento
        id={name}
        name={name}
        className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20"
        {...props}
      >
        {children}
      </Elemento>
      {erro && <span className="mt-1 block text-xs text-red-600">{erro}</span>}
    </label>
  );
}
