package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.ContaResponse;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.service.ContaBancariaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContaBancariaController.class)
class ContaBancariaControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ContaBancariaService service;

    private static final String CORPO = """
            {"usuarioId":1,"nmBanco":"Banco Demo","tipoConta":"CORRENTE","nrConta":"0001-9"}""";
    private final ContaResponse conta = new ContaResponse(5L, 1L, "Banco Demo", "CORRENTE", "0001-9", true);

    @Test
    void listarPorUsuarioRetorna200() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(conta));
        mvc.perform(get("/api/contas").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nmBanco").value("Banco Demo"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Conta 99 não encontrada"));
        mvc.perform(get("/api/contas/99")).andExpect(status().isNotFound());
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(conta);
        mvc.perform(post("/api/contas").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/contas/5")));
    }

    @Test
    void criarSemBancoRetorna400() throws Exception {
        mvc.perform(post("/api/contas").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO.replace("Banco Demo", "")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void criarComTipoInvalidoRetorna422() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Tipo de conta inválido: DIGITAL"));
        mvc.perform(post("/api/contas").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(5L), any())).thenReturn(conta);
        mvc.perform(put("/api/contas/5").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/contas/5")).andExpect(status().isNoContent());
        verify(service).excluir(5L);
    }

    @Test
    void excluirContaComLancamentosRetorna409() throws Exception {
        doThrow(new ConflitoException("A conta possui lançamentos vinculados; desative-a em vez de excluir"))
                .when(service).excluir(5L);
        mvc.perform(delete("/api/contas/5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("A conta possui lançamentos vinculados; desative-a em vez de excluir"));
    }
}
