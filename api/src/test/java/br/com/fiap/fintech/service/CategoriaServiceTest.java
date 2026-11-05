package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.CategoriaRequest;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.repository.CategoriaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
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
class CategoriaServiceTest {

    @Mock CategoriaRepository categoriaRepository;
    @Mock ReceitaRepository receitaRepository;
    @Mock GastoRepository gastoRepository;
    @InjectMocks CategoriaService service;
    Categoria moradia;

    @BeforeEach
    void setUp() {
        moradia = new Categoria("Moradia", "GASTO");
        moradia.setId(3L);
    }

    @Test
    void criarNormalizaTipo() {
        when(categoriaRepository.existsByNomeIgnoreCaseAndTipo("Pets", "GASTO")).thenReturn(false);
        when(categoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.criar(new CategoriaRequest(" Pets ", "gasto")).tipo()).isEqualTo("GASTO");
    }

    @Test
    void criarDuplicadaLancaConflito() {
        when(categoriaRepository.existsByNomeIgnoreCaseAndTipo("Moradia", "GASTO")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new CategoriaRequest("Moradia", "GASTO")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe a categoria 'Moradia' do tipo GASTO");
    }

    @Test
    void tipoForaDoDominioEhRecusado() {
        assertThatThrownBy(() -> service.criar(new CategoriaRequest("Pets", "DESPESA")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Tipo de categoria inválido: use RECEITA ou GASTO");
    }

    @Test
    void naoMudaOTipoDeCategoriaEmUso() {
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(moradia));
        when(categoriaRepository.existsByNomeIgnoreCaseAndTipoAndIdNot("Moradia", "RECEITA", 3L)).thenReturn(false);
        when(receitaRepository.existsByCategoriaId(3L)).thenReturn(false);
        when(gastoRepository.existsByCategoriaId(3L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(3L, new CategoriaRequest("Moradia", "RECEITA")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível mudar o tipo de uma categoria em uso");
    }

    @Test
    void excluirCategoriaEmUsoLancaConflito() {
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(moradia));
        when(receitaRepository.existsByCategoriaId(3L)).thenReturn(false);
        when(gastoRepository.existsByCategoriaId(3L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(3L))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("A categoria está em uso por lançamentos e não pode ser excluída");
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Categoria 99 não encontrada");
    }
}
