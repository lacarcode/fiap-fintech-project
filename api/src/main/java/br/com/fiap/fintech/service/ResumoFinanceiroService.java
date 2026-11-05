package br.com.fiap.fintech.service;

import br.com.fiap.fintech.dto.ResumoResponse;
import br.com.fiap.fintech.exception.RecursoNaoEncontradoException;
import br.com.fiap.fintech.model.Investimento;
import br.com.fiap.fintech.repository.GastoRepository;
import br.com.fiap.fintech.repository.InvestimentoRepository;
import br.com.fiap.fintech.repository.ReceitaRepository;
import br.com.fiap.fintech.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/** Visão consolidada do mês: fluxo de caixa (receitas − gastos) e carteira de investimentos ativos. */
@Service
public class ResumoFinanceiroService {

    private final UsuarioRepository usuarioRepository;
    private final ReceitaRepository receitaRepository;
    private final GastoRepository gastoRepository;
    private final InvestimentoRepository investimentoRepository;

    public ResumoFinanceiroService(UsuarioRepository usuarioRepository, ReceitaRepository receitaRepository,
                                   GastoRepository gastoRepository, InvestimentoRepository investimentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.receitaRepository = receitaRepository;
        this.gastoRepository = gastoRepository;
        this.investimentoRepository = investimentoRepository;
    }

    /** mes nulo = mês atual. */
    @Transactional(readOnly = true)
    public ResumoResponse resumir(Long usuarioId, YearMonth mes) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário " + usuarioId + " não encontrado");
        }
        YearMonth referencia = mes == null ? YearMonth.now() : mes;
        LocalDate inicio = referencia.atDay(1);
        LocalDate fim = referencia.atEndOfMonth();

        BigDecimal receitas = zeroSeNulo(receitaRepository.somarNoPeriodo(usuarioId, inicio, fim));
        BigDecimal gastos = zeroSeNulo(gastoRepository.somarNoPeriodo(usuarioId, inicio, fim));

        List<Investimento> ativos = investimentoRepository.findByUsuarioIdOrderByDtAplicacaoDescIdDesc(usuarioId)
                .stream().filter(i -> "ATIVO".equals(i.getStatus())).toList();
        BigDecimal investido = ativos.stream().map(Investimento::getValorAplicado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rendimento = ativos.stream().map(Investimento::calcularRendimentoEstimado)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumoResponse(referencia.toString(), receitas, gastos, receitas.subtract(gastos), investido,
                rendimento, gastoRepository.totaisPorCategoria(usuarioId, inicio, fim));
    }

    private static BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
