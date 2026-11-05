import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { expect, it, vi } from 'vitest';
import GastoForm from './GastoForm';

const categorias = [{ id: 3, nome: 'Moradia' }];
const contas = [{ id: 7, nmBanco: 'Banco Demo', nrConta: '0001-9' }];

it('envia os valores no formato esperado pela API', async () => {
  const onSubmit = vi.fn();
  render(<MemoryRouter><GastoForm categorias={categorias} contas={contas} onSubmit={onSubmit} /></MemoryRouter>);

  await userEvent.type(screen.getByLabelText('Descrição'), '  Aluguel  ');
  await userEvent.type(screen.getByLabelText('Valor (R$)'), '1500.50');
  await userEvent.selectOptions(screen.getByLabelText('Categoria'), '3');
  await userEvent.selectOptions(screen.getByLabelText('Conta'), '7');
  await userEvent.selectOptions(screen.getByLabelText('Tipo'), 'FIXO');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar' }));

  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
    descricao: 'Aluguel', valor: 1500.5, categoriaId: 3, contaId: 7, tipo: 'FIXO', comprovante: null,
  }));
});

it('preenche os campos ao editar um gasto existente', () => {
  render(
    <MemoryRouter>
      <GastoForm
        categorias={categorias}
        contas={contas}
        onSubmit={vi.fn()}
        inicial={{ descricao: 'Mercado', valor: '420.35', dtGasto: '2026-11-01', tipo: 'VARIAVEL', categoriaId: 3, contaId: 7, comprovante: '' }}
      />
    </MemoryRouter>,
  );
  expect(screen.getByLabelText('Descrição')).toHaveValue('Mercado');
  expect(screen.getByLabelText('Categoria')).toHaveValue('3');
});
