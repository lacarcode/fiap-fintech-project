package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(@NotBlank @Size(max = 80) String nome, @NotBlank String tipo) {
}
