package br.com.fiap.fintech.config;

import br.com.fiap.fintech.model.*;
import br.com.fiap.fintech.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Garante, a cada subida, as categorias de gasto e o usuário de teste do README (D-F8-05).
 * Se o usuário já existe, só reativa e redefine a senha configurada; os lançamentos de exemplo
 * só são criados na primeira vez.
 */
@Component
@ConditionalOnProperty(name = "fintech.dados-demo.habilitado", havingValue = "true")
public class DadosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDemo.class);
    private static final List<String> CATEGORIAS_GASTO =
            List.of("Moradia", "Alimentação", "Transporte", "Lazer", "Saúde", "Educação");
    private static final List<String> CATEGORIAS_RECEITA = List.of("Salário", "Freelance", "Rendimentos", "Outras receitas");

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ContaBancariaRepository contaRepository;
    private final ReceitaRepository receitaRepository;
    private final GastoRepository gastoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public DadosDemo(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                     ContaBancariaRepository contaRepository, ReceitaRepository receitaRepository,
                     GastoRepository gastoRepository, InvestimentoRepository investimentoRepository,
                     PasswordEncoder passwordEncoder,
                     @Value("${fintech.dados-demo.email}") String email,
                     @Value("${fintech.dados-demo.senha}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.contaRepository = contaRepository;
        this.receitaRepository = receitaRepository;
        this.gastoRepository = gastoRepository;
        this.investimentoRepository = investimentoRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Categoria> categorias = garantirCategorias(CATEGORIAS_GASTO, "GASTO");
        Map<String, Categoria> categoriasReceita = garantirCategorias(CATEGORIAS_RECEITA, "RECEITA");

        var existente = usuarioRepository.findByEmailIgnoreCase(email);
        if (existente.isPresent()) {
            existente.get().setAtivo(true);
            existente.get().setSenha(passwordEncoder.encode(senha));
            log.info("Usuário de teste {} já existe: senha e status redefinidos.", email);
            return;
        }

        Usuario demo = new Usuario();
        demo.setNome("Usuária Demo");
        demo.setEmail(email);
        demo.setSenha(passwordEncoder.encode(senha));
        usuarioRepository.save(demo);

        ContaBancaria conta = contaRepository.save(new ContaBancaria(demo, "Banco Demo", "CORRENTE", "0001-9"));
        LocalDate hoje = LocalDate.now();

        receitaRepository.saveAll(List.of(
                receita(demo, conta, categoriasReceita.get("Salário"), "Salário do mês", "5000.00",
                        hoje.withDayOfMonth(1), "SALARIO", "MENSAL"),
                receita(demo, conta, categoriasReceita.get("Freelance"), "Projeto de site", "1200.00",
                        hoje.withDayOfMonth(Math.min(10, hoje.lengthOfMonth())), "FREELANCE", "UNICA")));

        gastoRepository.saveAll(List.of(
                gasto(demo, conta, categorias.get("Moradia"), "Aluguel", "1500.00", hoje.withDayOfMonth(1), "FIXO"),
                gasto(demo, conta, categorias.get("Alimentação"), "Mercado do mês", "420.35", hoje.minusDays(3), "VARIAVEL"),
                gasto(demo, conta, categorias.get("Transporte"), "Corridas de aplicativo", "58.90", hoje.minusDays(1), "VARIAVEL")));

        investimentoRepository.saveAll(List.of(
                investimento(demo, conta, "CDB Banco Demo", "CDB", "5000.00", "0.1250", hoje.minusMonths(2), hoje.plusYears(2)),
                investimento(demo, conta, "Tesouro Selic", "TESOURO", "2000.00", "0.1075", hoje.minusMonths(1), null)));

        log.info("Usuário de teste {} criado com conta, receitas, gastos e investimentos de exemplo.", email);
    }

    private Map<String, Categoria> garantirCategorias(List<String> nomes, String tipo) {
        return nomes.stream()
                .map(nome -> categoriaRepository.findByNomeAndTipo(nome, tipo)
                        .orElseGet(() -> categoriaRepository.save(new Categoria(nome, tipo))))
                .collect(Collectors.toMap(Categoria::getNome, Function.identity()));
    }

    private static Receita receita(Usuario u, ContaBancaria c, Categoria cat, String descricao, String valor,
                                   LocalDate data, String origem, String recorrencia) {
        Receita r = new Receita();
        r.setUsuario(u);
        r.setConta(c);
        r.setCategoria(cat);
        r.setDescricao(descricao);
        r.setValor(new BigDecimal(valor));
        r.setDtRecebimento(data);
        r.setOrigem(origem);
        r.setRecorrencia(recorrencia);
        return r;
    }

    private static Gasto gasto(Usuario u, ContaBancaria c, Categoria cat, String descricao, String valor,
                               LocalDate data, String tipo) {
        Gasto g = new Gasto();
        g.setUsuario(u);
        g.setConta(c);
        g.setCategoria(cat);
        g.setDescricao(descricao);
        g.setValor(new BigDecimal(valor));
        g.setDtGasto(data);
        g.setTipo(tipo);
        return g;
    }

    private static Investimento investimento(Usuario u, ContaBancaria c, String nome, String tipo, String valor,
                                             String taxa, LocalDate aplicacao, LocalDate vencimento) {
        Investimento i = new Investimento();
        i.setUsuario(u);
        i.setConta(c);
        i.setNomeInvestimento(nome);
        i.setTipo(tipo);
        i.setInstituicao(c.getNmBanco());
        i.setValorAplicado(new BigDecimal(valor));
        i.setTaxaRentabilidade(new BigDecimal(taxa));
        i.setDtAplicacao(aplicacao);
        i.setDtVencimento(vencimento);
        return i;
    }
}
