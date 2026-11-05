package br.com.fiap.fintech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "T_SF_CONTA_BANCARIA")
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "nm_banco", nullable = false, length = 100)
    private String nmBanco;

    @Column(name = "tipo_conta", nullable = false, length = 20)
    private String tipoConta; // CORRENTE, POUPANCA, PAGAMENTO, SALARIO, INVESTIMENTO

    @Column(name = "nr_conta", nullable = false, length = 20)
    private String nrConta;

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "ativa", nullable = false, columnDefinition = "char(1)")
    private Boolean ativa;

    public ContaBancaria() {
    }

    public ContaBancaria(Usuario usuario, String nmBanco, String tipoConta, String nrConta) {
        this.usuario = usuario;
        this.nmBanco = nmBanco;
        this.tipoConta = tipoConta;
        this.nrConta = nrConta;
        this.ativa = true;
    }

    @PrePersist
    void aoCriar() {
        if (ativa == null) {
            ativa = true;
        }
    }

    /** Rótulo exibido nas telas: "Banco Demo · 0001-9". */
    public String getRotulo() {
        return nmBanco + " · " + nrConta;
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

    public String getNmBanco() {
        return nmBanco;
    }

    public void setNmBanco(String nmBanco) {
        this.nmBanco = nmBanco;
    }

    public String getTipoConta() {
        return tipoConta;
    }

    public void setTipoConta(String tipoConta) {
        this.tipoConta = tipoConta;
    }

    public String getNrConta() {
        return nrConta;
    }

    public void setNrConta(String nrConta) {
        this.nrConta = nrConta;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }
}
