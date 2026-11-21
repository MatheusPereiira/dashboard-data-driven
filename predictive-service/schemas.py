from typing import Literal, Optional
from pydantic import BaseModel, Field


ClassificacaoRisco = Literal["BAIXO", "MODERADO", "ALTO", "CRITICO"]


class DisciplinaFeature(BaseModel):
    nomeDisciplina: str = Field(..., min_length=1)
    notaEtapa1: Optional[float] = Field(default=None, ge=0.0, le=25.0)
    notaEtapa2: Optional[float] = Field(default=None, ge=0.0, le=30.0)
    notaEtapa3: Optional[float] = Field(default=None, ge=0.0, le=45.0)
    nota1: Optional[float] = Field(default=None, ge=0.0, le=100.0)
    nota2: Optional[float] = Field(default=None, ge=0.0, le=100.0)
    mediaAtual: Optional[float] = Field(default=None, ge=0.0, le=100.0)
    notaNecessariaParaPassar: Optional[float] = Field(default=None, ge=0.0)
    quantidadeFaltas: int = Field(..., ge=0)
    totalAulasSemestre: int = Field(..., gt=0)
    limiteFaltas: int = Field(..., ge=0)
    faltasRestantesPermitidas: int = Field(..., ge=0)
    percentualFrequencia: float = Field(..., ge=0.0, le=100.0)
    statusSituacao: Optional[str] = Field(default=None)


class PredicaoRequest(BaseModel):
    alunoId: str = Field(..., min_length=1)
    mediaMinimaAprovacao: float = Field(..., gt=0.0, le=100.0)
    frequenciaMinimaExigida: float = Field(..., ge=0.0, le=100.0)
    disciplinas: list[DisciplinaFeature] = Field(default_factory=list)


class PredicaoResposta(BaseModel):
    scoreRisco: float = Field(..., ge=0.0, le=100.0)
    classificacaoRisco: ClassificacaoRisco
    recomendacoes: list[str] = Field(default_factory=list)
    modelo: str
