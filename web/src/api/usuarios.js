import { request } from './client';

export const buscarUsuario = (id) => request(`/usuarios/${id}`);
export const criarUsuario = (dados) => request('/usuarios', { method: 'POST', body: dados });
export const atualizarUsuario = (id, dados) => request(`/usuarios/${id}`, { method: 'PUT', body: dados });
export const excluirUsuario = (id) => request(`/usuarios/${id}`, { method: 'DELETE' });
/** mes no formato AAAA-MM; sem mes = mês atual */
export const buscarResumo = (id, mes) => request(`/usuarios/${id}/resumo${mes ? `?mes=${mes}` : ''}`);
