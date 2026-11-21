import math
from schemas import ClassificacaoRisco, DisciplinaFeature, PredicaoRequest, PredicaoResposta

MODELO_VERSAO = "logistic-academic-risk-v2-fans"


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

    meta = media_minima * 10.0 if media_minima <= 10.0 else media_minima

    # Pontos acumulados
    pontos_acumulados = disc.mediaAtual if disc.mediaAtual is not None else 0.0
    if pontos_acumulados == 0.0:
        pontos_acumulados = (
            (disc.notaEtapa1 if disc.notaEtapa1 is not None else (disc.nota1 or 0.0))
            + (disc.notaEtapa2 if disc.notaEtapa2 is not None else (disc.nota2 or 0.0))
            + (disc.notaEtapa3 or 0.0)
        )

    # Pontos restantes possíveis
    pontos_restantes = 100.0
    if disc.notaEtapa1 is not None or disc.nota1 is not None:
        pontos_restantes -= 25.0
    if disc.notaEtapa2 is not None or disc.nota2 is not None:
        pontos_restantes -= 30.0
    if disc.notaEtapa3 is not None:
        pontos_restantes -= 45.0

    # Já atingiu a meta (60 pontos)
    if pontos_acumulados >= meta:
        return 5.0

    # Matematicamente impossível atingir a meta
    if pontos_acumulados + pontos_restantes < meta:
        return 96.0

    pontos_necessarios = max(0.0, meta - pontos_acumulados)
    if disc.notaNecessariaParaPassar is not None:
        pontos_necessarios = disc.notaNecessariaParaPassar

    # Taxa de esforço necessária sobre o saldo disponível
    taxa_esforco = pontos_necessarios / max(pontos_restantes, 1.0)
    fator_nota = min(1.5, taxa_esforco * 1.35)

    # 2. Pressão de faltas: consumo do limite de faltas institucional
    if disc.limiteFaltas > 0:
        consumo_faltas = min(1.5, disc.quantidadeFaltas / disc.limiteFaltas)
    else:
        consumo_faltas = 1.5 if disc.quantidadeFaltas > 0 else 0.0

    if disc.percentualFrequencia < frequencia_minima:
        return 96.0

    # 3. Interação combinada
    interacao = fator_nota * consumo_faltas

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
                "Verifique possibilidade de recuperacao/replanejamento da grade."
            )
            continue

        if disc.notaNecessariaParaPassar is not None and disc.notaNecessariaParaPassar > 0.0:
            recomendacoes.append(
                f"Foco de estudo em {disc.nomeDisciplina} (risco estimado {risco:.0f}%): "
                f"necessario acumular pelo menos {disc.notaNecessariaParaPassar:.1f} pontos nas etapas restantes para alcancar a meta institucional."
            )

        if disc.faltasRestantesPermitidas <= 3 and disc.limiteFaltas > 0:
            recomendacoes.append(
                f"Controle rigoroso de frequencia em {disc.nomeDisciplina}: "
                f"restam apenas {disc.faltasRestantesPermitidas} falta(s) permitidas dentro da margem de 20% de ausencia."
            )

    if len(em_risco) >= 2:
        recomendacoes.append(
            f"Atencao ao efeito cascata: {len(em_risco)} disciplinas apresentam risco elevado simultaneamente. "
            "Priorize o cronograma semanal pelas materias com maior risco."
        )

    if not recomendacoes:
        recomendacoes.append("Desempenho estavel: continue mantendo a assiduidade e a rotina de estudos em dia.")

    return recomendacoes


def avaliar_risco_academico(request: PredicaoRequest) -> PredicaoResposta:
    return predizer(request)


def predizer(request: PredicaoRequest) -> PredicaoResposta:
    if not request.disciplinas:
        return PredicaoResposta(
            scoreRisco=0.0,
            classificacaoRisco="BAIXO",
            recomendacoes=["Nenhuma disciplina cadastrada para analise preditiva."],
            modelo=MODELO_VERSAO,
        )

    riscos: list[tuple[DisciplinaFeature, float]] = []
    for disc in request.disciplinas:
        score_disc = _calcular_risco_disciplina(
            disc,
            request.mediaMinimaAprovacao,
            request.frequenciaMinimaExigida,
        )
        riscos.append((disc, score_disc))

    # Ponderação com foco no pior caso (70% max, 30% media)
    max_risco = max(score for _, score in riscos)
    media_risco = sum(score for _, score in riscos) / len(riscos)
    score_geral = round(0.70 * max_risco + 0.30 * media_risco, 2)

    classificacao = _classificar_risco(score_geral)
    recomendacoes = _gerar_recomendacoes(request, riscos)

    return PredicaoResposta(
        scoreRisco=score_geral,
        classificacaoRisco=classificacao,
        recomendacoes=recomendacoes,
        modelo=MODELO_VERSAO,
    )
