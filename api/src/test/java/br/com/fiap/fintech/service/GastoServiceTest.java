package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.GastoRequest;
import br.com.fiap.fintech.dto.GastoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Gasto;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.CategoriaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
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
class GastoServiceTest {

    @Mock GastoRepository gastoRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock ContaBancariaService contaService;
    @InjectMocks GastoService service;
    ContaBancaria conta;
    Categoria moradia;

    @BeforeEach
    void setUp() {
        Usuario ana = new Usuario();
        ana.setId(1L);
        conta = new ContaBancaria(ana, "Banco Demo", "CORRENTE", "0001-9");
        conta.setId(5L);
        moradia = new Categoria("Moradia", "GASTO");
        moradia.setId(3L);
    }

    private GastoRequest pedido(String tipo) {
        return new GastoRequest(1L, 5L, 3L, " Aluguel ", new BigDecimal("1500.00"), LocalDate.of(2026, 11, 1), tipo, null);
    }

    @Test
    void criarNormalizaTipoEUsaContaECategoria() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(moradia));
        when(gastoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GastoResponse r = service.criar(pedido("Variável"));

        assertThat(r.tipo()).isEqualTo("VARIAVEL");
        assertThat(r.descricao()).isEqualTo("Aluguel");
        assertThat(r.categoria()).isEqualTo("Moradia");
        assertThat(r.conta()).isEqualTo("Banco Demo · 0001-9");
        assertThat(r.usuarioId()).isEqualTo(1L);
    }

    @Test
    void categoriaDeReceitaEhRecusada() {
        Categoria salario = new Categoria("Salário", "RECEITA");
        salario.setId(1L);
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(salario));

        assertThatThrownBy(() -> service.criar(pedido("FIXO")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A categoria informada não é de gasto");
        verify(gastoRepository, never()).save(any());
    }

    @Test
    void tipoForaDoDominioEhRecusado() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(moradia));

        assertThatThrownBy(() -> service.criar(pedido("Eventual")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Tipo de gasto inválido: use FIXO ou VARIAVEL");
    }

    @Test
    void contaDeOutroUsuarioPropagaRegraDeNegocio() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L))
                .thenThrow(new RegraNegocioException("A conta informada não pertence ao usuário"));

        assertThatThrownBy(() -> service.criar(pedido("FIXO"))).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atualizarAlteraOGastoExistente() {
        Gasto existente = new Gasto();
        existente.setId(10L);
        when(gastoRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(moradia));

        GastoResponse r = service.atualizar(10L, pedido("FIXO"));

        assertThat(r.id()).isEqualTo(10L);
        assertThat(existente.getValor()).isEqualByComparingTo("1500.00");
    }

    @Test
    void atualizarNaoTransfereParaOutroUsuario() {
        Usuario bia = new Usuario();
        bia.setId(2L);
        Gasto existente = new Gasto();
        existente.setId(10L);
        existente.setUsuario(bia);
        when(gastoRepository.findById(10L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizar(10L, pedido("FIXO")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é permitido transferir o registro para outro usuário");
    }

    @Test
    void excluirInexistenteLancaNaoEncontrado() {
        when(gastoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Gasto 99 não encontrado");
    }
}
