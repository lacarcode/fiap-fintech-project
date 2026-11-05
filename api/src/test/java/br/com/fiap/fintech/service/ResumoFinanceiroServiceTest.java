package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ResumoResponse;
import br.com.fiap.fintech.dto.TotalPorCategoria;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.model.Investimento;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumoFinanceiroServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock ReceitaRepository receitaRepository;
    @Mock GastoRepository gastoRepository;
    @Mock InvestimentoRepository investimentoRepository;
    @InjectMocks ResumoFinanceiroService service;

    private final YearMonth novembro = YearMonth.of(2026, 11);

    private static Investimento investimento(String valor, String taxa, String status) {
        Investimento i = new Investimento();
        i.setValorAplicado(new BigDecimal(valor));
        i.setTaxaRentabilidade(new BigDecimal(taxa));
        i.setStatus(status);
        return i;
    }

    @Test
    void saldoEhReceitasMenosGastosEInvestimentoSoContaSeAtivo() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(receitaRepository.somarNoPeriodo(1L, novembro.atDay(1), novembro.atEndOfMonth()))
                .thenReturn(new BigDecimal("5000.00"));
        when(gastoRepository.somarNoPeriodo(1L, novembro.atDay(1), novembro.atEndOfMonth()))
                .thenReturn(new BigDecimal("1979.25"));
        when(gastoRepository.totaisPorCategoria(1L, novembro.atDay(1), novembro.atEndOfMonth()))
                .thenReturn(List.of(new TotalPorCategoria("Moradia", new BigDecimal("1500.00"))));
        when(investimentoRepository.findByUsuarioIdOrderByDtAplicacaoDescIdDesc(1L)).thenReturn(List.of(
                investimento("5000.00", "0.1250", "ATIVO"),
                investimento("1000.00", "0.1000", "RESGATADO")));

        ResumoResponse r = service.resumir(1L, novembro);

        assertThat(r.mes()).isEqualTo("2026-11");
        assertThat(r.saldo()).isEqualByComparingTo("3020.75");
        assertThat(r.totalInvestido()).isEqualByComparingTo("5000.00");
        assertThat(r.rendimentoEstimadoAnual()).isEqualByComparingTo("625.00");
        assertThat(r.gastosPorCategoria()).extracting(TotalPorCategoria::categoria).containsExactly("Moradia");
    }

    @Test
    void mesSemLancamentosResultaEmZeros() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        ResumoResponse r = service.resumir(1L, novembro);

        assertThat(r.totalReceitas()).isEqualByComparingTo("0");
        assertThat(r.totalGastos()).isEqualByComparingTo("0");
        assertThat(r.saldo()).isEqualByComparingTo("0");
        assertThat(r.gastosPorCategoria()).isEmpty();
    }

    @Test
    void usuarioInexistenteLancaNaoEncontrado() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.resumir(99L, novembro))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado");
    }
}
