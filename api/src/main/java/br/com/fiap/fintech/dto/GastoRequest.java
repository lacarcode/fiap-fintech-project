package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoRequest(
        @NotNull Long usuarioId,
        @NotNull Long contaId,
        @NotNull Long categoriaId,
        @NotBlank @Size(max = 300) String descricao,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor,
        @NotNull LocalDate dtGasto,
        @NotBlank String tipo,
        @Size(max = 500) String comprovante) {
}
