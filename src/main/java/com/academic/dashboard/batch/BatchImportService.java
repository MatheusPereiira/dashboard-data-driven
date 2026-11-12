package com.academic.dashboard.batch;

import com.academic.dashboard.batch.dto.BatchImportReportDTO;
import com.academic.dashboard.dto.DashboardPerformanceDTO;
import com.academic.dashboard.exception.BusinessValidationException;
import com.academic.dashboard.model.Aluno;
import com.academic.dashboard.service.AlunoService;
import com.academic.dashboard.service.AnalisePreditivaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BatchImportService {

    private static final Logger log = LoggerFactory.getLogger(BatchImportService.class);

    private final JobLauncher jobLauncher;
    private final Job importacaoBoletimJob;
    private final AlunoService alunoService;
    private final AnalisePreditivaService analisePreditivaService;

    public BatchImportService(
            JobLauncher jobLauncher,
            Job importacaoBoletimJob,
            AlunoService alunoService,
            AnalisePreditivaService analisePreditivaService) {
        this.jobLauncher = jobLauncher;
        this.importacaoBoletimJob = importacaoBoletimJob;
        this.alunoService = alunoService;
        this.analisePreditivaService = analisePreditivaService;
    }

    public BatchImportReportDTO executarImportacao(String alunoId, MultipartFile file) {
        Aluno aluno = alunoService.obterEntidade(alunoId);

        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("O arquivo CSV nao pode estar vazio.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new BusinessValidationException("Apenas arquivos no formato CSV (.csv) sao aceitos.");
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("boletim_" + alunoId + "_", ".csv");
            file.transferTo(tempFile.toFile());

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("alunoId", aluno.getId())
                    .addString("filePath", tempFile.toAbsolutePath().toString())
                    .addString("runId", UUID.randomUUID().toString())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(importacaoBoletimJob, jobParameters);

            int totalLidos = 0;
            int totalGravados = 0;
            int totalPulados = 0;

            for (StepExecution stepExecution : execution.getStepExecutions()) {
                totalLidos += stepExecution.getReadCount();
                totalGravados += stepExecution.getWriteCount();
                totalPulados += stepExecution.getSkipCount();
            }

            // Recalcula a análise preditiva consolidada com as novas disciplinas inseridas
            analisePreditivaService.recalcular(aluno);
            DashboardPerformanceDTO dashboard = analisePreditivaService.montarDashboard(aluno);

            List<String> avisos = new ArrayList<>();
            if (totalPulados > 0) {
                avisos.add(String.format("%d linha(s) continham dados invalidos ou malformados e foram ignoradas.", totalPulados));
            }

            return new BatchImportReportDTO(
                    execution.getId() != null ? execution.getId().toString() : "N/A",
                    execution.getStatus().name(),
                    totalLidos,
                    totalGravados,
                    totalPulados,
                    avisos,
                    dashboard
            );

        } catch (Exception e) {
            log.error("Falha ao executar job de importacao para aluno {}", alunoId, e);
            throw new BusinessValidationException("Falha ao processar arquivo CSV do boletim: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }
}
