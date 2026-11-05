import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect, it, vi } from 'vitest';
import ContaForm from './ContaForm';

it('cria conta ativa por padrão', async () => {
  const onSubmit = vi.fn();
  render(<ContaForm onSubmit={onSubmit} />);

  await userEvent.type(screen.getByLabelText('Banco'), ' Nubank ');
  await userEvent.selectOptions(screen.getByLabelText('Tipo de conta'), 'POUPANCA');
  await userEvent.type(screen.getByLabelText('Número da conta'), '1234-5');
  await userEvent.click(screen.getByRole('button', { name: 'Adicionar conta' }));

  expect(onSubmit).toHaveBeenCalledWith({ nmBanco: 'Nubank', tipoConta: 'POUPANCA', nrConta: '1234-5', ativa: true });
});

it('permite desativar uma conta em edição', async () => {
  const onSubmit = vi.fn();
  render(<ContaForm inicial={{ nmBanco: 'Banco Demo', tipoConta: 'CORRENTE', nrConta: '0001-9', ativa: true }} onSubmit={onSubmit} onCancelar={vi.fn()} />);

  await userEvent.click(screen.getByLabelText('Conta ativa'));
  await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ ativa: false }));
});
