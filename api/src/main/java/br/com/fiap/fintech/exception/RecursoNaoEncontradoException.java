package br.com.fiap.fintech.exception;

/** 404 — recurso pedido na URL não existe. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
