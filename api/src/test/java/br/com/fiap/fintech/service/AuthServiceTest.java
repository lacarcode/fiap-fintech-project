package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.LoginRequest;
import br.com.fiap.fintech.exception.CredenciaisInvalidasException;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    AuthService service;
    Usuario demo;

    @BeforeEach
    void setUp() {
        service = new AuthService(usuarioRepository, encoder);
        demo = new Usuario();
        demo.setId(7L);
        demo.setNome("Usuária Demo");
        demo.setEmail("demo@fintech.com");
        demo.setSenha(encoder.encode("segredo1"));
        demo.setAtivo(true);
    }

    @Test
    void loginComSenhaCorretaDevolveUsuario() {
        when(usuarioRepository.findByEmailIgnoreCase("demo@fintech.com")).thenReturn(Optional.of(demo));

        assertThat(service.login(new LoginRequest(" demo@fintech.com ", "segredo1")).id()).isEqualTo(7L);
    }

    @Test
    void loginComSenhaErradaLanca401() {
        when(usuarioRepository.findByEmailIgnoreCase("demo@fintech.com")).thenReturn(Optional.of(demo));

        assertThatThrownBy(() -> service.login(new LoginRequest("demo@fintech.com", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha inválidos");
    }

    @Test
    void usuarioInativoNaoEntra() {
        demo.setAtivo(false);
        when(usuarioRepository.findByEmailIgnoreCase("demo@fintech.com")).thenReturn(Optional.of(demo));

        assertThatThrownBy(() -> service.login(new LoginRequest("demo@fintech.com", "segredo1")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}
