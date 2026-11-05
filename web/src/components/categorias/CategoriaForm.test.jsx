import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect, it, vi } from 'vitest';
import CategoriaForm from './CategoriaForm';

it('envia nome sem espaços e o tipo escolhido', async () => {
  const onSubmit = vi.fn();
  render(<CategoriaForm onSubmit={onSubmit} />);

  await userEvent.type(screen.getByLabelText('Nome'), '  Pets  ');
  await userEvent.selectOptions(screen.getByLabelText('Tipo'), 'GASTO');
  await userEvent.click(screen.getByRole('button', { name: 'Adicionar categoria' }));

  expect(onSubmit).toHaveBeenCalledWith({ nome: 'Pets', tipo: 'GASTO' });
});
