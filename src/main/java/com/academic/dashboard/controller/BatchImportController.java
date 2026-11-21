package com.academic.dashboard.controller;

import com.academic.dashboard.batch.BatchImportService;
import com.academic.dashboard.batch.dto.BatchImportReportDTO;
import com.academic.dashboard.validation.InputSecurityValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * Importacao em lote do boletim (CSV) via Spring Batch.
 * Base: /api/v1/importacoes
 */
@RestController
@RequestMapping("/api/v1/importacoes")
public class BatchImportController {

    /** Cabecalho esperado pelo leitor do Spring Batch (CsvReaderConfig). */
    static final String MODELO_CSV = """
            nomeDisciplina,semestreLetivo,totalAulasSemestre,quantidadeFaltas,notaEtapa1,notaEtapa2,notaEtapa3
            Estrutura de Dados,2026.1,80,6,22.5,26.0,38.0
            Banco de Dados,2026.1,60,12,18.0,22.0,
            """;

    private final BatchImportService batchImportService;

    public BatchImportController(BatchImportService batchImportService) {
        this.batchImportService = batchImportService;
    }

    /**
     * Recebe o CSV no campo multipart "arquivo", executa o job e devolve o relatorio
     * (lidos, importados, rejeitados) junto com o dashboard recalculado.
     */
    @PostMapping(value = "/boletim/{alunoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BatchImportReportDTO> importarBoletim(
            @PathVariable String alunoId,
            @RequestParam("arquivo") MultipartFile arquivo) {

        InputSecurityValidator.validarIdentificador(alunoId, "identificador do aluno");
        InputSecurityValidator.validarArquivoCsv(arquivo);
        return ResponseEntity.ok(batchImportService.executarImportacao(alunoId.trim(), arquivo));
    }

    /** Download de um CSV modelo para o usuario preencher. */
    @GetMapping(value = "/boletim/modelo", produces = "text/csv")
    public ResponseEntity<byte[]> baixarModelo() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"modelo-boletim.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(MODELO_CSV.getBytes(StandardCharsets.UTF_8));
    }
}
