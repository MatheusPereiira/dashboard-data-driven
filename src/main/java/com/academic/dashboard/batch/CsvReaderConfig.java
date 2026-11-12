package com.academic.dashboard.batch;

import com.academic.dashboard.batch.dto.DisciplinaCsvRow;
import com.academic.dashboard.model.DisciplinaDesempenho;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Configuration
public class CsvReaderConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<DisciplinaCsvRow> csvItemReader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("nomeDisciplina", "semestreLetivo", "totalAulasSemestre", "quantidadeFaltas", "nota1", "nota2");
        tokenizer.setStrict(false);

        DefaultLineMapper<DisciplinaCsvRow> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new DisciplinaFieldSetMapper());

        return new FlatFileItemReaderBuilder<DisciplinaCsvRow>()
                .name("disciplinaCsvItemReader")
                .resource(new FileSystemResource(new File(filePath)))
                .linesToSkip(1) // Ignora cabeçalho
                .lineMapper(lineMapper)
                .strict(false)
                .build();
    }

    private static class DisciplinaFieldSetMapper implements FieldSetMapper<DisciplinaCsvRow> {
        @Override
        public DisciplinaCsvRow mapFieldSet(FieldSet fieldSet) {
            DisciplinaCsvRow row = new DisciplinaCsvRow();
            row.setNomeDisciplina(fieldSet.readString("nomeDisciplina"));
            row.setSemestreLetivo(fieldSet.readString("semestreLetivo"));
            row.setTotalAulasSemestre(parseInteger(fieldSet.readString("totalAulasSemestre")));
            row.setQuantidadeFaltas(parseInteger(fieldSet.readString("quantidadeFaltas")));
            row.setNota1(parseDouble(fieldSet.readString("nota1")));

            String nota2Str = fieldSet.readString("nota2");
            row.setNota2(parseDouble(nota2Str));

            return row;
        }

        private Integer parseInteger(String val) {
            if (val == null || val.trim().isEmpty()) {
                return null;
            }
            try {
                return Integer.valueOf(val.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private Double parseDouble(String val) {
            if (val == null || val.trim().isEmpty()) {
                return null;
            }
            try {
                return Double.valueOf(val.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}

