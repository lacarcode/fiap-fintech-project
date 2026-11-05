package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.CategoriaResponse;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.service.CategoriaService;
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

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean CategoriaService service;

    private static final String CORPO = """
            {"nome":"Moradia","tipo":"GASTO"}""";
    private final CategoriaResponse moradia = new CategoriaResponse(3L, "Moradia", "GASTO");

    @Test
    void listarPorTipoRetorna200() throws Exception {
        when(service.listar("GASTO")).thenReturn(List.of(moradia));
        mvc.perform(get("/api/categorias").param("tipo", "GASTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Moradia"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Categoria 99 não encontrada"));
        mvc.perform(get("/api/categorias/99")).andExpect(status().isNotFound());
    }

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(moradia);
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/categorias/3")));
    }

    @Test
    void criarDuplicadaRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(new ConflitoException("Já existe a categoria 'Moradia' do tipo GASTO"));
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isConflict());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(service.atualizar(eq(3L), any())).thenReturn(moradia);
        mvc.perform(put("/api/categorias/3").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/categorias/3")).andExpect(status().isNoContent());
        verify(service).excluir(3L);
    }
}
