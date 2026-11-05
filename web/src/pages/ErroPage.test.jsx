import { render, screen } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { expect, it } from 'vitest';
import ErroPage from './ErroPage';

it('mostra a página 404 personalizada para rota inexistente', () => {
  const router = createMemoryRouter(
    [
      { path: '/', element: <p>Início</p> },
      { path: '*', element: <ErroPage /> },
    ],
    { initialEntries: ['/rota-que-nao-existe'] },
  );
  render(<RouterProvider router={router} />);

  expect(screen.getByRole('heading', { name: 'Página não encontrada' })).toBeInTheDocument();
  expect(screen.getByRole('link', { name: 'Voltar ao início' })).toHaveAttribute('href', '/');
});
