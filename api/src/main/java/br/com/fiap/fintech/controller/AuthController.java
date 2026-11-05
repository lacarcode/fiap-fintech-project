package br.com.fiap.fintech.controller;

import br.com.fiap.fintech.dto.LoginRequest;
import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }
}
