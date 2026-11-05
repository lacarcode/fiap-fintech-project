import { request } from './client';

export const login = (email, senha) => request('/auth/login', { method: 'POST', body: { email, senha } });
