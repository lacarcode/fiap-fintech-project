package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ContaRequest;
import br.com.fiap.fintech.dto.ContaResponse;
import br.com.fiap.fintech.exception.ConflitoException;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Dominios;
import br.com.fiap.fintech.model.Usuario;
import br.com.fiap.fintech.repository.ContaBancariaRepository;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ContaBancariaService {

    private static final Set<String> TIPOS = Set.of("CORRENTE", "POUPANCA", "PAGAMENTO", "SALARIO", "INVESTIMENTO");

    private final ContaBancariaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReceitaRepository receitaRepository;
    private final GastoRepository gastoRepository;
    private final InvestimentoRepository investimentoRepository;

    public ContaBancariaService(ContaBancariaRepository contaRepository, UsuarioRepository usuarioRepository,
                                ReceitaRepository receitaRepository, GastoRepository gastoRepository,
                                InvestimentoRepository investimentoRepository) {
        this.contaRepository = contaRepository;
        this.usuarioRepository = usuarioRepository;
        this.receitaRepository = receitaRepository;
        this.gastoRepository = gastoRepository;
        this.investimentoRepository = investimentoRepository;
    }

    /** usuarioId nulo lista todas as contas. */
    @Transactional(readOnly = true)
    public List<ContaResponse> listar(Long usuarioId) {
        var contas = usuarioId == null
                ? contaRepository.findAllByOrderByIdAsc()
                : contaRepository.findByUsuarioIdOrderByIdAsc(usuarioId);
        return contas.stream().map(ContaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ContaResponse buscar(Long id) {
        return ContaResponse.de(obter(id));
    }

    @Transactional
    public ContaResponse criar(ContaRequest req) {
        Usuario usuario = usuarioRepository.findById(req.usuarioId())
                .orElseThrow(() -> new RegraNegocioException("Usuário " + req.usuarioId() + " não encontrado"));
        ContaBancaria conta = new ContaBancaria(usuario, req.nmBanco().trim(), tipoValido(req.tipoConta()),
                req.nrConta().trim());
        if (req.ativa() != null) {
            conta.setAtiva(req.ativa());
        }
        return ContaResponse.de(contaRepository.save(conta));
    }

    /** Com ativa=false a conta é desativada (antigo desativarConta). */
    @Transactional
    public ContaResponse atualizar(Long id, ContaRequest req) {
        ContaBancaria conta = obter(id);
        Posse.garantirMesmoDono(conta.getUsuario(), req.usuarioId());
        conta.setNmBanco(req.nmBanco().trim());
        conta.setTipoConta(tipoValido(req.tipoConta()));
        conta.setNrConta(req.nrConta().trim());
        if (req.ativa() != null) {
            conta.setAtiva(req.ativa());
        }
        return ContaResponse.de(conta);
    }

    @Transactional
    public void excluir(Long id) {
        ContaBancaria conta = obter(id);
        if (receitaRepository.existsByContaId(id) || gastoRepository.existsByContaId(id)
                || investimentoRepository.existsByContaId(id)) {
            throw new ConflitoException("A conta possui lançamentos vinculados; desative-a em vez de excluir");
        }
        contaRepository.delete(conta);
    }

    /** Regra usada por receitas, gastos e investimentos: só conta ativa do próprio usuário (D-14). */
    @Transactional(readOnly = true)
    public ContaBancaria obterContaAtivaDoUsuario(Long contaId, Long usuarioId) {
        ContaBancaria conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new RegraNegocioException("Conta " + contaId + " não encontrada"));
        if (!conta.getUsuario().getId().equals(usuarioId)) {
            throw new RegraNegocioException("A conta informada não pertence ao usuário");
        }
        if (!Boolean.TRUE.equals(conta.getAtiva())) {
            throw new RegraNegocioException("A conta informada está inativa");
        }
        return conta;
    }

    private ContaBancaria obter(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta " + id + " não encontrada"));
    }

    private static String tipoValido(String tipoConta) {
        String tipo = Dominios.normalizar(tipoConta);
        if (!TIPOS.contains(tipo)) {
            throw new RegraNegocioException("Tipo de conta inválido: " + tipoConta);
        }
        return tipo;
    }
}
