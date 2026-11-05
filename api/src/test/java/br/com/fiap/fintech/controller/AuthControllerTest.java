package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.exception.CredenciaisInvalidasException;
import br.com.fiap.fintech.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean AuthService service;

    private static final String CORPO = """
            {"email":"demo@fintech.com","senha":"segredo1"}""";

    @Test
    void loginValidoRetorna200() throws Exception {
        when(service.login(any())).thenReturn(
                new UsuarioResponse(7L, "Usuária Demo", "demo@fintech.com", null, LocalDate.now(), true));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void loginInvalidoRetorna401() throws Exception {
        when(service.login(any())).thenThrow(new CredenciaisInvalidasException("E-mail ou senha inválidos"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));
    }
}
