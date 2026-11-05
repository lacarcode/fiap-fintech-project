package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.Categoria;

public record CategoriaResponse(Long id, String nome, String tipo) {

    public static CategoriaResponse de(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getTipo());
    }
}
