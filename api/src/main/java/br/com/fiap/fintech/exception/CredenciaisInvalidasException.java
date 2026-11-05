package br.com.fiap.fintech.exception;

/** 401 — e-mail ou senha inválidos. */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
