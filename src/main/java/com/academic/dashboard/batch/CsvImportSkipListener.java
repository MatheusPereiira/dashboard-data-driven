package com.academic.dashboard.batch;

import com.academic.dashboard.batch.dto.DisciplinaCsvRow;
import com.academic.dashboard.model.DisciplinaDesempenho;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class CsvImportSkipListener implements SkipListener<DisciplinaCsvRow, DisciplinaDesempenho> {

    private static final Logger log = LoggerFactory.getLogger(CsvImportSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Linha do CSV ignorada na leitura devido a erro: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(DisciplinaCsvRow item, Throwable t) {
        log.warn("Item ignorado no processamento [disciplina: {}, semestre: {}]: {}",
                item != null ? item.getNomeDisciplina() : "desconhecido",
                item != null ? item.getSemestreLetivo() : "desconhecido",
                t.getMessage());
    }

    @Override
    public void onSkipInWrite(DisciplinaDesempenho item, Throwable t) {
        log.warn("Item ignorado na escrita [disciplina: {}]: {}",
                item != null ? item.getNomeDisciplina() : "desconhecido",
                t.getMessage());
    }
}
