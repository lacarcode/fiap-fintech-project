package br.com.fiap.fintech.dto;

import br.com.fiap.fintech.model.Investimento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestimentoResponse(Long id, Long usuarioId, Long contaId, String conta, String nomeInvestimento,
                                   String tipo, String instituicao, BigDecimal valorAplicado,
                                   BigDecimal taxaRentabilidade, LocalDate dtAplicacao, LocalDate dtVencimento,
                                   String status, BigDecimal rendimentoEstimadoAnual) {

    public static InvestimentoResponse de(Investimento i) {
        return new InvestimentoResponse(i.getId(), i.getUsuario().getId(), i.getConta().getId(),
                i.getConta().getRotulo(), i.getNomeInvestimento(), i.getTipo(), i.getInstituicao(),
                i.getValorAplicado(), i.getTaxaRentabilidade(), i.getDtAplicacao(), i.getDtVencimento(),
                i.getStatus(), i.calcularRendimentoEstimado());
    }
}
