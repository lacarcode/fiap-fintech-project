import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, expect, it, vi } from 'vitest';
import { login } from '../api/auth';
import { AuthProvider } from '../contexts/AuthContext';
import LoginPage from './LoginPage';

vi.mock('../api/auth', () => ({ login: vi.fn() }));

function renderizar() {
  const router = createMemoryRouter(
    [
      { path: '/login', element: <LoginPage /> },
      { path: '/', element: <p>Página inicial</p> },
    ],
    { initialEntries: ['/login'] },
  );
  render(<AuthProvider><RouterProvider router={router} /></AuthProvider>);
}

beforeEach(() => {
  localStorage.clear();
  vi.mocked(login).mockReset();
});

it('entra e redireciona para a página inicial', async () => {
  vi.mocked(login).mockResolvedValue({ id: 1, nome: 'Usuária Demo', email: 'demo@fintech.com' });
  renderizar();

  await userEvent.type(screen.getByLabelText('E-mail'), 'demo@fintech.com');
  await userEvent.type(screen.getByLabelText('Senha'), 'segredo1');
  await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

  expect(await screen.findByText('Página inicial')).toBeInTheDocument();
  expect(login).toHaveBeenCalledWith('demo@fintech.com', 'segredo1');
});

it('mostra a mensagem da API quando as credenciais são inválidas', async () => {
  vi.mocked(login).mockRejectedValue(new Error('E-mail ou senha inválidos'));
  renderizar();

  await userEvent.type(screen.getByLabelText('E-mail'), 'demo@fintech.com');
  await userEvent.type(screen.getByLabelText('Senha'), 'errada');
  await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

  expect(await screen.findByRole('alert')).toHaveTextContent('E-mail ou senha inválidos');
});
