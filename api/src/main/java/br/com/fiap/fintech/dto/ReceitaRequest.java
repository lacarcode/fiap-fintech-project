package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** origem e recorrencia são opcionais; recorrência nula vira UNICA. */
public record ReceitaRequest(
        @NotNull Long usuarioId,
        @NotNull Long contaId,
        @NotNull Long categoriaId,
        @NotBlank @Size(max = 300) String descricao,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor,
        @NotNull LocalDate dtRecebimento,
        String origem,
        String recorrencia,
        @Size(max = 500) String comprovante) {
}
