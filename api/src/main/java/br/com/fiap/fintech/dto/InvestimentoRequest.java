package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** taxaRentabilidade em fração anual (0.1250 = 12,5% a.a.); status nulo = ATIVO na criação ou mantém na edição. */
public record InvestimentoRequest(
        @NotNull Long usuarioId,
        @NotNull Long contaId,
        @NotBlank @Size(max = 150) String nomeInvestimento,
        @NotBlank String tipo,
        @Size(max = 100) String instituicao,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valorAplicado,
        @PositiveOrZero @Digits(integer = 3, fraction = 4) BigDecimal taxaRentabilidade,
        @NotNull LocalDate dtAplicacao,
        LocalDate dtVencimento,
        String status) {
}
