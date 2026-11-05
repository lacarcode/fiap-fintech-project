import { request } from './client';

export const listarInvestimentos = (usuarioId) => request(`/investimentos?usuarioId=${usuarioId}`);
export const buscarInvestimento = (id) => request(`/investimentos/${id}`);
export const criarInvestimento = (dados) => request('/investimentos', { method: 'POST', body: dados });
export const atualizarInvestimento = (id, dados) => request(`/investimentos/${id}`, { method: 'PUT', body: dados });
export const excluirInvestimento = (id) => request(`/investimentos/${id}`, { method: 'DELETE' });
