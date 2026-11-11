package com.academic.dashboard.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AlunoDTO {

    private String id;

    @NotBlank(message = "O nome do estudante e obrigatorio.")
    @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres.")
    private String nome;

    @NotBlank(message = "A matricula do estudante e obrigatoria.")
    @Size(min = 3, max = 40, message = "A matricula deve ter entre 3 e 40 caracteres.")
    private String matricula;

    @NotBlank(message = "O curso e obrigatorio.")
    @Size(min = 2, max = 120, message = "O curso deve ter entre 2 e 120 caracteres.")
    private String curso;

    @NotNull(message = "O semestre atual e obrigatorio.")
    @Min(value = 1, message = "O semestre atual deve ser no minimo 1.")
    @Max(value = 20, message = "O semestre atual nao pode exceder 20.")
    private Integer semestreAtual;

    @NotNull(message = "A media minima de aprovacao e obrigatoria.")
    @DecimalMin(value = "0.0", inclusive = false, message = "A media minima deve ser maior que 0.0.")
    @DecimalMax(value = "10.0", message = "A media minima nao pode exceder 10.0.")
    private Double mediaMinimaAprovacao;

    @DecimalMin(value = "0.0", message = "A frequencia minima exigida nao pode ser negativa.")
    @DecimalMax(value = "100.0", message = "A frequencia minima exigida nao pode exceder 100.0%.")
    private Double frequenciaMinimaExigida = 75.0;

    public AlunoDTO() {
    }

    public AlunoDTO(String id, String nome, String matricula, String curso,
                    Integer semestreAtual, Double mediaMinimaAprovacao, Double frequenciaMinimaExigida) {
        this.id = id;
        this.nome = nome;
        this.matricula = matricula;
        this.curso = curso;
        this.semestreAtual = semestreAtual;
        this.mediaMinimaAprovacao = mediaMinimaAprovacao;
        this.frequenciaMinimaExigida = frequenciaMinimaExigida != null ? frequenciaMinimaExigida : 75.0;
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

