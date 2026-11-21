package com.academic.dashboard.service;

import com.academic.dashboard.model.Aluno;
import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.model.StatusSituacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraDesempenhoTest {

    private CalculadoraDesempenho calculadora;
    private Aluno aluno;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraDesempenho();
        // Aluno no padrão FANS: média mínima 60.0 (de 100), frequência mínima 80.0% (máx 20% faltas)
        aluno = new Aluno("1", "Carlos Silva", "2024001", "Engenharia de Software", 3, 60.0, 80.0);
    }

    @Test
    @DisplayName("Deve calcular corretamente notas e frequencia com as 3 etapas aprovadas")
    void deveCalcularDisciplinaAprovada() {
        DisciplinaDesempenho disciplina = new DisciplinaDesempenho();
        disciplina.setNomeDisciplina("Calculo I");
        disciplina.setSemestreLetivo("2026.1");
        disciplina.setTotalAulasSemestre(80);
        disciplina.setQuantidadeFaltas(10);
        disciplina.setNotaEtapa1(20.0); // max 25
        disciplina.setNotaEtapa2(25.0); // max 30
        disciplina.setNotaEtapa3(35.0); // max 45 => total 80 pontos

        calculadora.calcular(disciplina, aluno);

        assertEquals(87.5, disciplina.getPercentualFrequencia());
        assertEquals(80.0, disciplina.getMediaAtual());
        assertEquals(0.0, disciplina.getNotaNecessariaParaPassar());
        assertEquals(6, disciplina.getFaltasRestantesPermitidas()); // 80 * 20% = 16 limite; 16 - 10 = 6
        assertEquals(StatusSituacao.APROVADO, disciplina.getStatusSituacao());
    }

    @Test
    @DisplayName("Deve calcular pontos necessarios quando Etapa 3 estiver pendente em atencao")
    void deveCalcularNotaNecessariaPendente() {
        DisciplinaDesempenho disciplina = new DisciplinaDesempenho();
        disciplina.setNomeDisciplina("Fisica I");
        disciplina.setSemestreLetivo("2026.1");
        disciplina.setTotalAulasSemestre(80);
        disciplina.setQuantidadeFaltas(5);
        disciplina.setNotaEtapa1(20.0); // max 25
        disciplina.setNotaEtapa2(12.0); // max 30 => acumula 32 pontos
        disciplina.setNotaEtapa3(null); // faltam 28 pontos de 45 possíveis

        calculadora.calcular(disciplina, aluno);

        assertEquals(32.0, disciplina.getMediaAtual());
        assertEquals(28.0, disciplina.getNotaNecessariaParaPassar()); // 60.0 - 32.0 = 28.0
        assertEquals(StatusSituacao.ATENCAO_NOTA, disciplina.getStatusSituacao());
    }

    @Test
    @DisplayName("Deve sinalizar risco critico quando a nota restante exigida for muito alta")
    void deveClassificarRiscoCriticoQuandoPontosNecessariosElevados() {
        DisciplinaDesempenho disciplina = new DisciplinaDesempenho();
        disciplina.setNomeDisciplina("Fisica II");
        disciplina.setSemestreLetivo("2026.1");
        disciplina.setTotalAulasSemestre(80);
        disciplina.setQuantidadeFaltas(5);
        disciplina.setNotaEtapa1(10.0); // de 25
        disciplina.setNotaEtapa2(null);
        disciplina.setNotaEtapa3(null);

        calculadora.calcular(disciplina, aluno);

        assertEquals(10.0, disciplina.getMediaAtual());
        assertEquals(50.0, disciplina.getNotaNecessariaParaPassar()); // precisa de 50 de 75
        assertEquals(StatusSituacao.RISCO_CRITICO, disciplina.getStatusSituacao());
    }

    @Test
    @DisplayName("Deve reprovar por faltas se faltas ultrapassarem o limite institucional de 20%")
    void deveReprovarPorFalta() {
        DisciplinaDesempenho disciplina = new DisciplinaDesempenho();
        disciplina.setNomeDisciplina("Algebra Linear");
        disciplina.setSemestreLetivo("2026.1");
        disciplina.setTotalAulasSemestre(80);
        disciplina.setQuantidadeFaltas(20); // limite é 16 (80% presenca exigida)
        disciplina.setNotaEtapa1(25.0);
        disciplina.setNotaEtapa2(30.0);
        disciplina.setNotaEtapa3(45.0);

        calculadora.calcular(disciplina, aluno);

        assertEquals(StatusSituacao.REPROVADO, disciplina.getStatusSituacao());
        assertEquals(0, disciplina.getFaltasRestantesPermitidas());
    }
}
