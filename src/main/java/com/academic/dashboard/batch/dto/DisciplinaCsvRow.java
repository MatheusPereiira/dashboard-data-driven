package com.academic.dashboard.batch.dto;

public class DisciplinaCsvRow {

    private String nomeDisciplina;
    private String semestreLetivo;
    private Integer totalAulasSemestre;
    private Integer quantidadeFaltas;
    private Double notaEtapa1;
    private Double notaEtapa2;
    private Double notaEtapa3;

    public DisciplinaCsvRow() {
    }

    public DisciplinaCsvRow(String nomeDisciplina, String semestreLetivo, Integer totalAulasSemestre,
                            Integer quantidadeFaltas, Double notaEtapa1, Double notaEtapa2, Double notaEtapa3) {
        this.nomeDisciplina = nomeDisciplina;
        this.semestreLetivo = semestreLetivo;
        this.totalAulasSemestre = totalAulasSemestre;
        this.quantidadeFaltas = quantidadeFaltas;
        this.notaEtapa1 = notaEtapa1;
        this.notaEtapa2 = notaEtapa2;
        this.notaEtapa3 = notaEtapa3;
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

    // Compatibilidade reversa com chamadas legadas
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
}
