package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.InvestimentoRequest;
import br.com.fiap.fintech.dto.InvestimentoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.exception.RegraNegocioException;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Dominios;
import br.com.fiap.fintech.model.Investimento;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class InvestimentoService {

    private static final Set<String> TIPOS =
            Set.of("CDB", "LCI", "LCA", "TESOURO", "ACOES", "FII", "FUNDO", "POUPANCA", "CRIPTO", "OUTROS");
    private static final Set<String> STATUS = Set.of("ATIVO", "RESGATADO", "VENCIDO");

    private final InvestimentoRepository investimentoRepository;
    private final ContaBancariaService contaService;

    public InvestimentoService(InvestimentoRepository investimentoRepository, ContaBancariaService contaService) {
        this.investimentoRepository = investimentoRepository;
        this.contaService = contaService;
    }

    /** usuarioId nulo lista todos os investimentos. */
    @Transactional(readOnly = true)
    public List<InvestimentoResponse> listar(Long usuarioId) {
        var investimentos = usuarioId == null
                ? investimentoRepository.findAllByOrderByDtAplicacaoDescIdDesc()
                : investimentoRepository.findByUsuarioIdOrderByDtAplicacaoDescIdDesc(usuarioId);
        return investimentos.stream().map(InvestimentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public InvestimentoResponse buscar(Long id) {
        return InvestimentoResponse.de(obter(id));
    }

    @Transactional
    public InvestimentoResponse criar(InvestimentoRequest req) {
        Investimento investimento = new Investimento();
        preencher(investimento, req);
        return InvestimentoResponse.de(investimentoRepository.save(investimento));
    }

    @Transactional
    public InvestimentoResponse atualizar(Long id, InvestimentoRequest req) {
        Investimento investimento = obter(id);
        Posse.garantirMesmoDono(investimento.getUsuario(), req.usuarioId());
        preencher(investimento, req);
        return InvestimentoResponse.de(investimento);
    }

    @Transactional
    public void excluir(Long id) {
        investimentoRepository.delete(obter(id));
    }

    private void preencher(Investimento inv, InvestimentoRequest req) {
        ContaBancaria conta = contaService.obterContaAtivaDoUsuario(req.contaId(), req.usuarioId());
        String tipo = Dominios.normalizar(req.tipo());
        if (!TIPOS.contains(tipo)) {
            throw new RegraNegocioException("Tipo de investimento inválido: " + req.tipo());
        }
        if (req.dtVencimento() != null && req.dtVencimento().isBefore(req.dtAplicacao())) {
            throw new RegraNegocioException("A data de vencimento não pode ser anterior à data de aplicação");
        }
        String status = req.status() == null || req.status().isBlank()
                ? (inv.getStatus() == null ? "ATIVO" : inv.getStatus())
                : Dominios.normalizar(req.status());
        if (!STATUS.contains(status)) {
            throw new RegraNegocioException("Status inválido: use ATIVO, RESGATADO ou VENCIDO");
        }
        inv.setUsuario(conta.getUsuario());
        inv.setConta(conta);
        inv.setNomeInvestimento(req.nomeInvestimento().trim());
        inv.setTipo(tipo);
        inv.setInstituicao(req.instituicao());
        inv.setValorAplicado(req.valorAplicado());
        inv.setTaxaRentabilidade(req.taxaRentabilidade());
        inv.setDtAplicacao(req.dtAplicacao());
        inv.setDtVencimento(req.dtVencimento());
        inv.setStatus(status);
    }

    private Investimento obter(Long id) {
        return investimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Investimento " + id + " não encontrado"));
    }
}
