package br.com.fiap.fintech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(
        @NotBlank @Size(max = 150) String nome,
        // @Pattern espelha o CHECK do banco (CK_SF_USUARIO_EMAIL exige ponto no domínio)
        @NotBlank @Email @Pattern(regexp = ".+@.+\\..+", message = "deve ter domínio com ponto") @Size(max = 255) String email,
        @NotBlank @Size(min = 6, max = 72) String senha,
        @Size(max = 500) String avatar) {
}
