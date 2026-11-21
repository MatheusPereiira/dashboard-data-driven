package com.academic.dashboard.service;

import com.academic.dashboard.model.Aluno;
import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.model.StatusSituacao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Motor de cálculo acadêmico por disciplina adaptado às regras da FANS:
 * - 3 Etapas somando 100 pontos:
 *   - Etapa 1: máx 25 pts (15 prova + 5 sala + 5 interdisciplinar)
 *   - Etapa 2: máx 30 pts (15 prova + 5 sala + 10 interdisciplinar)
 *   - Etapa 3: máx 45 pts (25 prova + 10 sala + 10 prova F interdisciplinar)
 * - Meta de aprovação: soma das 3 etapas >= 60 pontos
 * - Assiduidade: limite máximo de 20% de faltas (mínimo 80% de presença)
 */
@Component
public class CalculadoraDesempenho {

    public static final double PONTUACAO_MAXIMA_TOTAL = 100.0;
    public static final double PONTOS_ETAPA_1 = 25.0;
    public static final double PONTOS_ETAPA_2 = 30.0;
    public static final double PONTOS_ETAPA_3 = 45.0;

    public static final double MEDIA_MINIMA_PADRAO = 60.0;
    public static final double FREQUENCIA_MINIMA_PADRAO = 80.0;

    /** Se o que falta para passar na última etapa for mais de 35 pontos (de 45), entra em risco crítico */
    static final double LIMIAR_PONTOS_CRITICOS = 35.0;
    static final double LIMIAR_CONSUMO_FALTAS_ATENCAO = 0.75;
    static final double LIMIAR_CONSUMO_FALTAS_CRITICO = 0.90;

    private static final double EPSILON = 1e-9;

    public void calcular(DisciplinaDesempenho disciplina, Aluno aluno) {
        int totalAulas = disciplina.getTotalAulasSemestre();
        int faltas = disciplina.getQuantidadeFaltas();
        double mediaMinima = normalizarMediaMinima(aluno);
        int limiteFaltas = calcularLimiteFaltas(totalAulas, frequenciaMinima(aluno));

        disciplina.setPercentualFrequencia(arredondar((totalAulas - faltas) * 100.0 / totalAulas));
        disciplina.setFaltasRestantesPermitidas(Math.max(0, limiteFaltas - faltas));

        Double e1 = disciplina.getNotaEtapa1();
        Double e2 = disciplina.getNotaEtapa2();
        Double e3 = disciplina.getNotaEtapa3();

        double pontosAcumulados = 0.0;
        int etapasLancadas = 0;
        double pontosMaximosRestantes = PONTUACAO_MAXIMA_TOTAL;

        if (e1 != null) {
            pontosAcumulados += e1;
            etapasLancadas++;
            pontosMaximosRestantes -= PONTOS_ETAPA_1;
        }
        if (e2 != null) {
            pontosAcumulados += e2;
            etapasLancadas++;
            pontosMaximosRestantes -= PONTOS_ETAPA_2;
        }
        if (e3 != null) {
            pontosAcumulados += e3;
            etapasLancadas++;
            pontosMaximosRestantes -= PONTOS_ETAPA_3;
        }

        if (etapasLancadas > 0) {
            disciplina.setMediaAtual(arredondar(pontosAcumulados));
        } else {
            disciplina.setMediaAtual(0.0);
        }

        double pontosNecessarios = Math.max(0.0, mediaMinima - pontosAcumulados);
        if (etapasLancadas == 3) {
            disciplina.setNotaNecessariaParaPassar(0.0);
        } else {
            disciplina.setNotaNecessariaParaPassar(arredondar(pontosNecessarios));
        }

        disciplina.setStatusSituacao(classificar(disciplina, mediaMinima, limiteFaltas, pontosMaximosRestantes, etapasLancadas));
    }

    public int calcularLimiteFaltas(int totalAulas, double frequenciaMinimaExigida) {
        return (int) Math.floor(totalAulas * (100.0 - frequenciaMinimaExigida) / 100.0 + EPSILON);
    }

    public int calcularLimiteFaltas(DisciplinaDesempenho disciplina, Aluno aluno) {
        return calcularLimiteFaltas(disciplina.getTotalAulasSemestre(), frequenciaMinima(aluno));
    }

    public double consumoLimiteFaltas(int faltas, int limiteFaltas) {
        if (limiteFaltas <= 0) {
            return faltas > 0 ? 1.0 : 0.0;
        }
        return (double) faltas / limiteFaltas;
    }

    public boolean possuiAvaliacaoPendente(DisciplinaDesempenho disciplina) {
        return disciplina.getNotaEtapa1() == null || disciplina.getNotaEtapa2() == null || disciplina.getNotaEtapa3() == null;
    }

    public static boolean emAlerta(StatusSituacao status) {
        return status == StatusSituacao.ATENCAO_NOTA
                || status == StatusSituacao.ATENCAO_FALTA
                || status == StatusSituacao.RISCO_CRITICO
                || status == StatusSituacao.REPROVADO;
    }

    public List<String> gerarAlertas(DisciplinaDesempenho disciplina, Aluno aluno) {
        List<String> alertas = new ArrayList<>();
        String nome = disciplina.getNomeDisciplina();
        double mediaMinima = normalizarMediaMinima(aluno);
        double frequenciaMinima = frequenciaMinima(aluno);
        int faltas = disciplina.getQuantidadeFaltas();
        int limiteFaltas = calcularLimiteFaltas(disciplina, aluno);
        double consumo = consumoLimiteFaltas(faltas, limiteFaltas);

        if (faltas > limiteFaltas) {
            alertas.add(String.format(Locale.ROOT,
                    "Reprovacao por frequencia em %s: %d faltas registradas, acima do limite de %d "
                            + "(presenca de %.1f%%, minimo exigido de %.1f%%).",
                    nome, faltas, limiteFaltas, disciplina.getPercentualFrequencia(), frequenciaMinima));
        } else if (consumo >= LIMIAR_CONSUMO_FALTAS_ATENCAO) {
            alertas.add(String.format(Locale.ROOT,
                    "Atencao em %s: %.0f%% do limite de faltas ja utilizado (restam %d faltas permitidas).",
                    nome, Math.min(consumo, 1.0) * 100, disciplina.getFaltasRestantesPermitidas()));
        }

        double pontosAcumulados = disciplina.getMediaAtual() != null ? disciplina.getMediaAtual() : 0.0;
        Double necessarios = disciplina.getNotaNecessariaParaPassar();

        if (possuiAvaliacaoPendente(disciplina)) {
            if (disciplina.getStatusSituacao() == StatusSituacao.REPROVADO) {
                alertas.add(String.format(Locale.ROOT,
                        "Em %s, mesmo alcancando pontuacao maxima nas etapas restantes, nao e possivel atingir %.0f pontos: reprovacao iminente.",
                        nome, mediaMinima));
            } else if (necessarios != null && necessarios > 0.0) {
                alertas.add(String.format(Locale.ROOT,
                        "Meta em %s: voce possui %.1f pontos acumulados. Faltam %.1f pontos para alcancar os %.0f pontos de aprovacao.",
                        nome, pontosAcumulados, necessarios, mediaMinima));
            } else {
                alertas.add(String.format(Locale.ROOT,
                        "Parabens! Em %s voce ja garantiu %.1f pontos, ultrapassando os %.0f pontos de aprovacao.",
                        nome, pontosAcumulados, mediaMinima));
            }
        } else {
            if (pontosAcumulados < mediaMinima - EPSILON) {
                alertas.add(String.format(Locale.ROOT,
                        "Reprovacao por nota em %s: pontuacao final de %.1f abaixo dos %.0f pontos exigidos.",
                        nome, pontosAcumulados, mediaMinima));
            } else {
                alertas.add(String.format(Locale.ROOT,
                        "Aprovado em %s com pontuacao final de %.1f pontos!",
                        nome, pontosAcumulados));
            }
        }
        return alertas;
    }

    private StatusSituacao classificar(DisciplinaDesempenho disciplina, double mediaMinima, int limiteFaltas,
                                       double pontosMaximosRestantes, int etapasLancadas) {
        int faltas = disciplina.getQuantidadeFaltas();
        if (faltas > limiteFaltas) {
            return StatusSituacao.REPROVADO;
        }

        double pontosAcumulados = disciplina.getMediaAtual() != null ? disciplina.getMediaAtual() : 0.0;

        // Se já atingiu 60 pontos mesmo com etapas restantes, já está garantido na nota
        if (pontosAcumulados >= mediaMinima - EPSILON && etapasLancadas == 3) {
            return StatusSituacao.APROVADO;
        }

        // Se mesmo tirando nota máxima no que resta não atinge os 60 pontos
        if (pontosAcumulados + pontosMaximosRestantes < mediaMinima - EPSILON) {
            return StatusSituacao.REPROVADO;
        }

        if (etapasLancadas == 3) {
            return pontosAcumulados >= mediaMinima - EPSILON ? StatusSituacao.APROVADO : StatusSituacao.REPROVADO;
        }

        double consumo = consumoLimiteFaltas(faltas, limiteFaltas);
        double pontosNecessarios = disciplina.getNotaNecessariaParaPassar() != null ? disciplina.getNotaNecessariaParaPassar() : 0.0;

        boolean atencaoNota = pontosNecessarios > (pontosMaximosRestantes * 0.60);
        boolean criticoNota = pontosNecessarios >= LIMIAR_PONTOS_CRITICOS || pontosNecessarios > (pontosMaximosRestantes * 0.85);
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

    public static double normalizarMediaMinima(Aluno aluno) {
        if (aluno == null || aluno.getMediaMinimaAprovacao() == null) {
            return MEDIA_MINIMA_PADRAO;
        }
        double val = aluno.getMediaMinimaAprovacao();
        // Se aluno tiver configurado como 6.0 (escala antiga 0-10), normaliza para 60.0
        return val <= 10.0 ? val * 10.0 : val;
    }

    public static double frequenciaMinima(Aluno aluno) {
        return aluno != null && aluno.getFrequenciaMinimaExigida() != null
                ? aluno.getFrequenciaMinimaExigida()
                : FREQUENCIA_MINIMA_PADRAO;
    }

    public static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
