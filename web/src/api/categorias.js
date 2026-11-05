import { request } from './client';

export const listarCategorias = (tipo) => request(`/categorias${tipo ? `?tipo=${tipo}` : ''}`);
export const buscarCategoria = (id) => request(`/categorias/${id}`);
export const criarCategoria = (dados) => request('/categorias', { method: 'POST', body: dados });
export const atualizarCategoria = (id, dados) => request(`/categorias/${id}`, { method: 'PUT', body: dados });
export const excluirCategoria = (id) => request(`/categorias/${id}`, { method: 'DELETE' });
