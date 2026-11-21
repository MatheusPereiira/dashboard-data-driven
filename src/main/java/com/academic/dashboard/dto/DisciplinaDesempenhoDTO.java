package com.academic.dashboard.dto;

import com.academic.dashboard.model.StatusSituacao;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DisciplinaDesempenhoDTO {

    private String id;

    @NotBlank(message = "O identificador do aluno (alunoId) e obrigatorio.")
    private String alunoId;

    @NotBlank(message = "O nome da disciplina e obrigatorio.")
    @Size(min = 2, max = 120, message = "O nome da disciplina deve ter entre 2 e 120 caracteres.")
    private String nomeDisciplina;

    @NotBlank(message = "O semestre letivo e obrigatorio (ex.: 2026.1).")
    @Size(min = 4, max = 20, message = "O semestre letivo deve ter entre 4 e 20 caracteres.")
    private String semestreLetivo;

    @NotNull(message = "O total de aulas do semestre e obrigatorio.")
    @Min(value = 1, message = "O total de aulas do semestre deve ser no minimo 1.")
    private Integer totalAulasSemestre;

    @NotNull(message = "A quantidade de faltas e obrigatoria.")
    @Min(value = 0, message = "A quantidade de faltas nao pode ser negativa.")
    private Integer quantidadeFaltas;

    private Double percentualFrequencia;

    // Etapas da faculdade:
    // Etapa 1: máx 25 pts (15 prova + 5 sala + 5 interdisciplinar)
    @DecimalMin(value = "0.0", message = "A nota da Etapa 1 deve ser no minimo 0.0.")
    @DecimalMax(value = "25.0", message = "A nota da Etapa 1 nao pode exceder 25.0 pontos.")
    private Double notaEtapa1;

    // Etapa 2: máx 30 pts (15 prova + 10 interdisciplinar + 5 sala)
    @DecimalMin(value = "0.0", message = "A nota da Etapa 2 deve ser no minimo 0.0.")
    @DecimalMax(value = "30.0", message = "A nota da Etapa 2 nao pode exceder 30.0 pontos.")
    private Double notaEtapa2;

    // Etapa 3: máx 45 pts (25 prova + 10 sala + 10 prova F)
    @DecimalMin(value = "0.0", message = "A nota da Etapa 3 deve ser no minimo 0.0.")
    @DecimalMax(value = "45.0", message = "A nota da Etapa 3 nao pode exceder 45.0 pontos.")
    private Double notaEtapa3;

    // Total de pontos acumulados
    private Double mediaAtual;

    // Pontos restantes para atingir a meta (60 pts)
    private Double notaNecessariaParaPassar;

    private Integer faltasRestantesPermitidas;

    private StatusSituacao statusSituacao;

    public DisciplinaDesempenhoDTO() {
    }

    public DisciplinaDesempenhoDTO(String id, String alunoId, String nomeDisciplina, String semestreLetivo,
                                   Integer totalAulasSemestre, Integer quantidadeFaltas, Double percentualFrequencia,
                                   Double notaEtapa1, Double notaEtapa2, Double notaEtapa3, Double mediaAtual,
                                   Double notaNecessariaParaPassar, Integer faltasRestantesPermitidas, StatusSituacao statusSituacao) {
        this.id = id;
        this.alunoId = alunoId;
        this.nomeDisciplina = nomeDisciplina;
        this.semestreLetivo = semestreLetivo;
        this.totalAulasSemestre = totalAulasSemestre;
        this.quantidadeFaltas = quantidadeFaltas;
        this.percentualFrequencia = percentualFrequencia;
        this.notaEtapa1 = notaEtapa1;
        this.notaEtapa2 = notaEtapa2;
        this.notaEtapa3 = notaEtapa3;
        this.mediaAtual = mediaAtual;
        this.notaNecessariaParaPassar = notaNecessariaParaPassar;
        this.faltasRestantesPermitidas = faltasRestantesPermitidas;
        this.statusSituacao = statusSituacao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(String alunoId) {
        this.alunoId = alunoId;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public String getSemestreLetivo() {
        return semestreLetivo;
    }

    public void setSemestreLetivo(String semestreLetivo) {
        this.semestreLetivo = semestreLetivo;
    }

    public Integer getTotalAulasSemestre() {
        return totalAulasSemestre;
    }

    public void setTotalAulasSemestre(Integer totalAulasSemestre) {
        this.totalAulasSemestre = totalAulasSemestre;
    }

    public Integer getQuantidadeFaltas() {
        return quantidadeFaltas;
    }

    public void setQuantidadeFaltas(Integer quantidadeFaltas) {
        this.quantidadeFaltas = quantidadeFaltas;
    }

    public Double getPercentualFrequencia() {
        return percentualFrequencia;
    }

    public void setPercentualFrequencia(Double percentualFrequencia) {
        this.percentualFrequencia = percentualFrequencia;
    }

    public Double getNotaEtapa1() {
        return notaEtapa1;
    }

    public void setNotaEtapa1(Double notaEtapa1) {
        this.notaEtapa1 = notaEtapa1;
    }

    public Double getNotaEtapa2() {
        return notaEtapa2;
    }

    public void setNotaEtapa2(Double notaEtapa2) {
        this.notaEtapa2 = notaEtapa2;
    }

    public Double getNotaEtapa3() {
        return notaEtapa3;
    }

    public void setNotaEtapa3(Double notaEtapa3) {
        this.notaEtapa3 = notaEtapa3;
    }

    // Compatibilidade com nota1 e nota2 se chamado
    public Double getNota1() {
        return notaEtapa1;
    }

    public void setNota1(Double nota1) {
        this.notaEtapa1 = nota1;
    }

    public Double getNota2() {
        return notaEtapa2;
    }

    public void setNota2(Double nota2) {
        this.notaEtapa2 = nota2;
    }

    public Double getMediaAtual() {
        return mediaAtual;
    }

    public void setMediaAtual(Double mediaAtual) {
        this.mediaAtual = mediaAtual;
    }

    public Double getNotaNecessariaParaPassar() {
        return notaNecessariaParaPassar;
    }

    public void setNotaNecessariaParaPassar(Double notaNecessariaParaPassar) {
        this.notaNecessariaParaPassar = notaNecessariaParaPassar;
    }

    public Integer getFaltasRestantesPermitidas() {
        return faltasRestantesPermitidas;
    }

    public void setFaltasRestantesPermitidas(Integer faltasRestantesPermitidas) {
        this.faltasRestantesPermitidas = faltasRestantesPermitidas;
    }

    public StatusSituacao getStatusSituacao() {
        return statusSituacao;
    }

    public void setStatusSituacao(StatusSituacao statusSituacao) {
        this.statusSituacao = statusSituacao;
    }
}
