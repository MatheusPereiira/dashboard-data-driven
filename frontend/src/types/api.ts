export type StatusSituacao =
  | 'APROVADO'
  | 'REGULAR'
  | 'ATENCAO_NOTA'
  | 'ATENCAO_FALTA'
  | 'RISCO_CRITICO'
  | 'REPROVADO';

export type ClassificacaoRisco = 'BAIXO' | 'MODERADO' | 'ALTO' | 'CRITICO';

export interface AlunoDTO {
  id?: string;
  nome: string;
  matricula: string;
  curso: string;
  semestreAtual: number;
  mediaMinimaAprovacao: number;
  frequenciaMinimaExigida?: number;
}

export interface DisciplinaDesempenhoDTO {
  id?: string;
  alunoId: string;
  nomeDisciplina: string;
  semestreLetivo: string;
  totalAulasSemestre: number;
  quantidadeFaltas: number;
  percentualFrequencia?: number;
  notaEtapa1?: number | null;
  notaEtapa2?: number | null;
  notaEtapa3?: number | null;
  nota1?: number; // compatibilidade legada
  nota2?: number | null; // compatibilidade legada
  mediaAtual?: number;
  notaNecessariaParaPassar?: number;
  faltasRestantesPermitidas?: number;
  statusSituacao?: StatusSituacao;
}

export interface AnalisePreditivaDTO {
  id?: string;
  alunoId?: string;
  dataAtualizacao?: string;
  indiceRendimentoAcademico: number;
  frequenciaGlobal: number;
  scoreRiscoEvasaoReprovacao: number;
  classificacaoRisco: ClassificacaoRisco;
  alertasPreditivos: string[];
}

export interface DashboardPerformanceDTO {
  aluno: AlunoDTO;
  disciplinas: DisciplinaDesempenhoDTO[];
  analisePreditiva: AnalisePreditivaDTO;
  totalDisciplinasMatriculadas: number;
  totalDisciplinasEmAlerta: number;
}

export interface BatchImportReportDTO {
  jobExecutionId: string;
  status: string;
  totalLidos: number;
  totalImportados: number;
  totalRejeitados: number;
  avisos: string[];
  dashboard: DashboardPerformanceDTO;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  errors?: string[];
}
