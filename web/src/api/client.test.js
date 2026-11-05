import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, request } from './client';

afterEach(() => vi.unstubAllGlobals());

describe('request', () => {
  it('devolve null quando a API responde 204', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 204 })));
    await expect(request('/gastos/1', { method: 'DELETE' })).resolves.toBeNull();
  });

  it('envia JSON e devolve o corpo da resposta', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ id: 1 }), { status: 201, headers: { 'Content-Type': 'application/json' } }),
    );
    vi.stubGlobal('fetch', fetchMock);

    await expect(request('/gastos', { method: 'POST', body: { valor: 10 } })).resolves.toEqual({ id: 1 });
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/gastos', expect.objectContaining({
      method: 'POST',
      body: '{"valor":10}',
      headers: { 'Content-Type': 'application/json' },
    }));
  });

  it('converte o corpo de erro da API em ApiError', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ status: 422, mensagem: 'A conta informada não pertence ao usuário', detalhes: ['x'] }),
      { status: 422, headers: { 'Content-Type': 'application/json' } },
    )));

    const erro = await request('/gastos', { method: 'POST', body: {} }).catch((e) => e);
    expect(erro).toBeInstanceOf(ApiError);
    expect(erro.status).toBe(422);
    expect(erro.message).toBe('A conta informada não pertence ao usuário');
    expect(erro.detalhes).toEqual(['x']);
  });

  it('avisa quando a API está fora do ar', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

    const erro = await request('/gastos').catch((e) => e);
    expect(erro.status).toBe(0);
    expect(erro.message).toMatch(/Não foi possível conectar à API/);
  });
});
