package com.academic.dashboard.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "disciplinas_desempenho")
public class DisciplinaDesempenho {

    @Id
    private String id;

    @Indexed
    private String alunoId;

    private String nomeDisciplina;

    private String semestreLetivo;

    private Integer totalAulasSemestre;

    private Integer quantidadeFaltas;

    private Double percentualFrequencia;

    private Double nota1;

    private Double nota2;

    private Double mediaAtual;

    private Double notaNecessariaParaPassar;

    private Integer faltasRestantesPermitidas;

    private StatusSituacao statusSituacao;

    public DisciplinaDesempenho() {
    }

    public DisciplinaDesempenho(String id, String alunoId, String nomeDisciplina, String semestreLetivo,
                                Integer totalAulasSemestre, Integer quantidadeFaltas, Double percentualFrequencia,
                                Double nota1, Double nota2, Double mediaAtual, Double notaNecessariaParaPassar,
                                Integer faltasRestantesPermitidas, StatusSituacao statusSituacao) {
        this.id = id;
        this.alunoId = alunoId;
        this.nomeDisciplina = nomeDisciplina;
        this.semestreLetivo = semestreLetivo;
        this.totalAulasSemestre = totalAulasSemestre;
        this.quantidadeFaltas = quantidadeFaltas;
        this.percentualFrequencia = percentualFrequencia;
        this.nota1 = nota1;
        this.nota2 = nota2;
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

    public Double getNota1() {
        return nota1;
    }

    public void setNota1(Double nota1) {
        this.nota1 = nota1;
    }

    public Double getNota2() {
        return nota2;
    }

    public void setNota2(Double nota2) {
        this.nota2 = nota2;
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

