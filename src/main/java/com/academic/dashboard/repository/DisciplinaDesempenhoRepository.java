package com.academic.dashboard.repository;

import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.model.StatusSituacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisciplinaDesempenhoRepository extends MongoRepository<DisciplinaDesempenho, String> {

    List<DisciplinaDesempenho> findByAlunoId(String alunoId);

    List<DisciplinaDesempenho> findByAlunoIdAndSemestreLetivo(String alunoId, String semestreLetivo);

    List<DisciplinaDesempenho> findByAlunoIdAndStatusSituacao(String alunoId, StatusSituacao statusSituacao);

    List<DisciplinaDesempenho> findByAlunoIdAndSemestreLetivoAndStatusSituacao(
            String alunoId,
            String semestreLetivo,
            StatusSituacao statusSituacao
    );

    List<DisciplinaDesempenho> findByAlunoIdAndNomeDisciplinaIgnoreCaseAndSemestreLetivo(
            String alunoId,
            String nomeDisciplina,
            String semestreLetivo
    );

    boolean existsByAlunoIdAndNomeDisciplinaIgnoreCaseAndSemestreLetivo(
            String alunoId,
            String nomeDisciplina,
            String semestreLetivo
    );

    void deleteByAlunoId(String alunoId);
}

