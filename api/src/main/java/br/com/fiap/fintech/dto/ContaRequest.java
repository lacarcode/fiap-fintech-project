package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** ativa nula = true na criação; na edição mantém o valor atual. */
public record ContaRequest(
        @NotNull Long usuarioId,
        @NotBlank @Size(max = 100) String nmBanco,
        @NotBlank String tipoConta,
        @NotBlank @Size(max = 20) String nrConta,
        Boolean ativa) {
}
