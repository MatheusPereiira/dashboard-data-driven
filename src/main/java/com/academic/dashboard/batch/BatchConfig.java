package com.academic.dashboard.batch;

import com.academic.dashboard.batch.dto.DisciplinaCsvRow;
import com.academic.dashboard.model.DisciplinaDesempenho;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    public static final String JOB_IMPORTACAO_BOLETIM = "importacaoBoletimJob";
    public static final String STEP_IMPORTACAO_BOLETIM = "importacaoBoletimStep";

    @Bean
    public JobRepository jobRepository() {
        return new ResourcelessJobRepository();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(new SyncTaskExecutor());
        launcher.afterPropertiesSet();
        return launcher;
    }

    @Bean
    public Job importacaoBoletimJob(JobRepository jobRepository, Step importacaoBoletimStep) {
        return new JobBuilder(JOB_IMPORTACAO_BOLETIM, jobRepository)
                .start(importacaoBoletimStep)
                .build();
    }

    @Bean
    public Step importacaoBoletimStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<DisciplinaCsvRow> csvItemReader,
            DisciplinaItemProcessor processor,
            DisciplinaItemWriter writer,
            CsvImportSkipListener skipListener) {

        return new StepBuilder(STEP_IMPORTACAO_BOLETIM, jobRepository)
                .<DisciplinaCsvRow, DisciplinaDesempenho>chunk(10, transactionManager)
                .reader(csvItemReader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skip(FlatFileParseException.class)
                .skipLimit(100)
                .listener(skipListener)
                .build();
    }
}
