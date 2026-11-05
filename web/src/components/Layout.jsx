import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const LINKS = [
  { to: '/', rotulo: 'Início', end: true },
  { to: '/receitas', rotulo: 'Receitas' },
  { to: '/gastos', rotulo: 'Gastos' },
  { to: '/investimentos', rotulo: 'Investimentos' },
  { to: '/contas', rotulo: 'Contas' },
  { to: '/categorias', rotulo: 'Categorias' },
  { to: '/perfil', rotulo: 'Perfil' },
];

const estiloLink = ({ isActive }) =>
  `whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium ${isActive ? 'bg-white/15 text-white' : 'text-white/80 hover:bg-white/10'}`;

export default function Layout() {
  const { usuario, sair } = useAuth();
  return (
    <div className="min-h-screen md:flex">
      <aside className="bg-brand text-white md:min-h-screen md:w-60 md:shrink-0">
        <div className="flex items-center justify-between px-5 py-4 md:block">
          <span className="text-xl font-bold">Fintech</span>
          <span className="text-xs text-white/70 md:mt-1 md:block">Olá, {usuario.nome.split(' ')[0]}</span>
        </div>
        <nav className="flex gap-1 overflow-x-auto px-3 pb-3 md:flex-col md:pb-0">
          {LINKS.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end} className={estiloLink}>{l.rotulo}</NavLink>
          ))}
          <button type="button" onClick={sair} className="whitespace-nowrap rounded-lg px-3 py-2 text-left text-sm font-medium text-white/80 hover:bg-white/10">
            Sair
          </button>
        </nav>
      </aside>
      <main className="flex-1 px-4 py-6 md:px-8">
        <Outlet />
      </main>
    </div>
  );
}
