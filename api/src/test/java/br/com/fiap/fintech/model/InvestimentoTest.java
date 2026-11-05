package br.com.fiap.fintech.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InvestimentoTest {

    @Test
    void rendimentoEstimadoEhValorAplicadoVezesTaxaAnual() {
        Investimento cdb = new Investimento();
        cdb.setValorAplicado(new BigDecimal("5000.00"));
        cdb.setTaxaRentabilidade(new BigDecimal("0.1250"));

        assertThat(cdb.calcularRendimentoEstimado()).isEqualByComparingTo("625.00");
    }

    @Test
    void semTaxaNaoHaRendimentoEstimado() {
        Investimento acao = new Investimento();
        acao.setValorAplicado(new BigDecimal("1000.00"));

        assertThat(acao.calcularRendimentoEstimado()).isNull();
    }
}
