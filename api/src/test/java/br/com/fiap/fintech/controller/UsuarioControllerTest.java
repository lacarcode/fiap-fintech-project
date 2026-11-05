package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean UsuarioService service;

    private final UsuarioResponse ana =
            new UsuarioResponse(1L, "Ana", "ana@email.com", null, LocalDate.of(2026, 1, 10), true);

    @Test
    void listarRetorna200() throws Exception {
        when(service.listar()).thenReturn(List.of(ana));
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana@email.com"));
    }

    @Test
    void buscarRetorna200SemSenha() throws Exception {
        when(service.buscar(1L)).thenReturn(ana);
        mvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void buscarInexistenteRetorna404ComMensagem() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Usuário 99 não encontrado"));
        mvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Usuário 99 não encontrado"));
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(ana);
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@email.com","senha":"segredo1"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/usuarios/1")))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void criarInvalidoRetorna400ComDetalhes() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","email":"sem-arroba","senha":"1"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes").isNotEmpty());
        verifyNoInteractions(service);
    }

    @Test
    void criarComEmailDuplicadoRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(new ConflitoException("E-mail já cadastrado: ana@email.com"));
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@email.com","senha":"segredo1"}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(1L), any())).thenReturn(ana);
        mvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@email.com"}"""))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/usuarios/1")).andExpect(status().isNoContent());
        verify(service).excluir(1L);
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content("{nome"))
                .andExpect(status().isBadRequest());
    }
}
