package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ReceitaRequest;
import br.com.fiap.fintech.dto.ReceitaResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Receita;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.CategoriaRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
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
class ReceitaServiceTest {

    @Mock ReceitaRepository receitaRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock ContaBancariaService contaService;
    @InjectMocks ReceitaService service;
    Usuario ana;
    ContaBancaria conta;
    Categoria salario;

    @BeforeEach
    void setUp() {
        ana = new Usuario();
        ana.setId(1L);
        conta = new ContaBancaria(ana, "Banco Demo", "CORRENTE", "0001-9");
        conta.setId(5L);
        salario = new Categoria("Salário", "RECEITA");
        salario.setId(1L);
    }

    private ReceitaRequest pedido(Long usuarioId, String origem, String recorrencia) {
        return new ReceitaRequest(usuarioId, 5L, 1L, " Salário de novembro ", new BigDecimal("5000.00"),
                LocalDate.of(2026, 11, 5), origem, recorrencia, null);
    }

    @Test
    void criarNormalizaDominiosEAssumeRecorrenciaUnica() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(salario));
        when(receitaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReceitaResponse r = service.criar(pedido(1L, "Salário", null));

        assertThat(r.origem()).isEqualTo("SALARIO");
        assertThat(r.recorrencia()).isEqualTo("UNICA");
        assertThat(r.descricao()).isEqualTo("Salário de novembro");
        assertThat(r.categoria()).isEqualTo("Salário");
    }

    @Test
    void categoriaDeGastoEhRecusada() {
        Categoria moradia = new Categoria("Moradia", "GASTO");
        moradia.setId(1L);
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(moradia));

        assertThatThrownBy(() -> service.criar(pedido(1L, null, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A categoria informada não é de receita");
        verify(receitaRepository, never()).save(any());
    }

    @Test
    void origemForaDoDominioEhRecusada() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(salario));

        assertThatThrownBy(() -> service.criar(pedido(1L, "Herança", null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Origem inválida: use SALARIO, FREELANCE, INVESTIMENTO ou OUTROS");
    }

    @Test
    void recorrenciaForaDoDominioEhRecusada() {
        when(contaService.obterContaAtivaDoUsuario(5L, 1L)).thenReturn(conta);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(salario));

        assertThatThrownBy(() -> service.criar(pedido(1L, null, "Semanal")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Recorrência inválida: use UNICA, MENSAL ou ANUAL");
    }

    @Test
    void atualizarNaoTransfereParaOutroUsuario() {
        Receita existente = new Receita();
        existente.setId(30L);
        existente.setUsuario(ana);
        when(receitaRepository.findById(30L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizar(30L, pedido(2L, null, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é permitido transferir o registro para outro usuário");
    }

    @Test
    void excluirInexistenteLancaNaoEncontrado() {
        when(receitaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Receita 99 não encontrada");
    }
}
