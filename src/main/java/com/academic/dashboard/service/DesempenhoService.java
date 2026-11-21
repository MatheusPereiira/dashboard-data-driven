package com.academic.dashboard.service;

import com.academic.dashboard.dto.DashboardPerformanceDTO;
import com.academic.dashboard.dto.DisciplinaDesempenhoDTO;
import com.academic.dashboard.exception.BusinessValidationException;
import com.academic.dashboard.exception.ResourceNotFoundException;
import com.academic.dashboard.mapper.DisciplinaMapper;
import com.academic.dashboard.model.Aluno;
import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.model.StatusSituacao;
import com.academic.dashboard.repository.DisciplinaDesempenhoRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Regras de negocio do desempenho por disciplina: validacao dos dados informados, calculo automatico
 * dos indicadores e orquestracao do recalculo da analise preditiva a cada escrita ou exclusao.
 */
@Service
public class DesempenhoService {

    private static final Comparator<DisciplinaDesempenho> ORDEM = Comparator
            .comparing(DisciplinaDesempenho::getSemestreLetivo, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(DisciplinaDesempenho::getNomeDisciplina, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

    private final DisciplinaDesempenhoRepository disciplinaRepository;
    private final AlunoService alunoService;
    private final CalculadoraDesempenho calculadora;
    private final AnalisePreditivaService analisePreditivaService;

    public DesempenhoService(DisciplinaDesempenhoRepository disciplinaRepository,
                             AlunoService alunoService,
                             CalculadoraDesempenho calculadora,
                             AnalisePreditivaService analisePreditivaService) {
        this.disciplinaRepository = disciplinaRepository;
        this.alunoService = alunoService;
        this.calculadora = calculadora;
        this.analisePreditivaService = analisePreditivaService;
    }

    /**
     * Registra uma nova disciplina, calcula seus indicadores e retorna o dashboard atualizado.
     */
    public DashboardPerformanceDTO registrar(DisciplinaDesempenhoDTO dto) {
        Aluno aluno = alunoService.obterEntidade(dto.getAlunoId());
        DisciplinaDesempenho disciplina = DisciplinaMapper.toEntity(dto);
        disciplina.setId(null);
        disciplina.setAlunoId(aluno.getId());

        validarDados(disciplina);
        validarDuplicidade(disciplina, null);

        calculadora.calcular(disciplina, aluno);
        disciplinaRepository.save(disciplina);
        return recalcularERetornarDashboard(aluno);
    }

    /**
     * Atualiza integralmente os dados informados da disciplina (nome, semestre, carga horaria, faltas e notas).
     * Uma {@code nota2} nula indica avaliacao ainda pendente.
     */
    public DashboardPerformanceDTO atualizar(String id, DisciplinaDesempenhoDTO dto) {
        DisciplinaDesempenho existente = obterEntidade(id);
        if (dto.getAlunoId() != null && !dto.getAlunoId().trim().equals(existente.getAlunoId())) {
            throw new BusinessValidationException("Nao e permitido transferir a disciplina para outro aluno.");
        }
        Aluno aluno = alunoService.obterEntidade(existente.getAlunoId());

        DisciplinaDesempenho dadosNovos = DisciplinaMapper.toEntity(dto);
        existente.setNomeDisciplina(dadosNovos.getNomeDisciplina());
        existente.setSemestreLetivo(dadosNovos.getSemestreLetivo());
        existente.setTotalAulasSemestre(dadosNovos.getTotalAulasSemestre());
        existente.setQuantidadeFaltas(dadosNovos.getQuantidadeFaltas());
        existente.setNotaEtapa1(dadosNovos.getNotaEtapa1());
        existente.setNotaEtapa2(dadosNovos.getNotaEtapa2());
        existente.setNotaEtapa3(dadosNovos.getNotaEtapa3());

        validarDados(existente);
        validarDuplicidade(existente, existente.getId());

        calculadora.calcular(existente, aluno);
        disciplinaRepository.save(existente);
        return recalcularERetornarDashboard(aluno);
    }

    /**
     * Exclui a disciplina e reprocessa imediatamente a analise preditiva.
     */
    public DashboardPerformanceDTO excluir(String id) {
        DisciplinaDesempenho existente = obterEntidade(id);
        Aluno aluno = alunoService.obterEntidade(existente.getAlunoId());
        disciplinaRepository.delete(existente);
        return recalcularERetornarDashboard(aluno);
    }

    public DisciplinaDesempenhoDTO buscarPorId(String id) {
        return DisciplinaMapper.toDTO(obterEntidade(id));
    }

    /**
     * Lista as disciplinas do aluno com filtros opcionais por semestre letivo e situacao.
     */
    public List<DisciplinaDesempenhoDTO> listarPorAluno(String alunoId, String semestreLetivo, StatusSituacao status) {
        Aluno aluno = alunoService.obterEntidade(alunoId);
        String semestre = semestreLetivo != null && !semestreLetivo.isBlank() ? semestreLetivo.trim() : null;

        List<DisciplinaDesempenho> disciplinas;
        if (semestre != null && status != null) {
            disciplinas = disciplinaRepository.findByAlunoIdAndSemestreLetivoAndStatusSituacao(aluno.getId(), semestre, status);
        } else if (semestre != null) {
            disciplinas = disciplinaRepository.findByAlunoIdAndSemestreLetivo(aluno.getId(), semestre);
        } else if (status != null) {
            disciplinas = disciplinaRepository.findByAlunoIdAndStatusSituacao(aluno.getId(), status);
        } else {
            disciplinas = disciplinaRepository.findByAlunoId(aluno.getId());
        }
        return DisciplinaMapper.toDTOList(disciplinas.stream().sorted(ORDEM).toList());
    }

    public DisciplinaDesempenho obterEntidade(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O identificador da disciplina e obrigatorio.");
        }
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", id));
    }

    /**
     * Invariantes de dominio dos dados informados pelo aluno (reaplicadas no servico, independente do controller).
     */
    public static void validarDados(DisciplinaDesempenho disciplina) {
        if (disciplina.getNomeDisciplina() == null || disciplina.getNomeDisciplina().isBlank()) {
            throw new BusinessValidationException("O nome da disciplina e obrigatorio.");
        }
        if (disciplina.getSemestreLetivo() == null || disciplina.getSemestreLetivo().isBlank()) {
            throw new BusinessValidationException("O semestre letivo e obrigatorio (ex.: 2026.1).");
        }
        Integer totalAulas = disciplina.getTotalAulasSemestre();
        if (totalAulas == null || totalAulas <= 0) {
            throw new BusinessValidationException("O total de aulas do semestre deve ser maior que zero.");
        }
        Integer faltas = disciplina.getQuantidadeFaltas();
        if (faltas == null || faltas < 0) {
            throw new BusinessValidationException("A quantidade de faltas nao pode ser negativa.");
        }
        if (faltas > totalAulas) {
            throw new BusinessValidationException(String.format(
                    "A quantidade de faltas (%d) nao pode ser superior ao total de aulas do semestre (%d).",
                    faltas, totalAulas));
        }
        if (disciplina.getNotaEtapa1() != null) {
            validarNota(disciplina.getNotaEtapa1(), CalculadoraDesempenho.PONTOS_ETAPA_1, "Etapa 1");
        }
        if (disciplina.getNotaEtapa2() != null) {
            validarNota(disciplina.getNotaEtapa2(), CalculadoraDesempenho.PONTOS_ETAPA_2, "Etapa 2");
        }
        if (disciplina.getNotaEtapa3() != null) {
            validarNota(disciplina.getNotaEtapa3(), CalculadoraDesempenho.PONTOS_ETAPA_3, "Etapa 3");
        }
    }

    private static void validarNota(double nota, double maximo, String rotulo) {
        if (Double.isNaN(nota) || nota < 0.0 || nota > maximo) {
            throw new BusinessValidationException(String.format(
                    "A nota da %s deve estar entre 0.0 e %.1f pontos.", rotulo, maximo));
        }
    }

    private void validarDuplicidade(DisciplinaDesempenho disciplina, String idIgnorado) {
        boolean duplicada = disciplinaRepository
                .findByAlunoIdAndNomeDisciplinaIgnoreCaseAndSemestreLetivo(
                        disciplina.getAlunoId(), disciplina.getNomeDisciplina(), disciplina.getSemestreLetivo())
                .stream()
                .anyMatch(outra -> !outra.getId().equals(idIgnorado));
        if (duplicada) {
            throw new BusinessValidationException(String.format(
                    "A disciplina %s ja esta cadastrada no semestre %s para este aluno.",
                    disciplina.getNomeDisciplina(), disciplina.getSemestreLetivo()));
        }
    }

    private DashboardPerformanceDTO recalcularERetornarDashboard(Aluno aluno) {
        analisePreditivaService.recalcular(aluno);
        return analisePreditivaService.montarDashboard(aluno);
    }
}

