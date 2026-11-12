package com.academic.dashboard.service.preditivo;

import java.util.List;

/**
 * Resposta do motor preditivo em Python.
 *
 * @param scoreRisco         probabilidade estimada de reprovacao/evasao, de 0 a 100
 * @param classificacaoRisco BAIXO, MODERADO, ALTO ou CRITICO
 * @param recomendacoes      recomendacoes acionaveis geradas pelo modelo
 * @param modelo             identificacao/versao do modelo que produziu a predicao
 */
public record PredicaoResposta(
        Double scoreRisco,
        String classificacaoRisco,
        List<String> recomendacoes,
        String modelo
) {
}

