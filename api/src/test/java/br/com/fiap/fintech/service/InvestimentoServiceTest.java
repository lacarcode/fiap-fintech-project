package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.InvestimentoRequest;
import br.com.fiap.fintech.dto.InvestimentoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Investimento;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestimentoServiceTest {

    @Mock InvestimentoRepository investimentoRepository;
    @Mock ContaBancariaService contaService;
    @InjectMocks InvestimentoService service;
    ContaBancaria conta;

    @BeforeEach
    void setUp() {
        Usuario ana = new Usuario();
        ana.setId(1L);
        conta = new ContaBancaria(ana, "Banco Demo", "INVESTIMENTO", "0001-9");
        conta.setId(5L);
    }

    private InvestimentoRequest pedido(String tipo, LocalDate vencimento, String status) {
        return new InvestimentoRequest(1L, 5L, "CDB Banco Demo", tipo, "Banco Demo", new BigDecimal("5000.00"),
                new BigDecimal("0.1250"), LocalDate.of(2026, 1, 10), vencimento, status);
    }

    @Test
    void criarSemStatusAssumeAtivoECalculaRendimento() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(investimentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InvestimentoResponse r = service.criar(pedido("cdb", LocalDate.of(2028, 1, 10), null));

        assertThat(r.tipo()).isEqualTo("CDB");
        assertThat(r.status()).isEqualTo("ATIVO");
        assertThat(r.rendimentoEstimadoAnual()).isEqualByComparingTo("625.00");
    }

    @Test
    void vencimentoAntesDaAplicacaoEhRecusado() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);

        assertThatThrownBy(() -> service.criar(pedido("CDB", LocalDate.of(2025, 12, 31), null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A data de vencimento não pode ser anterior à data de aplicação");
        verify(investimentoRepository, never()).save(any());
    }

    @Test
    void tipoForaDoDominioEhRecusado() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);

        assertThatThrownBy(() -> service.criar(pedido("Imóvel", null, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageStartingWith("Tipo de investimento inválido");
    }

    @Test
    void atualizarComStatusResgatadoResgataOInvestimento() {
        Investimento existente = new Investimento();
        existente.setId(20L);
        existente.setStatus("ATIVO");
        when(investimentoRepository.findById(20L)).thenReturn(Optional.of(existente));
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);

        assertThat(service.atualizar(20L, pedido("CDB", null, "Resgatado")).status()).isEqualTo("RESGATADO");
    }

    @Test
    void atualizarSemStatusMantemOAtual() {
        Investimento existente = new Investimento();
        existente.setId(20L);
        existente.setStatus("VENCIDO");
        when(investimentoRepository.findById(20L)).thenReturn(Optional.of(existente));
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);

        assertThat(service.atualizar(20L, pedido("CDB", null, null)).status()).isEqualTo("VENCIDO");
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(investimentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Investimento 99 não encontrado");
    }
}
