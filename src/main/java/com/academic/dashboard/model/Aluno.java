package com.academic.dashboard.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "alunos")
public class Aluno {

    @Id
    private String id;

    private String nome;

    @Indexed(unique = true)
    private String matricula;

    private String curso;

    private Integer semestreAtual;

    private Double mediaMinimaAprovacao;

    private Double frequenciaMinimaExigida;

    public Aluno() {
    }

    public Aluno(String id, String nome, String matricula, String curso,
                 Integer semestreAtual, Double mediaMinimaAprovacao, Double frequenciaMinimaExigida) {
        this.id = id;
        this.nome = nome;
        this.matricula = matricula;
        this.curso = curso;
        this.semestreAtual = semestreAtual;
        this.mediaMinimaAprovacao = mediaMinimaAprovacao;
        this.frequenciaMinimaExigida = frequenciaMinimaExigida;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Integer getSemestreAtual() {
        return semestreAtual;
    }

    public void setSemestreAtual(Integer semestreAtual) {
        this.semestreAtual = semestreAtual;
    }

    public Double getMediaMinimaAprovacao() {
        return mediaMinimaAprovacao;
    }

    public void setMediaMinimaAprovacao(Double mediaMinimaAprovacao) {
        this.mediaMinimaAprovacao = mediaMinimaAprovacao;
    }

    public Double getFrequenciaMinimaExigida() {
        return frequenciaMinimaExigida;
    }

    public void setFrequenciaMinimaExigida(Double frequenciaMinimaExigida) {
        this.frequenciaMinimaExigida = frequenciaMinimaExigida;
    }
}

