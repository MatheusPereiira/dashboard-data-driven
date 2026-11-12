package com.academic.dashboard.service.preditivo;

import com.academic.dashboard.model.ClassificacaoRisco;

import java.util.List;

/**
 * Resultado consolidado da avaliacao de risco, independente da origem (Python ou modelo local).
 *
 * @param origem "python" quando calculado pelo servico externo, "local" quando pelo modelo de contingencia
 */
public record ResultadoPredicao(
        double scoreRisco,
        ClassificacaoRisco classificacaoRisco,
        List<String> recomendacoes,
        String origem
) {
}

