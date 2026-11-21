package com.academic.dashboard.batch;

import com.academic.dashboard.batch.dto.DisciplinaCsvRow;
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
        tokenizer.setNames("nomeDisciplina", "semestreLetivo", "totalAulasSemestre", "quantidadeFaltas", "notaEtapa1", "notaEtapa2", "notaEtapa3");
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

            // Etapa 1
            String e1 = null;
            if (fieldSet.hasNames()) {
                try { e1 = fieldSet.readString("notaEtapa1"); } catch (Exception ignored) {}
                if (e1 == null) {
                    try { e1 = fieldSet.readString("nota1"); } catch (Exception ignored) {}
                }
            }
            if (e1 == null && fieldSet.getFieldCount() > 4) {
                e1 = fieldSet.readString(4);
            }
            row.setNotaEtapa1(parseDouble(e1));

            // Etapa 2
            String e2 = null;
            if (fieldSet.hasNames()) {
                try { e2 = fieldSet.readString("notaEtapa2"); } catch (Exception ignored) {}
                if (e2 == null) {
                    try { e2 = fieldSet.readString("nota2"); } catch (Exception ignored) {}
                }
            }
            if (e2 == null && fieldSet.getFieldCount() > 5) {
                e2 = fieldSet.readString(5);
            }
            row.setNotaEtapa2(parseDouble(e2));

            // Etapa 3
            String e3 = null;
            if (fieldSet.hasNames()) {
                try { e3 = fieldSet.readString("notaEtapa3"); } catch (Exception ignored) {}
            }
            if (e3 == null && fieldSet.getFieldCount() > 6) {
                e3 = fieldSet.readString(6);
            }
            row.setNotaEtapa3(parseDouble(e3));

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
