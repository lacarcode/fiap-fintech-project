import Botao from './Botao';

export default function ConfirmarExclusao({ aberto, titulo = 'Confirmar exclusão', mensagem, onConfirmar, onCancelar, processando = false }) {
  if (!aberto) return null;
  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-900/40 px-4" role="dialog" aria-modal="true" aria-labelledby="confirmar-titulo">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <h2 id="confirmar-titulo" className="text-lg font-semibold text-slate-900">{titulo}</h2>
        <p className="mt-2 text-sm text-slate-600">{mensagem}</p>
        <div className="mt-6 flex justify-end gap-2">
          <Botao variante="secundario" onClick={onCancelar} disabled={processando}>Cancelar</Botao>
          <Botao variante="perigo" onClick={onConfirmar} disabled={processando}>
            {processando ? 'Excluindo…' : 'Excluir'}
          </Botao>
        </div>
      </div>
    </div>
  );
}
