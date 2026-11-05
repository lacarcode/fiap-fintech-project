package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.Receita;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceitaResponse(Long id, Long usuarioId, Long contaId, String conta, Long categoriaId, String categoria,
                              String descricao, BigDecimal valor, LocalDate dtRecebimento, String origem,
                              String recorrencia, String comprovante) {

    public static ReceitaResponse de(Receita r) {
        return new ReceitaResponse(r.getId(), r.getUsuario().getId(), r.getConta().getId(), r.getConta().getRotulo(),
                r.getCategoria().getId(), r.getCategoria().getNome(), r.getDescricao(), r.getValor(),
                r.getDtRecebimento(), r.getOrigem(), r.getRecorrencia(), r.getComprovante());
    }
}
