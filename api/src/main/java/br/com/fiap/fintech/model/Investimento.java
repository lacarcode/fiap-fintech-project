package br.com.fiap.fintech.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "T_SF_INVESTIMENTO")
public class Investimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_investimento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conta", nullable = false)
    private ContaBancaria conta;

    @Column(name = "nm_investimento", nullable = false, length = 150)
    private String nomeInvestimento;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo; // CDB, LCI, LCA, TESOURO, ACOES, FII, FUNDO, POUPANCA, CRIPTO, OUTROS

    @Column(name = "instituicao", length = 100)
    private String instituicao;

    @Column(name = "vl_aplicado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAplicado;

    @Column(name = "tx_rentabilidade", precision = 7, scale = 4)
    private BigDecimal taxaRentabilidade; // fração anual: 0.1250 = 12,5% a.a. (D-17)

    @Column(name = "dt_aplicacao", nullable = false)
    private LocalDate dtAplicacao;

    @Column(name = "dt_vencimento")
    private LocalDate dtVencimento;

    @Column(name = "status", nullable = false, length = 15)
    private String status; // ATIVO, RESGATADO, VENCIDO

    @PrePersist
    void aoCriar() {
        if (status == null) {
            status = "ATIVO";
        }
    }

    /** Rendimento anual estimado (juros simples); null quando não há taxa informada. */
    public BigDecimal calcularRendimentoEstimado() {
        if (taxaRentabilidade == null || valorAplicado == null) {
            return null;
        }
        return valorAplicado.multiply(taxaRentabilidade).setScale(2, RoundingMode.HALF_EVEN);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ContaBancaria getConta() {
        return conta;
    }

    public void setConta(ContaBancaria conta) {
        this.conta = conta;
    }

    public String getNomeInvestimento() {
        return nomeInvestimento;
    }

    public void setNomeInvestimento(String nomeInvestimento) {
        this.nomeInvestimento = nomeInvestimento;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }

    public BigDecimal getValorAplicado() {
        return valorAplicado;
    }

    public void setValorAplicado(BigDecimal valorAplicado) {
        this.valorAplicado = valorAplicado;
    }

    public BigDecimal getTaxaRentabilidade() {
        return taxaRentabilidade;
    }

    public void setTaxaRentabilidade(BigDecimal taxaRentabilidade) {
        this.taxaRentabilidade = taxaRentabilidade;
    }

    public LocalDate getDtAplicacao() {
        return dtAplicacao;
    }

    public void setDtAplicacao(LocalDate dtAplicacao) {
        this.dtAplicacao = dtAplicacao;
    }

    public LocalDate getDtVencimento() {
        return dtVencimento;
    }

    public void setDtVencimento(LocalDate dtVencimento) {
        this.dtVencimento = dtVencimento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
