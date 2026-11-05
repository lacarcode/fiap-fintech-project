package br.com.fiap.fintech.repository;

import br.com.fiap.fintech.model.Categoria;
import br.com.fiap.fintech.model.ContaBancaria;
import br.com.fiap.fintech.model.Gasto;
import br.com.fiap.fintech.model.Investimento;
import br.com.fiap.fintech.model.Receita;
import br.com.fiap.fintech.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Sobe o contexto com ddl-auto=validate contra o Oracle real e grava um grafo completo (rollback no fim). */
@SpringBootTest(properties = "fintech.dados-demo.habilitado=false")
@EnabledIfSystemProperty(named = "it.oracle", matches = "true")
@Transactional
class SchemaOracleTest {

    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired ContaBancariaRepository contas;
    @Autowired ReceitaRepository receitas;
    @Autowired GastoRepository gastos;
    @Autowired InvestimentoRepository investimentos;

    @Test
    void mapeamentoBateComODdlEPersisteTodasAsEntidades() {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste de Integração");
        usuario.setEmail("it-" + System.nanoTime() + "@teste.com");
        usuario.setSenha("$2a$10$hashDeTesteNaoUsadoParaLogin.................");
        usuarios.saveAndFlush(usuario);

        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.getAtivo()).isTrue();
        assertThat(usuario.getDtCadastro()).isEqualTo(LocalDate.now());

        Categoria categoria = categorias.findByNomeAndTipo("Teste IT", "GASTO")
                .orElseGet(() -> categorias.saveAndFlush(new Categoria("Teste IT", "GASTO")));
        ContaBancaria conta = contas.saveAndFlush(new ContaBancaria(usuario, "Banco IT", "CORRENTE", "IT-001"));

        Gasto gasto = new Gasto();
        gasto.setUsuario(usuario);
        gasto.setConta(conta);
        gasto.setCategoria(categoria);
        gasto.setDescricao("Gasto de integração");
        gasto.setValor(new BigDecimal("10.50"));
        gasto.setDtGasto(LocalDate.now());
        gasto.setTipo("VARIAVEL");
        gastos.saveAndFlush(gasto);

        Categoria salario = categorias.findByNomeAndTipo("Teste IT", "RECEITA")
                .orElseGet(() -> categorias.saveAndFlush(new Categoria("Teste IT", "RECEITA")));
        Receita receita = new Receita();
        receita.setUsuario(usuario);
        receita.setConta(conta);
        receita.setCategoria(salario);
        receita.setDescricao("Receita de integração");
        receita.setValor(new BigDecimal("3000.00"));
        receita.setDtRecebimento(LocalDate.now());
        receita.setOrigem("SALARIO");
        receita.setRecorrencia("MENSAL");
        receitas.saveAndFlush(receita);
        assertThat(receitas.existsByContaId(conta.getId())).isTrue();

        Investimento investimento = new Investimento();
        investimento.setUsuario(usuario);
        investimento.setConta(conta);
        investimento.setNomeInvestimento("CDB IT");
        investimento.setTipo("CDB");
        investimento.setValorAplicado(new BigDecimal("1000.00"));
        investimento.setTaxaRentabilidade(new BigDecimal("0.1250"));
        investimento.setDtAplicacao(LocalDate.now());
        investimentos.saveAndFlush(investimento);

        assertThat(gastos.findByUsuarioIdOrderByDtGastoDescIdDesc(usuario.getId())).hasSize(1);
        assertThat(investimentos.findByUsuarioIdOrderByDtAplicacaoDescIdDesc(usuario.getId()))
                .singleElement()
                .satisfies(i -> assertThat(i.getStatus()).isEqualTo("ATIVO"));
        assertThat(usuarios.existsByEmailIgnoreCase(usuario.getEmail().toUpperCase())).isTrue();
    }
}
