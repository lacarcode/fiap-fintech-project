package br.com.fiap.fintech.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "T_SF_RECEITA")
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receita")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // FK composta (id_conta, id_usuario) existe no banco (D-14); o service garante que a conta é do usuário
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conta", nullable = false)
    private ContaBancaria conta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "descricao", nullable = false, length = 300)
    private String descricao;

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "dt_recebimento", nullable = false)
    private LocalDate dtRecebimento;

    @Column(name = "origem", length = 20)
    private String origem; // SALARIO, FREELANCE, INVESTIMENTO, OUTROS

    @Column(name = "recorrencia", length = 10)
    private String recorrencia; // UNICA, MENSAL, ANUAL

    @Column(name = "comprovante", length = 500)
    private String comprovante;

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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getDtRecebimento() {
        return dtRecebimento;
    }

    public void setDtRecebimento(LocalDate dtRecebimento) {
        this.dtRecebimento = dtRecebimento;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getRecorrencia() {
        return recorrencia;
    }

    public void setRecorrencia(String recorrencia) {
        this.recorrencia = recorrencia;
    }

    public String getComprovante() {
        return comprovante;
    }

    public void setComprovante(String comprovante) {
        this.comprovante = comprovante;
    }
}
