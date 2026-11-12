package com.academic.dashboard.service.preditivo;

import com.academic.dashboard.model.ClassificacaoRisco;
import com.academic.dashboard.model.StatusSituacao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Modelo heuristico de risco executado localmente (contingencia quando o servico Python esta indisponivel).
 *
 * <p>Implementa exatamente o mesmo algoritmo do servico {@code predictive-service/app/modelo_risco.py},
 * garantindo resultados equivalentes nos dois caminhos:
 * <ul>
 *   <li>Risco por nota (avaliacao pendente) = (notaNecessaria / 10)^2; disciplina concluida e aprovada = 0.</li>
 *   <li>Risco por frequencia = (faltas / limiteFaltas)^2, limitado a 1.</li>
 *   <li>Risco da disciplina = 1 - (1 - riscoNota) x (1 - riscoFalta); disciplina reprovada = 1.</li>
 *   <li>Score global = 100 x (0.6 x maior risco + 0.4 x risco medio).</li>
 * </ul>
 */
@Component
public class MotorPreditivoLocal {

    public static final String MODELO = "heuristico-v1";

    private static final double PESO_MAIOR_RISCO = 0.6;
    private static final double PESO_RISCO_MEDIO = 0.4;
    private static final double LIMIAR_PRIORIZACAO = 0.5;

    public ResultadoPredicao avaliar(PredicaoRequest request) {
        List<PredicaoRequest.DisciplinaFeature> disciplinas = request.disciplinas();
        if (disciplinas == null || disciplinas.isEmpty()) {
            return new ResultadoPredicao(0.0, ClassificacaoRisco.BAIXO, List.of(), "local");
        }

        double maiorRisco = 0.0;
        double somaRiscos = 0.0;
        PredicaoRequest.DisciplinaFeature disciplinaMaisCritica = null;
        for (PredicaoRequest.DisciplinaFeature disciplina : disciplinas) {
            double risco = riscoDisciplina(disciplina);
            somaRiscos += risco;
            if (disciplinaMaisCritica == null || risco > maiorRisco) {
                maiorRisco = risco;
                disciplinaMaisCritica = disciplina;
            }
        }
        double riscoMedio = somaRiscos / disciplinas.size();
        double score = arredondar(100.0 * (PESO_MAIOR_RISCO * maiorRisco + PESO_RISCO_MEDIO * riscoMedio));
        ClassificacaoRisco classificacao = classificar(score);

        List<String> recomendacoes = new ArrayList<>();
        if (maiorRisco >= LIMIAR_PRIORIZACAO
                && !StatusSituacao.REPROVADO.name().equals(disciplinaMaisCritica.statusSituacao())) {
            recomendacoes.add(String.format(Locale.ROOT,
                    "Priorize %s: maior risco individual de reprovacao (%d%%).",
                    disciplinaMaisCritica.nomeDisciplina(), Math.round(maiorRisco * 100)));
        }
        if (classificacao == ClassificacaoRisco.ALTO || classificacao == ClassificacaoRisco.CRITICO) {
            recomendacoes.add("Risco academico " + classificacao.name()
                    + ": revise o plano de estudos e procure apoio da coordenacao ou monitoria.");
        }
        return new ResultadoPredicao(score, classificacao, recomendacoes, "local");
    }

    static double riscoDisciplina(PredicaoRequest.DisciplinaFeature disciplina) {
        if (StatusSituacao.REPROVADO.name().equals(disciplina.statusSituacao())) {
            return 1.0;
        }
        boolean pendente = disciplina.nota1() == null || disciplina.nota2() == null;
        double riscoNota = 0.0;
        if (pendente && disciplina.notaNecessariaParaPassar() != null) {
            riscoNota = Math.pow(limitar(disciplina.notaNecessariaParaPassar() / 10.0), 2);
        }
        double riscoFalta;
        if (disciplina.limiteFaltas() <= 0) {
            riscoFalta = disciplina.quantidadeFaltas() > 0 ? 1.0 : 0.0;
        } else {
            riscoFalta = Math.pow(limitar((double) disciplina.quantidadeFaltas() / disciplina.limiteFaltas()), 2);
        }
        return 1.0 - (1.0 - riscoNota) * (1.0 - riscoFalta);
    }

    public static ClassificacaoRisco classificar(double score) {
        if (score < 25.0) {
            return ClassificacaoRisco.BAIXO;
        }
        if (score < 50.0) {
            return ClassificacaoRisco.MODERADO;
        }
        if (score < 75.0) {
            return ClassificacaoRisco.ALTO;
        }
        return ClassificacaoRisco.CRITICO;
    }

    private static double limitar(double valor) {
        return Math.max(0.0, Math.min(1.0, valor));
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
