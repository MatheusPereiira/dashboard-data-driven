package com.academic.dashboard.batch;

import com.academic.dashboard.batch.dto.DisciplinaCsvRow;
import com.academic.dashboard.exception.BusinessValidationException;
import com.academic.dashboard.model.Aluno;
import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.repository.AlunoRepository;
import com.academic.dashboard.service.CalculadoraDesempenho;
import com.academic.dashboard.service.DesempenhoService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class DisciplinaItemProcessor implements ItemProcessor<DisciplinaCsvRow, DisciplinaDesempenho> {

    private final AlunoRepository alunoRepository;
    private final CalculadoraDesempenho calculadora;
    private final String alunoId;

    public DisciplinaItemProcessor(
            AlunoRepository alunoRepository,
            CalculadoraDesempenho calculadora,
            @Value("#{jobParameters['alunoId']}") String alunoId) {
        this.alunoRepository = alunoRepository;
        this.calculadora = calculadora;
        this.alunoId = alunoId;
    }

    @Override
    public DisciplinaDesempenho process(DisciplinaCsvRow row) {
        if (row == null) {
            return null;
        }

        // Sanitização contra CSV Injection / Formula Injection (=, +, -, @)
        String nomeDisciplina = sanitize(row.getNomeDisciplina());
        String semestreLetivo = sanitize(row.getSemestreLetivo());

        if (nomeDisciplina == null || nomeDisciplina.isBlank()) {
            throw new BusinessValidationException("Nome da disciplina e obrigatorio no CSV.");
        }

        if (semestreLetivo == null || semestreLetivo.isBlank()) {
            throw new BusinessValidationException("Semestre letivo e obrigatorio no CSV.");
        }

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new BusinessValidationException("Aluno nao encontrado: " + alunoId));

        DisciplinaDesempenho entidade = new DisciplinaDesempenho();
        entidade.setAlunoId(aluno.getId());
        entidade.setNomeDisciplina(nomeDisciplina);
        entidade.setSemestreLetivo(semestreLetivo);
        entidade.setTotalAulasSemestre(row.getTotalAulasSemestre());
        entidade.setQuantidadeFaltas(row.getQuantidadeFaltas());
        entidade.setNota1(row.getNota1());
        entidade.setNota2(row.getNota2());

        // Valida invariantes do domínio
        DesempenhoService.validarDados(entidade);

        // Realiza os cálculos analíticos de frequência, médias e projeções
        calculadora.calcular(entidade, aluno);

        return entidade;
    }

    private String sanitize(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        // Remove aspas adicionais se houverem
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        // Formula injection protection: remove prefixos perigosos se detectados
        while (!trimmed.isEmpty() && (trimmed.startsWith("=") || trimmed.startsWith("+") || trimmed.startsWith("-") || trimmed.startsWith("@"))) {
            trimmed = trimmed.substring(1).trim();
        }
        return trimmed;
    }
}

