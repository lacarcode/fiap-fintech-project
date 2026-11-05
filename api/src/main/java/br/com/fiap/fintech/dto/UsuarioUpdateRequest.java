package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** senha e ativo são opcionais: null mantém o valor atual. */
public record UsuarioUpdateRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Pattern(regexp = ".+@.+\\..+", message = "deve ter domínio com ponto") @Size(max = 255) String email,
        @Size(min = 6, max = 72) String senha,
        @Size(max = 500) String avatar,
        Boolean ativo) {
}
