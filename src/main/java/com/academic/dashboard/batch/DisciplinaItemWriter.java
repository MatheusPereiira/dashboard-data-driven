package com.academic.dashboard.batch;

import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.repository.DisciplinaDesempenhoRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
public class DisciplinaItemWriter implements ItemWriter<DisciplinaDesempenho> {

    private final DisciplinaDesempenhoRepository repository;

    public DisciplinaItemWriter(DisciplinaDesempenhoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends DisciplinaDesempenho> chunk) {
        for (DisciplinaDesempenho item : chunk) {
            // Verifica duplicidade no mesmo semestre para o mesmo aluno e atualiza ou insere (Upsert)
            List<DisciplinaDesempenho> existentes = repository
                    .findByAlunoIdAndNomeDisciplinaIgnoreCaseAndSemestreLetivo(
                            item.getAlunoId(),
                            item.getNomeDisciplina(),
                            item.getSemestreLetivo()
                    );

            if (!existentes.isEmpty()) {
                DisciplinaDesempenho existente = existentes.getFirst();
                existente.setTotalAulasSemestre(item.getTotalAulasSemestre());
                existente.setQuantidadeFaltas(item.getQuantidadeFaltas());
                existente.setNotaEtapa1(item.getNotaEtapa1());
                existente.setNotaEtapa2(item.getNotaEtapa2());
                existente.setNotaEtapa3(item.getNotaEtapa3());
                existente.setPercentualFrequencia(item.getPercentualFrequencia());
                existente.setMediaAtual(item.getMediaAtual());
                existente.setNotaNecessariaParaPassar(item.getNotaNecessariaParaPassar());
                existente.setFaltasRestantesPermitidas(item.getFaltasRestantesPermitidas());
                existente.setStatusSituacao(item.getStatusSituacao());
                repository.save(existente);
            } else {
                repository.save(item);
            }
        }
    }
}

