package br.com.fiap.fintech.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "T_SF_USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha; // hash BCrypt; nunca sai da API

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "dt_cadastro", nullable = false)
    private LocalDate dtCadastro;

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "ativo", nullable = false, columnDefinition = "char(1)")
    private Boolean ativo;

    // O DEFAULT do banco não vale quando o Hibernate envia NULL explícito
    @PrePersist
    void aoCriar() {
        if (dtCadastro == null) {
            dtCadastro = LocalDate.now();
        }
        if (ativo == null) {
            ativo = true;
        }
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDate getDtCadastro() {
        return dtCadastro;
    }

    public void setDtCadastro(LocalDate dtCadastro) {
        this.dtCadastro = dtCadastro;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
