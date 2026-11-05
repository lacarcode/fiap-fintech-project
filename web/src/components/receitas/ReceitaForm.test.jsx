import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { expect, it, vi } from 'vitest';
import ReceitaForm from './ReceitaForm';

const categorias = [{ id: 1, nome: 'Salário' }];
const contas = [{ id: 7, nmBanco: 'Banco Demo', nrConta: '0001-9' }];

it('envia os valores no formato esperado pela API', async () => {
  const onSubmit = vi.fn();
  render(<MemoryRouter><ReceitaForm categorias={categorias} contas={contas} onSubmit={onSubmit} /></MemoryRouter>);

  await userEvent.selectOptions(screen.getByLabelText('Origem'), 'SALARIO');
  await userEvent.type(screen.getByLabelText('Descrição'), ' Salário de novembro ');
  await userEvent.type(screen.getByLabelText('Valor (R$)'), '5000');
  await userEvent.selectOptions(screen.getByLabelText('Recorrência'), 'MENSAL');
  await userEvent.selectOptions(screen.getByLabelText('Conta destino'), '7');
  await userEvent.selectOptions(screen.getByLabelText('Categoria'), '1');
  await userEvent.click(screen.getByRole('button', { name: 'Salvar' }));

  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
    descricao: 'Salário de novembro', valor: 5000, origem: 'SALARIO', recorrencia: 'MENSAL',
    contaId: 7, categoriaId: 1, comprovante: null,
  }));
});

it('envia origem nula quando não informada', async () => {
  const onSubmit = vi.fn();
  render(
    <MemoryRouter>
      <ReceitaForm
        categorias={categorias}
        contas={contas}
        onSubmit={onSubmit}
        inicial={{ descricao: 'Bônus', valor: '300', dtRecebimento: '2026-11-10', categoriaId: 1, contaId: 7 }}
      />
    </MemoryRouter>,
  );
  await userEvent.click(screen.getByRole('button', { name: 'Salvar' }));
  expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ origem: null, recorrencia: 'UNICA' }));
});
