import { createBrowserRouter } from 'react-router-dom';
import Layout from './components/Layout';
import RotaProtegida from './components/RotaProtegida';
import CadastroPage from './pages/CadastroPage';
import CategoriasPage from './pages/CategoriasPage';
import ContasPage from './pages/ContasPage';
import DashboardPage from './pages/DashboardPage';
import ErroPage from './pages/ErroPage';
import GastoFormPage from './pages/GastoFormPage';
import GastosPage from './pages/GastosPage';
import InvestimentoFormPage from './pages/InvestimentoFormPage';
import InvestimentosPage from './pages/InvestimentosPage';
import LoginPage from './pages/LoginPage';
import PerfilPage from './pages/PerfilPage';
import ReceitaFormPage from './pages/ReceitaFormPage';
import ReceitasPage from './pages/ReceitasPage';

export const rotas = [
  { path: '/login', element: <LoginPage />, errorElement: <ErroPage /> },
  { path: '/cadastro', element: <CadastroPage />, errorElement: <ErroPage /> },
  {
    path: '/',
    element: <RotaProtegida><Layout /></RotaProtegida>,
    errorElement: <ErroPage />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'receitas', element: <ReceitasPage /> },
      { path: 'receitas/novo', element: <ReceitaFormPage /> },
      { path: 'receitas/:id/editar', element: <ReceitaFormPage /> },
      { path: 'gastos', element: <GastosPage /> },
      { path: 'gastos/novo', element: <GastoFormPage /> },
      { path: 'gastos/:id/editar', element: <GastoFormPage /> },
      { path: 'investimentos', element: <InvestimentosPage /> },
      { path: 'investimentos/novo', element: <InvestimentoFormPage /> },
      { path: 'investimentos/:id/editar', element: <InvestimentoFormPage /> },
      { path: 'contas', element: <ContasPage /> },
      { path: 'categorias', element: <CategoriasPage /> },
      { path: 'perfil', element: <PerfilPage /> },
    ],
  },
  { path: '*', element: <ErroPage /> },
];

export const router = createBrowserRouter(rotas);
