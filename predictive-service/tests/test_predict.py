import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from fastapi.testclient import TestClient
from main import app

client = TestClient(app)


def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "UP"
    assert "modelo" in body


def test_predict_sem_disciplinas_retorna_risco_baixo():
    payload = {
        "alunoId": "6740f1c2a1b2c3d4e5f60789",
        "mediaMinimaAprovacao": 6.0,
        "frequenciaMinimaExigida": 75.0,
        "disciplinas": [],
    }
    response = client.post("/api/v1/predict", json=payload)
    assert response.status_code == 200
    body = response.json()
    assert body["scoreRisco"] == 0.0
    assert body["classificacaoRisco"] == "BAIXO"
    assert body["modelo"] == "logistic-academic-risk-v1"


def test_predict_aluno_aprovado_retorna_risco_baixo():
    payload = {
        "alunoId": "6740f1c2a1b2c3d4e5f60789",
        "mediaMinimaAprovacao": 6.0,
        "frequenciaMinimaExigida": 75.0,
        "disciplinas": [
            {
                "nomeDisciplina": "Estrutura de Dados",
                "nota1": 8.5,
                "nota2": 9.0,
                "mediaAtual": 8.75,
                "notaNecessariaParaPassar": 0.0,
                "quantidadeFaltas": 2,
                "totalAulasSemestre": 80,
                "limiteFaltas": 20,
                "faltasRestantesPermitidas": 18,
                "percentualFrequencia": 97.5,
                "statusSituacao": "APROVADO",
            }
        ],
    }
    response = client.post("/api/v1/predict", json=payload)
    assert response.status_code == 200
    body = response.json()
    assert body["scoreRisco"] < 25.0
    assert body["classificacaoRisco"] == "BAIXO"
    assert len(body["recomendacoes"]) >= 1


def test_predict_disciplina_risco_critico_retorna_critico_e_recomendacoes():
    payload = {
        "alunoId": "6740f1c2a1b2c3d4e5f60789",
        "mediaMinimaAprovacao": 6.0,
        "frequenciaMinimaExigida": 75.0,
        "disciplinas": [
            {
                "nomeDisciplina": "Banco de Dados",
                "nota1": 4.5,
                "nota2": None,
                "mediaAtual": 4.5,
                "notaNecessariaParaPassar": 7.5,
                "quantidadeFaltas": 13,
                "totalAulasSemestre": 60,
                "limiteFaltas": 15,
                "faltasRestantesPermitidas": 2,
                "percentualFrequencia": 78.33,
                "statusSituacao": "RISCO_CRITICO",
            }
        ],
    }
    response = client.post("/api/v1/predict", json=payload)
    assert response.status_code == 200
    body = response.json()
    assert body["scoreRisco"] >= 75.0
    assert body["classificacaoRisco"] == "CRITICO"
    assert any("Banco de Dados" in rec for rec in body["recomendacoes"])


def test_predict_payload_invalido_retorna_422():
    payload = {
        "alunoId": "",
        "mediaMinimaAprovacao": 15.0,
        "frequenciaMinimaExigida": 75.0,
        "disciplinas": [],
    }
    response = client.post("/api/v1/predict", json=payload)
    assert response.status_code == 422
