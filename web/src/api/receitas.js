import { request } from './client';

export const listarReceitas = (usuarioId) => request(`/receitas?usuarioId=${usuarioId}`);
export const buscarReceita = (id) => request(`/receitas/${id}`);
export const criarReceita = (dados) => request('/receitas', { method: 'POST', body: dados });
export const atualizarReceita = (id, dados) => request(`/receitas/${id}`, { method: 'PUT', body: dados });
export const excluirReceita = (id) => request(`/receitas/${id}`, { method: 'DELETE' });
