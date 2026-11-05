package br.com.fiap.fintech.exception;

/** 409 — conflito com dado existente (ex.: e-mail já cadastrado). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
