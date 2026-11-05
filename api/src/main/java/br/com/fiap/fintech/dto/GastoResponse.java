package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.Gasto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoResponse(Long id, Long usuarioId, Long contaId, String conta, Long categoriaId, String categoria,
                            String descricao, BigDecimal valor, LocalDate dtGasto, String tipo, String comprovante) {

    public static GastoResponse de(Gasto g) {
        return new GastoResponse(g.getId(), g.getUsuario().getId(), g.getConta().getId(), g.getConta().getRotulo(),
                g.getCategoria().getId(), g.getCategoria().getNome(), g.getDescricao(), g.getValor(), g.getDtGasto(),
                g.getTipo(), g.getComprovante());
    }
}
