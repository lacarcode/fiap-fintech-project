package br.com.fiap.fintech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "T_SF_CATEGORIA")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(name = "nome", nullable = false, length = 80)
    private String nome;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo; // RECEITA ou GASTO

    public Categoria() {
    }

    public Categoria(String nome, String tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
