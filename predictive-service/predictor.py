import math
from schemas import ClassificacaoRisco, DisciplinaFeature, PredicaoRequest, PredicaoResposta

MODELO_VERSAO = "logistic-academic-risk-v1"


def _sigmoid(x: float) -> float:
    return 1.0 / (1.0 + math.exp(-x))


def _classificar_risco(score: float) -> ClassificacaoRisco:
    if score >= 75.0:
        return "CRITICO"
    if score >= 50.0:
        return "ALTO"
    if score >= 25.0:
        return "MODERADO"
    return "BAIXO"


def _calcular_risco_disciplina(
    disc: DisciplinaFeature,
    media_minima: float,
    frequencia_minima: float,
) -> float:
    status = (disc.statusSituacao or "").strip().upper()
    if status == "REPROVADO":
        return 96.0
    if status == "APROVADO":
        return 4.0

    # 1. Pressao de nota: distancia em relacao a media minima e dificuldade da Nota 2
    media = disc.mediaAtual if disc.mediaAtual is not None else (disc.nota1 or 0.0)
    deficit_nota = max(0.0, (media_minima - media) / max(media_minima, 1.0))

    pressao_nota2 = 0.0
    if disc.nota2 is None and disc.notaNecessariaParaPassar is not None:
        if disc.notaNecessariaParaPassar > 10.0:
            return 95.0
        pressao_nota2 = max(0.0, (disc.notaNecessariaParaPassar - media_minima) / max(10.0 - media_minima, 1.0))

    fator_nota = min(1.5, deficit_nota * 1.15 + pressao_nota2 * 0.65)

    # 2. Pressao de frequencia: consumo do limite de faltas permitido
    if disc.limiteFaltas > 0:
        consumo_faltas = min(1.5, disc.quantidadeFaltas / disc.limiteFaltas)
    else:
        consumo_faltas = 1.5 if disc.quantidadeFaltas > 0 else 0.0

    if disc.percentualFrequencia < frequencia_minima:
        return 96.0

    # 3. Interacao combinada (nota baixa + alto consumo de faltas eleva o risco exponencialmente)
    interacao = fator_nota * consumo_faltas

    # Combina fatores via funcao logistica calibrada (escala 0 a 100)
    logit = -2.35 + (2.9 * fator_nota) + (2.4 * consumo_faltas) + (1.6 * interacao)
    probabilidade = _sigmoid(logit) * 100.0

    if status == "RISCO_CRITICO":
        probabilidade = max(probabilidade, 78.0)

    return round(min(100.0, max(0.0, probabilidade)), 2)


def _gerar_recomendacoes(
    request: PredicaoRequest,
    riscos_por_disciplina: list[tuple[DisciplinaFeature, float]],
) -> list[str]:
    recomendacoes: list[str] = []

    ordenadas = sorted(riscos_por_disciplina, key=lambda item: item[1], reverse=True)
    em_risco = [item for item in ordenadas if item[1] >= 40.0]

    for disc, risco in em_risco[:3]:
        status = (disc.statusSituacao or "").strip().upper()
        if status == "REPROVADO":
            recomendacoes.append(
                f"Prioridade maxima em {disc.nomeDisciplina}: disciplina em situacao de reprovacao. "
                "Verifique possibilidade de recuperacao/exame final ou replanejamento da grade."
            )
            continue

        if disc.nota2 is None and disc.notaNecessariaParaPassar is not None and disc.notaNecessariaParaPassar > request.mediaMinimaAprovacao:
            recomendacoes.append(
                f"Foco de estudo em {disc.nomeDisciplina} (risco estimado {risco:.0f}%): "
                f"dedique revisao extra para alcancar pelo menos {disc.notaNecessariaParaPassar:.1f} na Avaliacao 2."
            )

        if disc.faltasRestantesPermitidas <= 3 and disc.limiteFaltas > 0:
            recomendacoes.append(
                f"Controle rigoroso de presenca em {disc.nomeDisciplina}: "
                f"restam apenas {disc.faltasRestantesPermitidas} falta(s) antes do limite de reprovacao por frequencia."
            )

    if len(em_risco) >= 2:
        recomendacoes.append(
            f"Atencao ao efeito cascata: {len(em_risco)} disciplinas apresentam risco elevado simultaneamente. "
            "Priorize o cronograma semanal pelas materias com maior risco."
        )

    if not recomendacoes:
        recomendacoes.append(
            "Ritmo academico saudavel segundo o modelo preditivo. Mantenha a constancia nas aulas e revisoes."
        )

    return recomendacoes


def avaliar_risco_academico(request: PredicaoRequest) -> PredicaoResposta:
    if not request.disciplinas:
        return PredicaoResposta(
            scoreRisco=0.0,
            classificacaoRisco="BAIXO",
            recomendacoes=[],
            modelo=MODELO_VERSAO,
        )

    riscos_por_disciplina: list[tuple[DisciplinaFeature, float]] = [
        (
            disc,
            _calcular_risco_disciplina(
                disc,
                request.mediaMinimaAprovacao,
                request.frequenciaMinimaExigida,
            ),
        )
        for disc in request.disciplinas
    ]

    valores_risco = [r for _, r in riscos_por_disciplina]
    risco_medio = sum(valores_risco) / len(valores_risco)
    risco_maximo = max(valores_risco)

    qtd_alerta = sum(1 for r in valores_risco if r >= 50.0)
    penalidade_cascata = min(15.0, max(0, qtd_alerta - 1) * 6.5)

    # Peso balanceado entre o pior gargalo (55%), media geral do semestre (45%) e efeito cascata
    score_final = round(
        min(100.0, max(0.0, (0.55 * risco_maximo) + (0.45 * risco_medio) + penalidade_cascata)),
        2,
    )

    classificacao = _classificar_risco(score_final)
    recomendacoes = _gerar_recomendacoes(request, riscos_por_disciplina)

    return PredicaoResposta(
        scoreRisco=score_final,
        classificacaoRisco=classificacao,
        recomendacoes=recomendacoes,
        modelo=MODELO_VERSAO,
    )
