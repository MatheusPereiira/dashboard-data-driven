package com.academic.dashboard.service;

import com.academic.dashboard.model.Aluno;
import com.academic.dashboard.model.ClassificacaoRisco;
import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.model.StatusSituacao;
import com.academic.dashboard.service.preditivo.MotorPreditivoLocal;
import com.academic.dashboard.service.preditivo.PredicaoRequest;
import com.academic.dashboard.service.preditivo.ResultadoPredicao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraDesempenhoTest {

    private CalculadoraDesempenho calculadora;
    private Aluno aluno;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraDesempenho();
        aluno = new Aluno("1", "Carlos Silva", "2024001", "Engenharia de Software", 3, 6.0, 75.0);
    }

    @Test
    @DisplayName("Deve calcular corretamente notas e frequencia com duas notas aprovadas")
    void deveCalcularDisciplinaAprovada() {
        DisciplinaDesempenho disciplina = new DisciplinaDesempenho();
        disciplina.setNomeDisciplina("Calculo I");
        disciplina.setSemestreLetivo("2026.1");
        disciplina.setTotalAulasSemestre(80);
        disciplina.setQuantidadeFaltas(10);
        disciplina.setNota1(7.0);
        disciplina.setNota2(8.0);

        calculadora.calcular(disciplina, aluno);

        assertEquals(87.5, disciplina.getPercentualFrequencia());
        assertEquals(7.5, disciplina.getMediaAtual());
        assertNull(disciplina.getNotaNecessariaParaPassar());
        assertEquals(10, disciplina.getFaltasRestantesPermitidas()); // limite = 20, 20 - 10 = 10
        assertEquals(StatusSituacao.APROVADO, disciplina.getStatusSituacao());
    }

    @Test
    @DisplayName("Deve calcular nota necessaria quando Nota 2 estiver pendente")
    void deveCalcularNotaNecessariaPendente() {
        DisciplinaDesempenho disciplina = new DisciplinaDesempenho();
        disciplina.setNomeDisciplina("Fisica I");
        disciplina.setSemestreLetivo("2026.1");
        disciplina.setTotalAulasSemestre(80);
        disciplina.setQuantidadeFaltas(5);
        disciplina.setNota1(5.0);
        disciplina.setNota2(null);

        calculadora.calcular(disciplina, aluno);

        assertEquals(5.0, disciplina.getMediaAtual());
        assertEquals(7.0, disciplina.getNotaNecessariaParaPassar()); // 2 * 6.0 - 5.0 = 7.0
        assertEquals(StatusSituacao.ATENCAO_NOTA, disciplina.getStatusSituacao());
    }

    @Test
    @DisplayName("Deve reprovar por faltas se faltas ultrapassarem o limite")
    void deveReprovarPorFalta() {
        DisciplinaDesempenho disciplina = new DisciplinaDesempenho();
        disciplina.setNomeDisciplina("Algebra Linear");
        disciplina.setSemestreLetivo("2026.1");
        disciplina.setTotalAulasSemestre(80);
        disciplina.setQuantidadeFaltas(25); // limite é 20 (75%)
        disciplina.setNota1(9.0);
        disciplina.setNota2(9.0);

        calculadora.calcular(disciplina, aluno);

        assertEquals(StatusSituacao.REPROVADO, disciplina.getStatusSituacao());
        assertEquals(0, disciplina.getFaltasRestantesPermitidas());
    }
}

