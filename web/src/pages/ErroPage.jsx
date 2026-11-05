import { isRouteErrorResponse, Link, useRouteError } from 'react-router-dom';

/** Usada como rota "*" (404) e como errorElement (erro inesperado em qualquer rota). */
export default function ErroPage() {
  const erro = useRouteError();
  const naoEncontrada = !erro || (isRouteErrorResponse(erro) && erro.status === 404);
  return (
    <main className="grid min-h-screen place-items-center px-4 text-center">
      <div>
        <p className="text-6xl font-bold text-brand">{naoEncontrada ? '404' : 'Ops!'}</p>
        <h1 className="mt-4 text-2xl font-semibold text-slate-900">
          {naoEncontrada ? 'Página não encontrada' : 'Algo deu errado'}
        </h1>
        <p className="mt-2 text-sm text-slate-500">
          {naoEncontrada ? 'O endereço acessado não existe ou foi removido.' : 'Ocorreu um erro inesperado. Tente novamente em instantes.'}
        </p>
        <Link to="/" className="mt-6 inline-block rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-claro">
          Voltar ao início
        </Link>
      </div>
    </main>
  );
}
