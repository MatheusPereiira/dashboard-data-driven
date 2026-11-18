package com.academic.dashboard.controller;

import com.academic.dashboard.dto.DashboardPerformanceDTO;
import com.academic.dashboard.dto.DisciplinaDesempenhoDTO;
import com.academic.dashboard.model.StatusSituacao;
import com.academic.dashboard.service.DesempenhoService;
import com.academic.dashboard.validation.InputSecurityValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lancamento e gestao de notas/faltas por disciplina.
 * Toda operacao de escrita devolve o dashboard ja recalculado, evitando uma segunda chamada do frontend.
 * Base: /api/v1/disciplinas
 */
@RestController
@RequestMapping("/api/v1/disciplinas")
public class DesempenhoController {

    private static final String ROTULO_DISCIPLINA = "identificador da disciplina";
    private static final String ROTULO_ALUNO = "identificador do aluno";

    private final DesempenhoService desempenhoService;

    public DesempenhoController(DesempenhoService desempenhoService) {
        this.desempenhoService = desempenhoService;
    }

    @PostMapping
    public ResponseEntity<DashboardPerformanceDTO> registrar(@Valid @RequestBody DisciplinaDesempenhoDTO dto) {
        InputSecurityValidator.sanitizarDisciplinaDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(desempenhoService.registrar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisciplinaDesempenhoDTO> buscar(@PathVariable String id) {
        InputSecurityValidator.validarIdentificador(id, ROTULO_DISCIPLINA);
        return ResponseEntity.ok(desempenhoService.buscarPorId(id.trim()));
    }

    /**
     * Lista as disciplinas do aluno. Filtros opcionais:
     * ?semestreLetivo=2026.1 e/ou ?statusSituacao=ATENCAO_FALTA
     */
    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<DisciplinaDesempenhoDTO>> listarPorAluno(
            @PathVariable String alunoId,
            @RequestParam(required = false) String semestreLetivo,
            @RequestParam(required = false) StatusSituacao statusSituacao) {

        InputSecurityValidator.validarIdentificador(alunoId, ROTULO_ALUNO);
        String semestre = InputSecurityValidator.sanitizarParametroFiltro(semestreLetivo, "semestreLetivo");
        return ResponseEntity.ok(desempenhoService.listarPorAluno(alunoId.trim(), semestre, statusSituacao));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DashboardPerformanceDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody DisciplinaDesempenhoDTO dto) {

        InputSecurityValidator.validarIdentificador(id, ROTULO_DISCIPLINA);
        InputSecurityValidator.sanitizarDisciplinaDTO(dto);
        return ResponseEntity.ok(desempenhoService.atualizar(id.trim(), dto));
    }

    /** Exclui a disciplina e devolve o dashboard recalculado (200). */
    @DeleteMapping("/{id}")
    public ResponseEntity<DashboardPerformanceDTO> excluir(@PathVariable String id) {
        InputSecurityValidator.validarIdentificador(id, ROTULO_DISCIPLINA);
        return ResponseEntity.ok(desempenhoService.excluir(id.trim()));
    }
}
