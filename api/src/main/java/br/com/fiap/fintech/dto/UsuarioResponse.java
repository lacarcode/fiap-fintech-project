package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.Usuario;

import java.time.LocalDate;

/** Nunca inclui a senha. */
public record UsuarioResponse(Long id, String nome, String email, String avatar, LocalDate dtCadastro, boolean ativo) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getAvatar(), u.getDtCadastro(),
                Boolean.TRUE.equals(u.getAtivo()));
    }
}
