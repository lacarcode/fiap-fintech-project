package br.com.fiap.fintech.exception;

import java.util.List;

/** Corpo padrão de erro da API. */
public record ApiErro(int status, String mensagem, List<String> detalhes) {
}
