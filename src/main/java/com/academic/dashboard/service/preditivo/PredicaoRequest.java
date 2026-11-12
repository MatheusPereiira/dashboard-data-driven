package com.academic.dashboard.service.preditivo;

import java.util.List;

/**
 * Payload enviado ao motor preditivo em Python ({@code POST app.predictive.python-service-url}).
 */
public record PredicaoRequest(
        String alunoId,
        double mediaMinimaAprovacao,
        double frequenciaMinimaExigida,
        List<DisciplinaFeature> disciplinas
) {

    /**
     * Vetor de caracteristicas (notas e frequencia) de uma disciplina, ja calculado pelo backend Java.
     */
    public record DisciplinaFeature(
            String nomeDisciplina,
            Double nota1,
            Double nota2,
            Double mediaAtual,
            Double notaNecessariaParaPassar,
            int quantidadeFaltas,
            int totalAulasSemestre,
            int limiteFaltas,
            int faltasRestantesPermitidas,
            double percentualFrequencia,
            String statusSituacao
    ) {
    }
}

