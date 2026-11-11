package com.academic.dashboard.dto;

import java.util.ArrayList;
import java.util.List;

public class DashboardPerformanceDTO {

    private AlunoDTO aluno;
    private List<DisciplinaDesempenhoDTO> disciplinas = new ArrayList<>();
    private AnalisePreditivaDTO analisePreditiva;
    private Integer totalDisciplinasMatriculadas;
    private Integer totalDisciplinasEmAlerta;

    public DashboardPerformanceDTO() {
    }

    public DashboardPerformanceDTO(AlunoDTO aluno,
                                   List<DisciplinaDesempenhoDTO> disciplinas,
                                   AnalisePreditivaDTO analisePreditiva,
                                   Integer totalDisciplinasMatriculadas,
                                   Integer totalDisciplinasEmAlerta) {
        this.aluno = aluno;
        this.disciplinas = disciplinas != null ? disciplinas : new ArrayList<>();
        this.analisePreditiva = analisePreditiva;
        this.totalDisciplinasMatriculadas = totalDisciplinasMatriculadas;
        this.totalDisciplinasEmAlerta = totalDisciplinasEmAlerta;
    }

    public AlunoDTO getAluno() {
        return aluno;
    }

    public void setAluno(AlunoDTO aluno) {
        this.aluno = aluno;
    }

    public List<DisciplinaDesempenhoDTO> getDisciplinas() {
        return disciplinas;
    }

    public void setDisciplinas(List<DisciplinaDesempenhoDTO> disciplinas) {
        this.disciplinas = disciplinas != null ? disciplinas : new ArrayList<>();
    }

    public AnalisePreditivaDTO getAnalisePreditiva() {
        return analisePreditiva;
    }

    public void setAnalisePreditiva(AnalisePreditivaDTO analisePreditiva) {
        this.analisePreditiva = analisePreditiva;
    }

    public Integer getTotalDisciplinasMatriculadas() {
        return totalDisciplinasMatriculadas;
    }

    public void setTotalDisciplinasMatriculadas(Integer totalDisciplinasMatriculadas) {
        this.totalDisciplinasMatriculadas = totalDisciplinasMatriculadas;
    }

    public Integer getTotalDisciplinasEmAlerta() {
        return totalDisciplinasEmAlerta;
    }

    public void setTotalDisciplinasEmAlerta(Integer totalDisciplinasEmAlerta) {
        this.totalDisciplinasEmAlerta = totalDisciplinasEmAlerta;
    }
}

