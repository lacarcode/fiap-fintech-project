import { request } from './client';

export const listarGastos = (usuarioId) => request(`/gastos?usuarioId=${usuarioId}`);
export const buscarGasto = (id) => request(`/gastos/${id}`);
export const criarGasto = (dados) => request('/gastos', { method: 'POST', body: dados });
export const atualizarGasto = (id, dados) => request(`/gastos/${id}`, { method: 'PUT', body: dados });
export const excluirGasto = (id) => request(`/gastos/${id}`, { method: 'DELETE' });
