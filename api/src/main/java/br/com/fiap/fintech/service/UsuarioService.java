package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.UsuarioCreateRequest;
import br.com.fiap.fintech.dto.UsuarioResponse;
import br.com.fiap.fintech.dto.UsuarioUpdateRequest;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.ContaBancariaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ReceitaRepository receitaRepository;
    private final GastoRepository gastoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final ContaBancariaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, ReceitaRepository receitaRepository,
                          GastoRepository gastoRepository, InvestimentoRepository investimentoRepository,
                          ContaBancariaRepository contaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.receitaRepository = receitaRepository;
        this.gastoRepository = gastoRepository;
        this.investimentoRepository = investimentoRepository;
        this.contaRepository = contaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll(Sort.by("id")).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.de(obter(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCreateRequest req) {
        String email = normalizarEmail(req.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("E-mail já cadastrado: " + email);
        }
        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(req.senha()));
        usuario.setAvatar(req.avatar());
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest req) {
        Usuario usuario = obter(id);
        String email = normalizarEmail(req.email());
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflitoException("E-mail já cadastrado: " + email);
        }
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setAvatar(req.avatar());
        if (req.ativo() != null) {
            usuario.setAtivo(req.ativo());
        }
        if (req.senha() != null && !req.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(req.senha()));
        }
        return UsuarioResponse.de(usuario);
    }

    /** Encerra o cadastro: apaga receitas, gastos, investimentos e contas do usuário (D-F8-06). */
    @Transactional
    public void excluir(Long id) {
        Usuario usuario = obter(id);
        receitaRepository.deleteByUsuarioId(id);
        gastoRepository.deleteByUsuarioId(id);
        investimentoRepository.deleteByUsuarioId(id);
        contaRepository.deleteByUsuarioId(id);
        usuarioRepository.delete(usuario);
        // Força o DELETE aqui: se houver metas/dívidas, a FK vira 409 no handler
        usuarioRepository.flush();
    }

    private Usuario obter(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
