package com.academic.dashboard.service;

import com.academic.dashboard.dto.DashboardPerformanceDTO;
import com.academic.dashboard.dto.DisciplinaDesempenhoDTO;
import com.academic.dashboard.mapper.AlunoMapper;
import com.academic.dashboard.mapper.AnaliseMapper;
import com.academic.dashboard.mapper.DisciplinaMapper;
import com.academic.dashboard.model.Aluno;
import com.academic.dashboard.model.AnalisePreditiva;
import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.repository.AnalisePreditivaRepository;
import com.academic.dashboard.repository.DisciplinaDesempenhoRepository;
import com.academic.dashboard.service.preditivo.MotorPreditivoService;
import com.academic.dashboard.service.preditivo.ResultadoPredicao;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Consolida a {@link AnalisePreditiva} do aluno e monta o Dashboard de Desempenho.
 *
 * <p>Indice de rendimento (IRA) e frequencia global consideram todo o historico registrado.
 * O score de risco e os alertas consideram o semestre letivo mais recente, evitando que reprovacoes
 * de semestres ja encerrados mantenham o painel permanentemente em estado critico.
 */
@Service
public class AnalisePreditivaService {

    static final String ALERTA_SEM_DISCIPLINAS =
            "Nenhuma disciplina registrada. Cadastre suas disciplinas ou importe o boletim CSV para gerar a analise.";

    private static final Comparator<DisciplinaDesempenho> ORDEM_DISCIPLINAS = Comparator
            .comparing(DisciplinaDesempenho::getSemestreLetivo, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(DisciplinaDesempenho::getNomeDisciplina, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

    private final DisciplinaDesempenhoRepository disciplinaRepository;
    private final AnalisePreditivaRepository analiseRepository;
    private final CalculadoraDesempenho calculadora;
    private final MotorPreditivoService motorPreditivoService;

    public AnalisePreditivaService(DisciplinaDesempenhoRepository disciplinaRepository,
                                   AnalisePreditivaRepository analiseRepository,
                                   CalculadoraDesempenho calculadora,
                                   MotorPreditivoService motorPreditivoService) {
        this.disciplinaRepository = disciplinaRepository;
        this.analiseRepository = analiseRepository;
        this.calculadora = calculadora;
        this.motorPreditivoService = motorPreditivoService;
    }

    /**
     * Recalcula e persiste a analise preditiva do aluno a partir das disciplinas armazenadas.
     */
    public AnalisePreditiva recalcular(Aluno aluno) {
        List<DisciplinaDesempenho> disciplinas = disciplinaRepository.findByAlunoId(aluno.getId());

        AnalisePreditiva analise = analiseRepository.findByAlunoId(aluno.getId()).orElseGet(AnalisePreditiva::new);
        analise.setAlunoId(aluno.getId());
        analise.setDataAtualizacao(LocalDateTime.now());

        if (disciplinas.isEmpty()) {
            analise.setIndiceRendimentoAcademico(null);
            analise.setFrequenciaGlobal(null);
            ResultadoPredicao vazio = motorPreditivoService.avaliar(aluno, List.of());
            analise.setScoreRiscoEvasaoReprovacao(vazio.scoreRisco());
            analise.setClassificacaoRisco(vazio.classificacaoRisco());
            analise.setAlertasPreditivos(new ArrayList<>(List.of(ALERTA_SEM_DISCIPLINAS)));
            return analiseRepository.save(analise);
        }

        analise.setIndiceRendimentoAcademico(calcularIndiceRendimento(disciplinas));
        analise.setFrequenciaGlobal(calcularFrequenciaGlobal(disciplinas));

        List<DisciplinaDesempenho> semestreCorrente = filtrarSemestreMaisRecente(disciplinas);
        ResultadoPredicao predicao = motorPreditivoService.avaliar(aluno, semestreCorrente);
        analise.setScoreRiscoEvasaoReprovacao(predicao.scoreRisco());
        analise.setClassificacaoRisco(predicao.classificacaoRisco());

        Set<String> alertas = new LinkedHashSet<>();
        semestreCorrente.stream()
                .sorted(ORDEM_DISCIPLINAS)
                .forEach(d -> alertas.addAll(calculadora.gerarAlertas(d, aluno)));
        alertas.addAll(predicao.recomendacoes());
        analise.setAlertasPreditivos(new ArrayList<>(alertas));

        return analiseRepository.save(analise);
    }

    /**
     * Monta o dashboard consolidado. Se ainda nao houver analise persistida, ela e gerada.
     */
    public DashboardPerformanceDTO montarDashboard(Aluno aluno) {
        List<DisciplinaDesempenho> disciplinas = new ArrayList<>(disciplinaRepository.findByAlunoId(aluno.getId()));
        disciplinas.sort(ORDEM_DISCIPLINAS);

        AnalisePreditiva analise = analiseRepository.findByAlunoId(aluno.getId())
                .orElseGet(() -> recalcular(aluno));

        List<DisciplinaDesempenhoDTO> disciplinasDTO = DisciplinaMapper.toDTOList(disciplinas);
        int emAlerta = (int) filtrarSemestreMaisRecente(disciplinas).stream()
                .filter(d -> CalculadoraDesempenho.emAlerta(d.getStatusSituacao()))
                .count();

        return new DashboardPerformanceDTO(
                AlunoMapper.toDTO(aluno),
                disciplinasDTO,
                AnaliseMapper.toDTO(analise),
                disciplinas.size(),
                emAlerta);
    }

    static Double calcularIndiceRendimento(List<DisciplinaDesempenho> disciplinas) {
        OptionalDouble media = disciplinas.stream()
                .map(DisciplinaDesempenho::getMediaAtual)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();
        return media.isPresent() ? CalculadoraDesempenho.arredondar(media.getAsDouble()) : null;
    }

    /** Frequencia global ponderada pela carga horaria: total de presencas / total de aulas. */
    static Double calcularFrequenciaGlobal(List<DisciplinaDesempenho> disciplinas) {
        long totalAulas = 0;
        long totalFaltas = 0;
        for (DisciplinaDesempenho d : disciplinas) {
            totalAulas += d.getTotalAulasSemestre();
            totalFaltas += d.getQuantidadeFaltas();
        }
        if (totalAulas <= 0) {
            return null;
        }
        return CalculadoraDesempenho.arredondar((totalAulas - totalFaltas) * 100.0 / totalAulas);
    }

    static List<DisciplinaDesempenho> filtrarSemestreMaisRecente(List<DisciplinaDesempenho> disciplinas) {
        String semestreMaisRecente = disciplinas.stream()
                .map(DisciplinaDesempenho::getSemestreLetivo)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
        if (semestreMaisRecente == null) {
            return disciplinas;
        }
        return disciplinas.stream()
                .filter(d -> semestreMaisRecente.equals(d.getSemestreLetivo()))
                .toList();
    }
}
