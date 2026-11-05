package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.ReceitaResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.service.ReceitaService;
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

@WebMvcTest(ReceitaController.class)
class ReceitaControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ReceitaService service;

    private static final String CORPO = """
            {"usuarioId":1,"contaId":5,"categoriaId":1,"descricao":"Salário","valor":5000.00,
             "dtRecebimento":"2026-11-05","origem":"SALARIO","recorrencia":"MENSAL"}""";

    private final ReceitaResponse salario = new ReceitaResponse(30L, 1L, 5L, "Banco Demo · 0001-9", 1L, "Salário",
            "Salário", new BigDecimal("5000.00"), LocalDate.of(2026, 11, 5), "SALARIO", "MENSAL", null);

    @Test
    void listarPorUsuarioRetorna200() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(salario));
        mvc.perform(get("/api/receitas").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origem").value("SALARIO"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Receita 99 não encontrada"));
        mvc.perform(get("/api/receitas/99")).andExpect(status().isNotFound());
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(salario);
        mvc.perform(post("/api/receitas").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/receitas/30")));
    }

    @Test
    void criarSemDataRetorna400() throws Exception {
        mvc.perform(post("/api/receitas").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO.replace("\"dtRecebimento\":\"2026-11-05\",", "")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void criarComCategoriaDeGastoRetorna422() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("A categoria informada não é de receita"));
        mvc.perform(post("/api/receitas").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(30L), any())).thenReturn(salario);
        mvc.perform(put("/api/receitas/30").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/receitas/30")).andExpect(status().isNoContent());
        verify(service).excluir(30L);
    }
}
