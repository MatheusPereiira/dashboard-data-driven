package com.academic.dashboard.service;

import com.academic.dashboard.model.Aluno;
import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.model.StatusSituacao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Motor de calculo academico por disciplina (regras deterministicas, sem I/O).
 *
 * <p>Premissas adotadas:
 * <ul>
 *   <li>Duas avaliacoes com pesos iguais: media final = (nota1 + nota2) / 2.</li>
 *   <li>Enquanto a nota2 estiver pendente, a media atual corresponde a media das avaliacoes ja registradas (nota1)
 *       e a nota necessaria e a pontuacao minima na Avaliacao 2 para atingir a media minima da instituicao.</li>
 *   <li>Limite de faltas = piso(totalAulas x (100 - frequenciaMinima) / 100). Ultrapassar o limite reprova por frequencia.</li>
 * </ul>
 */
@Component
public class CalculadoraDesempenho {

    public static final double NOTA_MAXIMA = 10.0;
    public static final double FREQUENCIA_MINIMA_PADRAO = 75.0;

    /** Nota necessaria na avaliacao pendente a partir da qual a disciplina e considerada em risco critico. */
    static final double LIMIAR_NOTA_CRITICA = 8.0;
    /** Fracao do limite de faltas consumida a partir da qual a disciplina entra em atencao. */
    static final double LIMIAR_CONSUMO_FALTAS_ATENCAO = 0.75;
    /** Fracao do limite de faltas consumida a partir da qual a disciplina entra em risco critico. */
    static final double LIMIAR_CONSUMO_FALTAS_CRITICO = 0.90;

    private static final double EPSILON = 1e-9;

    /**
     * Calcula e preenche na entidade os indicadores derivados: percentual de frequencia, media atual,
     * nota necessaria para aprovacao, saldo de faltas e situacao.
     */
    public void calcular(DisciplinaDesempenho disciplina, Aluno aluno) {
        int totalAulas = disciplina.getTotalAulasSemestre();
        int faltas = disciplina.getQuantidadeFaltas();
        double mediaMinima = aluno.getMediaMinimaAprovacao();
        int limiteFaltas = calcularLimiteFaltas(totalAulas, frequenciaMinima(aluno));

        disciplina.setPercentualFrequencia(arredondar((totalAulas - faltas) * 100.0 / totalAulas));
        disciplina.setFaltasRestantesPermitidas(Math.max(0, limiteFaltas - faltas));

        Double nota1 = disciplina.getNota1();
        Double nota2 = disciplina.getNota2();
        if (nota1 != null && nota2 != null) {
            disciplina.setMediaAtual(arredondar((nota1 + nota2) / 2.0));
            disciplina.setNotaNecessariaParaPassar(null);
        } else if (nota1 != null) {
            disciplina.setMediaAtual(arredondar(nota1));
            disciplina.setNotaNecessariaParaPassar(arredondar(Math.max(0.0, 2 * mediaMinima - nota1)));
        } else {
            disciplina.setMediaAtual(null);
            disciplina.setNotaNecessariaParaPassar(arredondar(mediaMinima));
        }

        disciplina.setStatusSituacao(classificar(disciplina, mediaMinima, limiteFaltas));
    }

    /** Quantidade maxima de faltas permitidas antes da reprovacao por frequencia. */
    public int calcularLimiteFaltas(int totalAulas, double frequenciaMinimaExigida) {
        return (int) Math.floor(totalAulas * (100.0 - frequenciaMinimaExigida) / 100.0 + EPSILON);
    }

    public int calcularLimiteFaltas(DisciplinaDesempenho disciplina, Aluno aluno) {
        return calcularLimiteFaltas(disciplina.getTotalAulasSemestre(), frequenciaMinima(aluno));
    }

    /** Fracao (0..1+) do limite de faltas ja consumida. */
    public double consumoLimiteFaltas(int faltas, int limiteFaltas) {
        if (limiteFaltas <= 0) {
            return faltas > 0 ? 1.0 : 0.0;
        }
        return (double) faltas / limiteFaltas;
    }

    public boolean possuiAvaliacaoPendente(DisciplinaDesempenho disciplina) {
        return disciplina.getNota1() == null || disciplina.getNota2() == null;
    }

    public static boolean emAlerta(StatusSituacao status) {
        return status == StatusSituacao.ATENCAO_NOTA
                || status == StatusSituacao.ATENCAO_FALTA
                || status == StatusSituacao.RISCO_CRITICO
                || status == StatusSituacao.REPROVADO;
    }

    /**
     * Gera diagnosticos objetivos (alertas e metas) para uma disciplina ja calculada.
     */
    public List<String> gerarAlertas(DisciplinaDesempenho disciplina, Aluno aluno) {
        List<String> alertas = new ArrayList<>();
        String nome = disciplina.getNomeDisciplina();
        double mediaMinima = aluno.getMediaMinimaAprovacao();
        double frequenciaMinima = frequenciaMinima(aluno);
        int faltas = disciplina.getQuantidadeFaltas();
        int limiteFaltas = calcularLimiteFaltas(disciplina, aluno);
        double consumo = consumoLimiteFaltas(faltas, limiteFaltas);

        if (faltas > limiteFaltas) {
            alertas.add(String.format(Locale.ROOT,
                    "Reprovacao por frequencia em %s: %d faltas registradas, acima do limite de %d "
                            + "(frequencia de %.1f%%, minimo exigido de %.1f%%).",
                    nome, faltas, limiteFaltas, disciplina.getPercentualFrequencia(), frequenciaMinima));
        } else if (consumo >= LIMIAR_CONSUMO_FALTAS_ATENCAO) {
            alertas.add(String.format(Locale.ROOT,
                    "Atencao em %s: %.0f%% do limite de ausencias ja utilizado (restam %d faltas permitidas).",
                    nome, Math.min(consumo, 1.0) * 100, disciplina.getFaltasRestantesPermitidas()));
        }

        if (possuiAvaliacaoPendente(disciplina)) {
            Double necessaria = disciplina.getNotaNecessariaParaPassar();
            if (necessaria != null && necessaria > NOTA_MAXIMA) {
                alertas.add(String.format(Locale.ROOT,
                        "Em %s seria necessaria nota %.1f na Avaliacao 2, acima do maximo de %.1f: "
                                + "aprovacao por media inviavel.",
                        nome, necessaria, NOTA_MAXIMA));
            } else if (necessaria != null && necessaria > 0.0) {
                alertas.add(String.format(Locale.ROOT,
                        "Para atingir a media %.1f em %s, e necessario obter nota minima de %.1f na Avaliacao 2.",
                        mediaMinima, nome, necessaria));
            }
        } else if (disciplina.getMediaAtual() != null && disciplina.getMediaAtual() < mediaMinima - EPSILON) {
            alertas.add(String.format(Locale.ROOT,
                    "Reprovacao por nota em %s: media final %.1f abaixo da media minima de %.1f.",
                    nome, disciplina.getMediaAtual(), mediaMinima));
        }
        return alertas;
    }

    private StatusSituacao classificar(DisciplinaDesempenho disciplina, double mediaMinima, int limiteFaltas) {
        int faltas = disciplina.getQuantidadeFaltas();
        if (faltas > limiteFaltas) {
            return StatusSituacao.REPROVADO;
        }

        if (!possuiAvaliacaoPendente(disciplina)) {
            return disciplina.getMediaAtual() + EPSILON >= mediaMinima
                    ? StatusSituacao.APROVADO
                    : StatusSituacao.REPROVADO;
        }

        double necessaria = disciplina.getNotaNecessariaParaPassar();
        if (necessaria > NOTA_MAXIMA + EPSILON) {
            return StatusSituacao.REPROVADO;
        }

        double consumo = consumoLimiteFaltas(faltas, limiteFaltas);
        boolean atencaoNota = necessaria > mediaMinima + EPSILON;
        boolean criticoNota = necessaria >= LIMIAR_NOTA_CRITICA - EPSILON;
        boolean atencaoFalta = consumo >= LIMIAR_CONSUMO_FALTAS_ATENCAO;
        boolean criticoFalta = consumo >= LIMIAR_CONSUMO_FALTAS_CRITICO;

        if (criticoNota || criticoFalta || (atencaoNota && atencaoFalta)) {
            return StatusSituacao.RISCO_CRITICO;
        }
        if (atencaoNota) {
            return StatusSituacao.ATENCAO_NOTA;
        }
        if (atencaoFalta) {
            return StatusSituacao.ATENCAO_FALTA;
        }
        return StatusSituacao.REGULAR;
    }

    public static double frequenciaMinima(Aluno aluno) {
        return aluno.getFrequenciaMinimaExigida() != null
                ? aluno.getFrequenciaMinimaExigida()
                : FREQUENCIA_MINIMA_PADRAO;
    }

    public static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
