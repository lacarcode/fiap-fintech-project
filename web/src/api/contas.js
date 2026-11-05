import { request } from './client';

export const listarContas = (usuarioId) => request(`/contas?usuarioId=${usuarioId}`);
export const buscarConta = (id) => request(`/contas/${id}`);
export const criarConta = (dados) => request('/contas', { method: 'POST', body: dados });
export const atualizarConta = (id, dados) => request(`/contas/${id}`, { method: 'PUT', body: dados });
export const excluirConta = (id) => request(`/contas/${id}`, { method: 'DELETE' });
