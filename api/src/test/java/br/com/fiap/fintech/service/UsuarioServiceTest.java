package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.UsuarioCreateRequest;
import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.dto.UsuarioUpdateRequest;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.ContaBancariaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock ReceitaRepository receitaRepository;
    @Mock GastoRepository gastoRepository;
    @Mock InvestimentoRepository investimentoRepository;
    @Mock ContaBancariaRepository contaRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, receitaRepository, gastoRepository, investimentoRepository,
                contaRepository, encoder);
    }

    private Usuario ana() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Ana");
        u.setEmail("ana@email.com");
        u.setSenha(encoder.encode("senhaAntiga"));
        u.setAtivo(true);
        return u;
    }

    @Test
    void criarNormalizaEmailEGuardaSoOHashDaSenha() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@email.com")).thenReturn(false);
        when(usuarioRepository.save(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse resposta = service.criar(new UsuarioCreateRequest(" Ana ", " Ana@Email.com ", "segredo1", null));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertThat(salvo.getValue().getEmail()).isEqualTo("ana@email.com");
        assertThat(salvo.getValue().getNome()).isEqualTo("Ana");
        assertThat(salvo.getValue().getSenha()).isNotEqualTo("segredo1");
        assertThat(encoder.matches("segredo1", salvo.getValue().getSenha())).isTrue();
        assertThat(resposta.id()).isEqualTo(1L);
    }

    @Test
    void criarComEmailJaCadastradoLancaConflito() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@email.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new UsuarioCreateRequest("Ana", "ana@email.com", "segredo1", null)))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("E-mail já cadastrado");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado");
    }

    @Test
    void atualizarSemSenhaMantemOHashAtual() {
        Usuario ana = ana();
        String hashAntigo = ana.getSenha();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("ana.nova@email.com", 1L)).thenReturn(false);

        UsuarioResponse resposta = service.atualizar(1L,
                new UsuarioUpdateRequest("Ana Maria", "ana.nova@email.com", null, null, null));

        assertThat(resposta.nome()).isEqualTo("Ana Maria");
        assertThat(ana.getEmail()).isEqualTo("ana.nova@email.com");
        assertThat(ana.getSenha()).isEqualTo(hashAntigo);
    }

    @Test
    void atualizarComEmailDeOutroUsuarioLancaConflito() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana()));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("bia@email.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, new UsuarioUpdateRequest("Ana", "bia@email.com", null, null, null)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void excluirRemoveLancamentosEContasAntesDoUsuario() {
        Usuario ana = ana();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));

        service.excluir(1L);

        InOrder ordem = inOrder(receitaRepository, gastoRepository, investimentoRepository, contaRepository,
                usuarioRepository);
        ordem.verify(receitaRepository).deleteByUsuarioId(1L);
        ordem.verify(gastoRepository).deleteByUsuarioId(1L);
        ordem.verify(investimentoRepository).deleteByUsuarioId(1L);
        ordem.verify(contaRepository).deleteByUsuarioId(1L);
        ordem.verify(usuarioRepository).delete(ana);
        ordem.verify(usuarioRepository).flush();
    }
}
