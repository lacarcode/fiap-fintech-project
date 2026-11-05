const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

/** Erro devolvido pela API (ou de conexão, com status 0). */
export class ApiError extends Error {
  constructor(status, mensagem, detalhes = []) {
    super(mensagem);
    this.name = 'ApiError';
    this.status = status;
    this.detalhes = detalhes;
  }
}

export async function request(caminho, { method = 'GET', body } = {}) {
  let resposta;
  try {
    resposta = await fetch(`${BASE_URL}${caminho}`, {
      method,
      headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar à API. Verifique se o backend está em execução.');
  }
  if (resposta.status === 204) return null;
  const dados = await resposta.json().catch(() => null);
  if (!resposta.ok) {
    throw new ApiError(resposta.status, dados?.mensagem ?? `Erro ${resposta.status}`, dados?.detalhes ?? []);
  }
  return dados;
}
