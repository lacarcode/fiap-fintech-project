import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, expect, it, vi } from 'vitest';
import { listarGastos } from '../api/gastos';
import { listarReceitas } from '../api/receitas';
import { buscarResumo } from '../api/usuarios';
import { AuthProvider } from '../contexts/AuthContext';
import DashboardPage from './DashboardPage';

vi.mock('../api/usuarios', () => ({ buscarResumo: vi.fn() }));
vi.mock('../api/receitas', () => ({ listarReceitas: vi.fn() }));
vi.mock('../api/gastos', () => ({ listarGastos: vi.fn() }));

beforeEach(() => {
  localStorage.setItem('fintech.usuario', JSON.stringify({ id: 1, nome: 'Usuária Demo', email: 'demo@fintech.com' }));
  vi.mocked(buscarResumo).mockResolvedValue({
    mes: '2026-11', totalReceitas: 5000, totalGastos: 1979.25, saldo: 3020.75, totalInvestido: 5000,
    rendimentoEstimadoAnual: 625, gastosPorCategoria: [{ categoria: 'Moradia', total: 1500 }],
  });
  vi.mocked(listarReceitas).mockResolvedValue([{ id: 1, dtRecebimento: '2026-11-01', descricao: 'Salário do mês', categoria: 'Salário', valor: 5000 }]);
  vi.mocked(listarGastos).mockResolvedValue([{ id: 2, dtGasto: '2026-11-03', descricao: 'Aluguel', categoria: 'Moradia', valor: 1500 }]);
});

it('mostra o saldo calculado pela API e as últimas transações', async () => {
  render(<AuthProvider><MemoryRouter><DashboardPage /></MemoryRouter></AuthProvider>);

  expect(await screen.findByText(/3\.020,75/)).toBeInTheDocument();
  expect(buscarResumo).toHaveBeenCalledWith(1);
  expect(screen.getByText('Salário do mês')).toBeInTheDocument();
  expect(screen.getByText('Aluguel')).toBeInTheDocument();
});
