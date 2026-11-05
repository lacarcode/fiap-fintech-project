package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.InvestimentoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.service.InvestimentoService;
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

@WebMvcTest(InvestimentoController.class)
class InvestimentoControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean InvestimentoService service;

    private static final String CORPO = """
            {"usuarioId":1,"contaId":5,"nomeInvestimento":"CDB Banco Demo","tipo":"CDB","valorAplicado":5000.00,
             "taxaRentabilidade":0.1250,"dtAplicacao":"2026-01-10","dtVencimento":"2028-01-10"}""";

    private final InvestimentoResponse cdb = new InvestimentoResponse(20L, 1L, 5L, "Banco Demo · 0001-9",
            "CDB Banco Demo", "CDB", "Banco Demo", new BigDecimal("5000.00"), new BigDecimal("0.1250"),
            LocalDate.of(2026, 1, 10), LocalDate.of(2028, 1, 10), "ATIVO", new BigDecimal("625.00"));

    @Test
    void listarPorUsuarioRetorna200ComRendimento() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(cdb));
        mvc.perform(get("/api/investimentos").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rendimentoEstimadoAnual").value(625.00));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Investimento 99 não encontrado"));
        mvc.perform(get("/api/investimentos/99")).andExpect(status().isNotFound());
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(cdb);
        mvc.perform(post("/api/investimentos").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/investimentos/20")));
    }

    @Test
    void criarComTaxaNegativaRetorna400() throws Exception {
        mvc.perform(post("/api/investimentos").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO.replace("0.1250", "-0.01")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void criarComVencimentoInvalidoRetorna422() throws Exception {
        when(service.criar(any())).thenThrow(
                new RegraNegocioException("A data de vencimento não pode ser anterior à data de aplicação"));
        mvc.perform(post("/api/investimentos").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(20L), any())).thenReturn(cdb);
        mvc.perform(put("/api/investimentos/20").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/investimentos/20")).andExpect(status().isNoContent());
        verify(service).excluir(20L);
    }
}
