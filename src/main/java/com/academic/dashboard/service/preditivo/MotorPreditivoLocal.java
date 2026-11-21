package com.academic.dashboard.service.preditivo;

import com.academic.dashboard.model.ClassificacaoRisco;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class MotorPreditivoLocal {

    public ResultadoPredicao avaliar(PredicaoRequest request) {
        if (request.disciplinas().isEmpty()) {
            return new ResultadoPredicao(0.0, ClassificacaoRisco.BAIXO, List.of(), "local-rules");
        }

        double mediaCorte = request.mediaMinimaAprovacao();
        double freqCorte = request.frequenciaMinimaExigida();

        double somaRisco = 0.0;
        double piorRisco = 0.0;
        int materiasEmAlerta = 0;
        List<String> recomendacoes = new ArrayList<>();

        for (PredicaoRequest.DisciplinaFeature d : request.disciplinas()) {
            double risco = calcularRisco(d, mediaCorte, freqCorte);
            somaRisco += risco;
            if (risco > piorRisco) {
                piorRisco = risco;
            }
            if (risco >= 50.0) {
                materiasEmAlerta++;
            }

            if (d.notaNecessariaParaPassar() != null && d.notaNecessariaParaPassar() > 0.0 && risco >= 40.0) {
                recomendacoes.add(String.format(Locale.ROOT,
                        "Foco na disciplina %s: faltam %.1f pontos para a aprovacao. Intensifique o estudo para a proxima etapa.",
                        d.nomeDisciplina(), d.notaNecessariaParaPassar()));
            }
            if (d.faltasRestantesPermitidas() <= 3 && d.limiteFaltas() > 0) {
                recomendacoes.add(String.format(Locale.ROOT,
                        "Atencao as presencas em %s: voce possui apenas %d falta(s) antes do limite de reprovacao por frequencia.",
                        d.nomeDisciplina(), d.faltasRestantesPermitidas()));
            }
        }

        double mediaRisco = somaRisco / request.disciplinas().size();
        double penalidadeCascata = Math.min(15.0, Math.max(0, materiasEmAlerta - 1) * 6.0);
        double scoreFinal = Math.min(100.0, Math.max(0.0, (0.55 * piorRisco) + (0.45 * mediaRisco) + penalidadeCascata));
        scoreFinal = Math.round(scoreFinal * 100.0) / 100.0;

        ClassificacaoRisco classificacao;
        if (scoreFinal >= 75.0) {
            classificacao = ClassificacaoRisco.CRITICO;
        } else if (scoreFinal >= 50.0) {
            classificacao = ClassificacaoRisco.ALTO;
        } else if (scoreFinal >= 25.0) {
            classificacao = ClassificacaoRisco.MODERADO;
        } else {
            classificacao = ClassificacaoRisco.BAIXO;
        }

        return new ResultadoPredicao(scoreFinal, classificacao, recomendacoes, "local-rules");
    }

    private double calcularRisco(PredicaoRequest.DisciplinaFeature d, double mediaCorte, double freqCorte) {
        String status = d.statusSituacao() != null ? d.statusSituacao().toUpperCase() : "";
        if ("REPROVADO".equals(status)) {
            return 98.0;
        }
        if ("APROVADO".equals(status)) {
            return 2.0;
        }

        double pontosAcumulados = d.mediaAtual() != null ? d.mediaAtual() : 0.0;
        double pontosNecessarios = d.notaNecessariaParaPassar() != null ? d.notaNecessariaParaPassar() : 0.0;

        double fatorNota;
        if (pontosNecessarios <= 0.0) {
            fatorNota = 0.0;
        } else {
            fatorNota = Math.min(1.0, pontosNecessarios / 45.0);
        }

        double consumoFaltas = d.limiteFaltas() > 0 ? (double) d.quantidadeFaltas() / d.limiteFaltas() : 0.0;
        if (d.percentualFrequencia() < freqCorte) {
            return 98.0;
        }

        double risco = (fatorNota * 60.0) + (consumoFaltas * 40.0);
        if ("RISCO_CRITICO".equals(status)) {
            risco = Math.max(risco, 78.0);
        }
        return Math.min(100.0, Math.max(0.0, risco));
    }
}
