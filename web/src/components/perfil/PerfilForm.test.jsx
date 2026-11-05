import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect, it, vi } from 'vitest';
import PerfilForm from './PerfilForm';

const ana = { nome: 'Ana', email: 'ana@email.com', avatar: null };

it('envia senha nula quando o campo fica em branco', async () => {
  const onSubmit = vi.fn();
  render(<PerfilForm inicial={ana} onSubmit={onSubmit} />);

  await userEvent.clear(screen.getByLabelText('Nome'));
  await userEvent.type(screen.getByLabelText('Nome'), 'Ana Maria');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

  expect(onSubmit).toHaveBeenCalledWith({ nome: 'Ana Maria', email: 'ana@email.com', avatar: null, senha: null, ativo: true });
});

it('envia a nova senha quando preenchida', async () => {
  const onSubmit = vi.fn();
  render(<PerfilForm inicial={ana} onSubmit={onSubmit} />);

  await userEvent.type(screen.getByLabelText('Nova senha (opcional)'), 'novaSenha1');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ senha: 'novaSenha1' }));
});
