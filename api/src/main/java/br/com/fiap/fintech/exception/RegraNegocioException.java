package br.com.fiap.fintech.exception;

/** 422 — dados válidos no formato, mas que violam regra do Fintech. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
