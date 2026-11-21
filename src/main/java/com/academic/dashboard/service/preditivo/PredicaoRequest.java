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
     * Vetor de caracteristicas (etapas e frequencia) de uma disciplina.
     */
    public record DisciplinaFeature(
            String nomeDisciplina,
            Double notaEtapa1,
            Double notaEtapa2,
            Double notaEtapa3,
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
