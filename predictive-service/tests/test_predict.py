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
        "mediaMinimaAprovacao": 60.0,
        "frequenciaMinimaExigida": 80.0,
        "disciplinas": [],
    }
    response = client.post("/api/v1/predict", json=payload)
    assert response.status_code == 200
    body = response.json()
    assert body["scoreRisco"] == 0.0
    assert body["classificacaoRisco"] == "BAIXO"
    assert "fans" in body["modelo"]


def test_predict_aluno_aprovado_retorna_risco_baixo():
    payload = {
        "alunoId": "6740f1c2a1b2c3d4e5f60789",
        "mediaMinimaAprovacao": 60.0,
        "frequenciaMinimaExigida": 80.0,
        "disciplinas": [
            {
                "nomeDisciplina": "Estrutura de Dados",
                "notaEtapa1": 22.0,
                "notaEtapa2": 26.0,
                "notaEtapa3": 38.0,
                "mediaAtual": 86.0,
                "notaNecessariaParaPassar": 0.0,
                "quantidadeFaltas": 2,
                "totalAulasSemestre": 80,
                "limiteFaltas": 16,
                "faltasRestantesPermitidas": 14,
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
        "mediaMinimaAprovacao": 60.0,
        "frequenciaMinimaExigida": 80.0,
        "disciplinas": [
            {
                "nomeDisciplina": "Banco de Dados",
                "notaEtapa1": 8.0,
                "notaEtapa2": None,
                "notaEtapa3": None,
                "mediaAtual": 8.0,
                "notaNecessariaParaPassar": 52.0,
                "quantidadeFaltas": 14,
                "totalAulasSemestre": 80,
                "limiteFaltas": 16,
                "faltasRestantesPermitidas": 2,
                "percentualFrequencia": 82.5,
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
        "mediaMinimaAprovacao": 150.0,
        "frequenciaMinimaExigida": 80.0,
        "disciplinas": [],
    }
    response = client.post("/api/v1/predict", json=payload)
    assert response.status_code == 422
