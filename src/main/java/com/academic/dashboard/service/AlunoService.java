package com.academic.dashboard.service;

import com.academic.dashboard.dto.AlunoDTO;
import com.academic.dashboard.dto.DashboardPerformanceDTO;
import com.academic.dashboard.exception.BusinessValidationException;
import com.academic.dashboard.exception.ResourceNotFoundException;
import com.academic.dashboard.mapper.AlunoMapper;
import com.academic.dashboard.model.Aluno;
import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.repository.AlunoRepository;
import com.academic.dashboard.repository.AnalisePreditivaRepository;
import com.academic.dashboard.repository.DisciplinaDesempenhoRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Gerencia o ciclo de vida do perfil discente e de seus parametros institucionais.
 */
@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final DisciplinaDesempenhoRepository disciplinaRepository;
    private final AnalisePreditivaRepository analiseRepository;
    private final CalculadoraDesempenho calculadora;
    private final AnalisePreditivaService analisePreditivaService;

    public AlunoService(AlunoRepository alunoRepository,
                        DisciplinaDesempenhoRepository disciplinaRepository,
                        AnalisePreditivaRepository analiseRepository,
                        CalculadoraDesempenho calculadora,
                        AnalisePreditivaService analisePreditivaService) {
        this.alunoRepository = alunoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.analiseRepository = analiseRepository;
        this.calculadora = calculadora;
        this.analisePreditivaService = analisePreditivaService;
    }

    /**
     * Cadastra o perfil discente e inicializa a analise preditiva vazia (sem dados pre-populados).
     */
    public AlunoDTO criar(AlunoDTO dto) {
        Aluno aluno = AlunoMapper.toEntity(dto);
        aluno.setId(null);
        validarParametros(aluno);

        if (alunoRepository.existsByMatricula(aluno.getMatricula())) {
            throw matriculaDuplicada(aluno.getMatricula());
        }

        Aluno salvo = salvar(aluno);
        analisePreditivaService.recalcular(salvo);
        return AlunoMapper.toDTO(salvo);
    }

    public AlunoDTO buscarPorId(String id) {
        return AlunoMapper.toDTO(obterEntidade(id));
    }

    /**
     * Atualiza o perfil. Alteracoes na media minima ou na frequencia minima exigida disparam o recalculo
     * de todas as disciplinas e da analise preditiva do aluno.
     */
    public AlunoDTO atualizar(String id, AlunoDTO dto) {
        Aluno existente = obterEntidade(id);
        Aluno dadosNovos = AlunoMapper.toEntity(dto);
        validarParametros(dadosNovos);

        if (!existente.getMatricula().equals(dadosNovos.getMatricula())) {
            alunoRepository.findByMatricula(dadosNovos.getMatricula())
                    .filter(outro -> !outro.getId().equals(existente.getId()))
                    .ifPresent(outro -> {
                        throw matriculaDuplicada(dadosNovos.getMatricula());
                    });
        }

        boolean parametrosAlterados =
                !Objects.equals(existente.getMediaMinimaAprovacao(), dadosNovos.getMediaMinimaAprovacao())
                        || !Objects.equals(existente.getFrequenciaMinimaExigida(), dadosNovos.getFrequenciaMinimaExigida());

        existente.setNome(dadosNovos.getNome());
        existente.setMatricula(dadosNovos.getMatricula());
        existente.setCurso(dadosNovos.getCurso());
        existente.setSemestreAtual(dadosNovos.getSemestreAtual());
        existente.setMediaMinimaAprovacao(dadosNovos.getMediaMinimaAprovacao());
        existente.setFrequenciaMinimaExigida(dadosNovos.getFrequenciaMinimaExigida());

        Aluno salvo = salvar(existente);
        if (parametrosAlterados) {
            recalcularDisciplinas(salvo);
            analisePreditivaService.recalcular(salvo);
        }
        return AlunoMapper.toDTO(salvo);
    }

    /**
     * Remove o aluno juntamente com suas disciplinas e analise preditiva.
     */
    public void excluir(String id) {
        Aluno aluno = obterEntidade(id);
        disciplinaRepository.deleteByAlunoId(aluno.getId());
        analiseRepository.deleteByAlunoId(aluno.getId());
        alunoRepository.delete(aluno);
    }

    public DashboardPerformanceDTO obterDashboard(String id) {
        return analisePreditivaService.montarDashboard(obterEntidade(id));
    }

    public Aluno obterEntidade(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O identificador do aluno e obrigatorio.");
        }
        return alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno", id));
    }

    private void recalcularDisciplinas(Aluno aluno) {
        List<DisciplinaDesempenho> disciplinas = disciplinaRepository.findByAlunoId(aluno.getId());
        if (disciplinas.isEmpty()) {
            return;
        }
        disciplinas.forEach(d -> calculadora.calcular(d, aluno));
        disciplinaRepository.saveAll(disciplinas);
    }

    private void validarParametros(Aluno aluno) {
        Double media = aluno.getMediaMinimaAprovacao();
        if (media == null || media <= 0.0 || media > CalculadoraDesempenho.NOTA_MAXIMA) {
            throw new BusinessValidationException("A media minima de aprovacao deve estar entre 0.0 (exclusivo) e 10.0.");
        }
        Double frequencia = aluno.getFrequenciaMinimaExigida();
        if (frequencia == null || frequencia < 0.0 || frequencia > 100.0) {
            throw new BusinessValidationException("A frequencia minima exigida deve estar entre 0.0% e 100.0%.");
        }
    }

    private Aluno salvar(Aluno aluno) {
        try {
            return alunoRepository.save(aluno);
        } catch (DuplicateKeyException ex) {
            throw matriculaDuplicada(aluno.getMatricula());
        }
    }

    private static BusinessValidationException matriculaDuplicada(String matricula) {
        return new BusinessValidationException("Ja existe um aluno cadastrado com a matricula " + matricula + ".");
    }
}

