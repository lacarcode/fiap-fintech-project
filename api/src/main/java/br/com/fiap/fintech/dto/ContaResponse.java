package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.ContaBancaria;

public record ContaResponse(Long id, Long usuarioId, String nmBanco, String tipoConta, String nrConta, boolean ativa) {

    public static ContaResponse de(ContaBancaria c) {
        return new ContaResponse(c.getId(), c.getUsuario().getId(), c.getNmBanco(), c.getTipoConta(), c.getNrConta(),
                Boolean.TRUE.equals(c.getAtiva()));
    }
}
