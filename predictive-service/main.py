from fastapi import FastAPI
from predictor import MODELO_VERSAO, avaliar_risco_academico
from schemas import PredicaoRequest, PredicaoResposta

app = FastAPI(
    title="Academic Retention Predictive Service",
    description="Microservico FastAPI de analise preditiva de risco de reprovacao e evasao academica.",
    version="1.0.0",
)


@app.get("/health")
def health_check() -> dict[str, str]:
    return {"status": "UP", "modelo": MODELO_VERSAO}


@app.post("/api/v1/predict", response_model=PredicaoResposta)
def predict(request: PredicaoRequest) -> PredicaoResposta:
    return avaliar_risco_academico(request)
