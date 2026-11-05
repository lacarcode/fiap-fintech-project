package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ContaRequest;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.ContaBancariaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaBancariaServiceTest {

    @Mock ContaBancariaRepository contaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ReceitaRepository receitaRepository;
    @Mock GastoRepository gastoRepository;
    @Mock InvestimentoRepository investimentoRepository;
    @InjectMocks ContaBancariaService service;
    Usuario ana;
    ContaBancaria conta;

    @BeforeEach
    void setUp() {
        ana = new Usuario();
        ana.setId(1L);
        conta = new ContaBancaria(ana, "Banco Demo", "CORRENTE", "0001-9");
        conta.setId(5L);
    }

    @Test
    void contaDoProprioUsuarioEhAceita() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));
        assertThat(service.obterContaAtivaDoUsuario(5L, 1L)).isSameAs(conta);
    }

    @Test
    void contaDeOutroUsuarioEhRecusada() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));
        assertThatThrownBy(() -> service.obterContaAtivaDoUsuario(5L, 2L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A conta informada não pertence ao usuário");
    }

    @Test
    void contaInativaEhRecusada() {
        conta.setAtiva(false);
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));
        assertThatThrownBy(() -> service.obterContaAtivaDoUsuario(5L, 1L))
                .hasMessage("A conta informada está inativa");
    }

    @Test
    void contaInexistenteEhRecusada() {
        when(contaRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obterContaAtivaDoUsuario(9L, 1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Conta 9 não encontrada");
    }

    @Test
    void criarNormalizaTipoDeConta() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(contaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.criar(new ContaRequest(1L, "Nubank", "Poupança", "123", null)).tipoConta())
                .isEqualTo("POUPANCA");
    }

    @Test
    void criarComTipoInvalidoEhRecusado() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        assertThatThrownBy(() -> service.criar(new ContaRequest(1L, "Nubank", "Digital", "123", null)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atualizarPodeDesativarAConta() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));

        assertThat(service.atualizar(5L, new ContaRequest(1L, "Banco Demo", "CORRENTE", "0001-9", false)).ativa())
                .isFalse();
    }

    @Test
    void atualizarNaoTransfereContaParaOutroUsuario() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.atualizar(5L, new ContaRequest(2L, "Banco Demo", "CORRENTE", "0001-9", null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é permitido transferir o registro para outro usuário");
    }

    @Test
    void excluirContaComLancamentosLancaConflito() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));
        when(receitaRepository.existsByContaId(5L)).thenReturn(false);
        when(gastoRepository.existsByContaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(5L))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("A conta possui lançamentos vinculados; desative-a em vez de excluir");
        verify(contaRepository, never()).delete(any());
    }

    @Test
    void excluirContaSemLancamentosRemove() {
        when(contaRepository.findById(5L)).thenReturn(Optional.of(conta));

        service.excluir(5L);

        verify(contaRepository).delete(conta);
    }
}
