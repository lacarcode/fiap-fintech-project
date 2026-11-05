package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.LoginRequest;
import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.exception.CredenciaisInvalidasException;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Mesma mensagem para e-mail inexistente, senha errada ou usuário inativo (não revela qual falhou). */
    @Transactional(readOnly = true)
    public UsuarioResponse login(LoginRequest req) {
        return usuarioRepository.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .filter(u -> passwordEncoder.matches(req.senha(), u.getSenha()))
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos"));
    }
}
