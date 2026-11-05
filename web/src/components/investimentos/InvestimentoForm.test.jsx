import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { expect, it, vi } from 'vitest';
import InvestimentoForm from './InvestimentoForm';

const contas = [{ id: 7, nmBanco: 'Banco Demo', nrConta: '0001-9' }];

it('converte a taxa de % ao ano para fração e omite campos vazios', async () => {
  const onSubmit = vi.fn();
  render(<MemoryRouter><InvestimentoForm contas={contas} onSubmit={onSubmit} /></MemoryRouter>);

  await userEvent.type(screen.getByLabelText('Nome'), 'Tesouro Selic');
  await userEvent.selectOptions(screen.getByLabelText('Tipo'), 'TESOURO');
  await userEvent.type(screen.getByLabelText('Valor aplicado (R$)'), '2000');
  await userEvent.type(screen.getByLabelText('Rentabilidade (% a.a.)'), '10.75');
  await userEvent.selectOptions(screen.getByLabelText('Conta'), '7');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar' }));

  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
    nomeInvestimento: 'Tesouro Selic', tipo: 'TESOURO', valorAplicado: 2000, taxaRentabilidade: 0.1075,
    contaId: 7, instituicao: null, dtVencimento: null, status: null,
  }));
  expect(screen.queryByLabelText('Status')).not.toBeInTheDocument();
});

it('mostra o status ao editar', () => {
  render(<MemoryRouter><InvestimentoForm contas={contas} onSubmit={vi.fn()} editando inicial={{ status: 'ATIVO' }} /></MemoryRouter>);
  expect(screen.getByLabelText('Status')).toHaveValue('ATIVO');
});
