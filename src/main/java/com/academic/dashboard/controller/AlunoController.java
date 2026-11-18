package com.academic.dashboard.controller;

import com.academic.dashboard.dto.AlunoDTO;
import com.academic.dashboard.dto.DashboardPerformanceDTO;
import com.academic.dashboard.service.AlunoService;
import com.academic.dashboard.validation.InputSecurityValidator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints do perfil discente e do dashboard consolidado.
 * Base: /api/v1/alunos
 */
@RestController
@RequestMapping("/api/v1/alunos")
public class AlunoController {

    private static final String ROTULO_ID = "identificador do aluno";

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    /** Cadastra o perfil do estudante. Retorna 201 com header Location. */
    @PostMapping
    public ResponseEntity<AlunoDTO> criar(@Valid @RequestBody AlunoDTO dto) {
        InputSecurityValidator.sanitizarAlunoDTO(dto);
        AlunoDTO criado = alunoService.criar(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    /** Lista os perfis cadastrados (usado pela tela de selecao de aluno). */
    @GetMapping
    public ResponseEntity<List<AlunoDTO>> listar() {
        return ResponseEntity.ok(alunoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlunoDTO> buscar(@PathVariable String id) {
        InputSecurityValidator.validarIdentificador(id, ROTULO_ID);
        return ResponseEntity.ok(alunoService.buscarPorId(id.trim()));
    }

    /** Atualiza o perfil. Mudancas de media/frequencia minima recalculam todas as disciplinas. */
    @PutMapping("/{id}")
    public ResponseEntity<AlunoDTO> atualizar(@PathVariable String id, @Valid @RequestBody AlunoDTO dto) {
        InputSecurityValidator.validarIdentificador(id, ROTULO_ID);
        InputSecurityValidator.sanitizarAlunoDTO(dto);
        return ResponseEntity.ok(alunoService.atualizar(id.trim(), dto));
    }

    /** Remove o aluno, suas disciplinas e a analise preditiva. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        InputSecurityValidator.validarIdentificador(id, ROTULO_ID);
        alunoService.excluir(id.trim());
        return ResponseEntity.noContent().build();
    }

    /** Dashboard completo: perfil, disciplinas, KPIs e analise preditiva. */
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<DashboardPerformanceDTO> dashboard(@PathVariable String id) {
        InputSecurityValidator.validarIdentificador(id, ROTULO_ID);
        return ResponseEntity.ok(alunoService.obterDashboard(id.trim()));
    }
}
