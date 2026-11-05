package br.com.fiap.fintech.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/** Converte exceções em respostas HTTP com corpo ApiErro. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ApiErro> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<ApiErro> conflito(ConflitoException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<ApiErro> regraNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), List.of());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ResponseEntity<ApiErro> credenciais(CredenciaisInvalidasException e) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErro> validacao(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .sorted()
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", detalhes);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<ApiErro> requisicaoInvalida(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida: verifique o formato dos dados enviados", List.of());
    }

    // Constraints do banco (UNIQUE, FK, CHECK) que escaparam das validações do service
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErro> integridade(DataIntegrityViolationException e) {
        return resposta(HttpStatus.CONFLICT,
                "A operação viola uma restrição do banco de dados (registro duplicado ou em uso)", List.of());
    }

    private ResponseEntity<ApiErro> resposta(HttpStatus status, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status).body(new ApiErro(status.value(), mensagem, detalhes));
    }
}
