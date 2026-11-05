package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.GastoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.service.GastoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GastoController.class)
class GastoControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean GastoService service;

    private static final String CORPO = """
            {"usuarioId":1,"contaId":5,"categoriaId":3,"descricao":"Aluguel","valor":1500.00,
             "dtGasto":"2026-11-01","tipo":"FIXO"}""";

    private final GastoResponse aluguel = new GastoResponse(10L, 1L, 5L, "Banco Demo · 0001-9", 3L, "Moradia",
            "Aluguel", new BigDecimal("1500.00"), LocalDate.of(2026, 11, 1), "FIXO", null);

    @Test
    void listarPorUsuarioRetorna200() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(aluguel));
        mvc.perform(get("/api/gastos").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Moradia"))
                .andExpect(jsonPath("$[0].dtGasto").value("2026-11-01"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Gasto 99 não encontrado"));
        mvc.perform(get("/api/gastos/99")).andExpect(status().isNotFound());
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(aluguel);
        mvc.perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/gastos/10")));
    }

    @Test
    void criarComValorNegativoRetorna400() throws Exception {
        mvc.perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO.replace("1500.00", "-5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value(org.hamcrest.Matchers.startsWith("valor:")));
        verifyNoInteractions(service);
    }

    @Test
    void criarComContaDeOutroUsuarioRetorna422() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("A conta informada não pertence ao usuário"));
        mvc.perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("A conta informada não pertence ao usuário"));
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(10L), any())).thenReturn(aluguel);
        mvc.perform(put("/api/gastos/10").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/gastos/10")).andExpect(status().isNoContent());
        verify(service).excluir(10L);
    }
}
