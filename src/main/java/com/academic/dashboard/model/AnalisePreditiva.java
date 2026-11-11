package com.academic.dashboard.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "analises_preditivas")
public class AnalisePreditiva {

    @Id
    private String id;

    @Indexed(unique = true)
    private String alunoId;

    private LocalDateTime dataAtualizacao;

    private Double indiceRendimentoAcademico;

    private Double frequenciaGlobal;

    private Double scoreRiscoEvasaoReprovacao;

    private ClassificacaoRisco classificacaoRisco;

    private List<String> alertasPreditivos = new ArrayList<>();

    public AnalisePreditiva() {
    }

    public AnalisePreditiva(String id, String alunoId, LocalDateTime dataAtualizacao,
                            Double indiceRendimentoAcademico, Double frequenciaGlobal,
                            Double scoreRiscoEvasaoReprovacao, ClassificacaoRisco classificacaoRisco,
                            List<String> alertasPreditivos) {
        this.id = id;
        this.alunoId = alunoId;
        this.dataAtualizacao = dataAtualizacao;
        this.indiceRendimentoAcademico = indiceRendimentoAcademico;
        this.frequenciaGlobal = frequenciaGlobal;
        this.scoreRiscoEvasaoReprovacao = scoreRiscoEvasaoReprovacao;
        this.classificacaoRisco = classificacaoRisco;
        this.alertasPreditivos = alertasPreditivos != null ? alertasPreditivos : new ArrayList<>();
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

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Double getIndiceRendimentoAcademico() {
        return indiceRendimentoAcademico;
    }

    public void setIndiceRendimentoAcademico(Double indiceRendimentoAcademico) {
        this.indiceRendimentoAcademico = indiceRendimentoAcademico;
    }

    public Double getFrequenciaGlobal() {
        return frequenciaGlobal;
    }

    public void setFrequenciaGlobal(Double frequenciaGlobal) {
        this.frequenciaGlobal = frequenciaGlobal;
    }

    public Double getScoreRiscoEvasaoReprovacao() {
        return scoreRiscoEvasaoReprovacao;
    }

    public void setScoreRiscoEvasaoReprovacao(Double scoreRiscoEvasaoReprovacao) {
        this.scoreRiscoEvasaoReprovacao = scoreRiscoEvasaoReprovacao;
    }

    public ClassificacaoRisco getClassificacaoRisco() {
        return classificacaoRisco;
    }

    public void setClassificacaoRisco(ClassificacaoRisco classificacaoRisco) {
        this.classificacaoRisco = classificacaoRisco;
    }

    public List<String> getAlertasPreditivos() {
        return alertasPreditivos;
    }

    public void setAlertasPreditivos(List<String> alertasPreditivos) {
        this.alertasPreditivos = alertasPreditivos != null ? alertasPreditivos : new ArrayList<>();
    }
}

