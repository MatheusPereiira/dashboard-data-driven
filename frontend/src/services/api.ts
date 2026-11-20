import {
  AlunoDTO,
  DisciplinaDesempenhoDTO,
  DashboardPerformanceDTO,
  BatchImportReportDTO,
  StatusSituacao
} from '../types/api';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = `${BASE_URL}${endpoint}`;
  
  const headers = new Headers(options.headers || {});
  if (!(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMsg = `Erro na requisição (${response.status})`;
    try {
      const errorData = await response.json();
      if (errorData.message) {
        errorMsg = errorData.message;
      } else if (errorData.errors && Array.isArray(errorData.errors)) {
        errorMsg = errorData.errors.join('; ');
      }
    } catch {
      // fallback text
      const errorText = await response.text();
      if (errorText) errorMsg = errorText;
    }
    throw new Error(errorMsg);
  }

  // If 204 No Content
  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}

export const api = {
  // Alunos
  async listarAlunos(): Promise<AlunoDTO[]> {
    return request<AlunoDTO[]>('/alunos');
  },

  async buscarAluno(id: string): Promise<AlunoDTO> {
    return request<AlunoDTO>(`/alunos/${id}`);
  },

  async criarAluno(dto: AlunoDTO): Promise<AlunoDTO> {
    return request<AlunoDTO>('/alunos', {
      method: 'POST',
      body: JSON.stringify(dto),
    });
  },

  async atualizarAluno(id: string, dto: AlunoDTO): Promise<AlunoDTO> {
    return request<AlunoDTO>(`/alunos/${id}`, {
      method: 'PUT',
      body: JSON.stringify(dto),
    });
  },

  async excluirAluno(id: string): Promise<void> {
    return request<void>(`/alunos/${id}`, {
      method: 'DELETE',
    });
  },

  async obterDashboard(id: string): Promise<DashboardPerformanceDTO> {
    return request<DashboardPerformanceDTO>(`/alunos/${id}/dashboard`);
  },

  // Disciplinas
  async registrarDisciplina(dto: DisciplinaDesempenhoDTO): Promise<DashboardPerformanceDTO> {
    return request<DashboardPerformanceDTO>('/disciplinas', {
      method: 'POST',
      body: JSON.stringify(dto),
    });
  },

  async atualizarDisciplina(id: string, dto: DisciplinaDesempenhoDTO): Promise<DashboardPerformanceDTO> {
    return request<DashboardPerformanceDTO>(`/disciplinas/${id}`, {
      method: 'PUT',
      body: JSON.stringify(dto),
    });
  },

  async excluirDisciplina(id: string): Promise<DashboardPerformanceDTO> {
    return request<DashboardPerformanceDTO>(`/disciplinas/${id}`, {
      method: 'DELETE',
    });
  },

  async listarDisciplinas(
    alunoId: string,
    semestreLetivo?: string,
    statusSituacao?: StatusSituacao
  ): Promise<DisciplinaDesempenhoDTO[]> {
    const params = new URLSearchParams();
    if (semestreLetivo) params.append('semestreLetivo', semestreLetivo);
    if (statusSituacao) params.append('statusSituacao', statusSituacao);
    const qs = params.toString() ? `?${params.toString()}` : '';
    return request<DisciplinaDesempenhoDTO[]>(`/disciplinas/aluno/${alunoId}${qs}`);
  },

  // Importação em Lote via Spring Batch
  async importarBoletimCsv(alunoId: string, file: File): Promise<BatchImportReportDTO> {
    const formData = new FormData();
    formData.append('arquivo', file);

    return request<BatchImportReportDTO>(`/importacoes/boletim/${alunoId}`, {
      method: 'POST',
      body: formData,
    });
  },

  async baixarModeloCsv(): Promise<Blob> {
    const response = await fetch(`${BASE_URL}/importacoes/boletim/modelo`);
    if (!response.ok) {
      throw new Error('Falha ao baixar o modelo de CSV');
    }
    return response.blob();
  }
};
