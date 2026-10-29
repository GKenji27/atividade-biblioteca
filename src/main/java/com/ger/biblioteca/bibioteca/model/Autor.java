package com.ger.biblioteca.bibioteca.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String nacionalidade;
    private int obrasPublicadas;

    public Autor() {}

    public Autor(String nome, String nacionalidade, int obrasPublicadas) {
        this.nome = nome;
        this.nacionalidade = nacionalidade;
        this.obrasPublicadas = obrasPublicadas;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }

    public int getObrasPublicadas() { return obrasPublicadas; }
    public void setObrasPublicadas(int obrasPublicadas) { this.obrasPublicadas = obrasPublicadas; }
}
