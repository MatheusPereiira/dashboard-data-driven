import React, { useState, useEffect, useCallback } from 'react';
import {
  AlunoDTO,
  DisciplinaDesempenhoDTO,
  DashboardPerformanceDTO,
  BatchImportReportDTO,
} from './types/api';
import { api } from './services/api';
import { Navbar } from './components/Navbar';
import { KpiCards } from './components/KpiCards';
import { AlertasBanner } from './components/AlertasBanner';
import { ChartsSection } from './components/ChartsSection';
import { DisciplinasTable } from './components/DisciplinasTable';
import { DisciplinaModal } from './components/DisciplinaModal';
import { AlunoModal } from './components/AlunoModal';
import { CsvImportModal } from './components/CsvImportModal';
import { EmptyState } from './components/EmptyState';
import { ToastContainer, ToastMessage } from './components/Toast';
import { BookOpen, Calendar, GraduationCap, School } from 'lucide-react';

export const App: React.FC = () => {
  const [alunos, setAlunos] = useState<AlunoDTO[]>([]);
  const [selectedAlunoId, setSelectedAlunoId] = useState<string | null>(null);
  const [dashboard, setDashboard] = useState<DashboardPerformanceDTO | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Modals state
  const [isNovoAlunoModalOpen, setIsNovoAlunoModalOpen] = useState(false);
  const [isEditarAlunoModalOpen, setIsEditarAlunoModalOpen] = useState(false);
  const [isDisciplinaModalOpen, setIsDisciplinaModalOpen] = useState(false);
  const [editingDisciplina, setEditingDisciplina] = useState<DisciplinaDesempenhoDTO | null>(null);
  const [isCsvModalOpen, setIsCsvModalOpen] = useState(false);

  // Toasts
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const addToast = (type: 'success' | 'warning' | 'error' | 'info', message: string, title?: string) => {
    const id = Math.random().toString(36).substring(2, 9);
    setToasts((prev) => [...prev, { id, type, message, title }]);
    setTimeout(() => {
      dismissToast(id);
    }, 4500);
  };

  const dismissToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  // Fetch dashboard for active student
  const fetchDashboard = useCallback(async (alunoId: string) => {
    try {
      setIsLoading(true);
      const data = await api.obterDashboard(alunoId);
      setDashboard(data);
    } catch (err: any) {
      addToast('error', err.message || 'Falha ao carregar dashboard', 'Erro de Conexão');
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Fetch list of all students
  const fetchAlunos = useCallback(async (preferredId?: string) => {
    try {
      setIsLoading(true);
      const lista = await api.listarAlunos();
      setAlunos(lista);

      if (lista.length > 0) {
        let targetId = preferredId;
        if (!targetId || !lista.some((a) => a.id === targetId)) {
          const savedId = localStorage.getItem('lastSelectedAlunoId');
          targetId = savedId && lista.some((a) => a.id === savedId) ? savedId : lista[0].id!;
        }
        setSelectedAlunoId(targetId);
        localStorage.setItem('lastSelectedAlunoId', targetId);
        await fetchDashboard(targetId);
      } else {
        setSelectedAlunoId(null);
        setDashboard(null);
      }
    } catch (err: any) {
      addToast(
        'warning',
        'Não foi possível conectar ao backend na porta 8080. Verifique se o serviço Spring Boot está ativo.',
        'Aviso de Conexão'
      );
      setAlunos([]);
      setDashboard(null);
    } finally {
      setIsLoading(false);
    }
  }, [fetchDashboard]);

  useEffect(() => {
    fetchAlunos();
  }, [fetchAlunos]);

  // Handle switching active student
  const handleSelectAluno = (id: string) => {
    setSelectedAlunoId(id);
    localStorage.setItem('lastSelectedAlunoId', id);
    fetchDashboard(id);
  };

  // Student CRUD
  const handleSaveNovoAluno = async (dto: AlunoDTO) => {
    const criado = await api.criarAluno(dto);
    addToast('success', `Estudante "${criado.nome}" cadastrado com sucesso!`, 'Cadastro Realizado');
    await fetchAlunos(criado.id);
  };

  const handleUpdateAluno = async (dto: AlunoDTO) => {
    if (!selectedAlunoId) return;
    const atualizado = await api.atualizarAluno(selectedAlunoId, dto);
    addToast('success', 'Critérios e perfil institucional atualizados!', 'Perfil Atualizado');
    await fetchAlunos(atualizado.id);
  };

  const handleDeleteAluno = async (id: string) => {
    await api.excluirAluno(id);
    addToast('info', 'Estudante e dados vinculados removidos.', 'Aluno Removido');
    localStorage.removeItem('lastSelectedAlunoId');
    await fetchAlunos();
  };

  // Discipline CRUD
  const handleOpenNovaDisciplina = () => {
    setEditingDisciplina(null);
    setIsDisciplinaModalOpen(true);
  };

  const handleOpenEditarDisciplina = (disc: DisciplinaDesempenhoDTO) => {
    setEditingDisciplina(disc);
    setIsDisciplinaModalOpen(true);
  };

  const handleSaveDisciplina = async (dto: DisciplinaDesempenhoDTO) => {
    if (dto.id) {
      const res = await api.atualizarDisciplina(dto.id, dto);
      setDashboard(res);
      addToast('success', `Disciplina "${dto.nomeDisciplina}" atualizada com sucesso!`, 'Atualizado');
    } else {
      const res = await api.registrarDisciplina(dto);
      setDashboard(res);
      addToast('success', `Disciplina "${dto.nomeDisciplina}" cadastrada e analisada!`, 'Registrado');
    }
  };

  const handleDeleteDisciplina = async (id: string, nome: string) => {
    if (!confirm(`Deseja realmente remover a disciplina "${nome}"?`)) return;
    try {
      const res = await api.excluirDisciplina(id);
      setDashboard(res);
      addToast('info', `Disciplina "${nome}" removida e indicadores recalculados.`, 'Excluído');
    } catch (err: any) {
      addToast('error', err.message || 'Erro ao excluir disciplina', 'Erro');
    }
  };

  // CSV Import
  const handleImportCsv = async (file: File): Promise<BatchImportReportDTO> => {
    if (!selectedAlunoId) throw new Error('Nenhum aluno ativo selecionado.');
    const report = await api.importarBoletimCsv(selectedAlunoId, file);
    if (report.dashboard) {
      setDashboard(report.dashboard);
    } else {
      await fetchDashboard(selectedAlunoId);
    }
    addToast(
      'success',
      `Importação concluída: ${report.totalImportados} disciplina(s) inserida(s).`,
      'Spring Batch Concluído'
    );
    return report;
  };

  const handleDownloadCsvTemplate = async () => {
    try {
      const blob = await api.baixarModeloCsv();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'modelo-boletim.csv';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err: any) {
      addToast('error', err.message || 'Falha ao baixar modelo CSV', 'Erro');
    }
  };

  const currentAluno = dashboard?.aluno || alunos.find((a) => a.id === selectedAlunoId) || null;

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      {/* Top Navigation */}
      <Navbar
        alunos={alunos}
        currentAluno={currentAluno}
        onSelectAluno={handleSelectAluno}
        onOpenNovoAlunoModal={() => setIsNovoAlunoModalOpen(true)}
        onOpenEditarAlunoModal={() => setIsEditarAlunoModalOpen(true)}
        onRefresh={() => selectedAlunoId && fetchDashboard(selectedAlunoId)}
        isLoading={isLoading}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* If no students exist at all */}
        {!isLoading && alunos.length === 0 && (
          <EmptyState
            type="no-students"
            onOpenNovoAluno={() => setIsNovoAlunoModalOpen(true)}
          />
        )}

        {/* If student is selected and dashboard is available */}
        {currentAluno && dashboard && (
          <>
            {/* Student Profile Hero Strip */}
            <div className="glass-panel rounded-2xl p-5 border border-slate-800/80 flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-2xl bg-indigo-600/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center font-bold text-lg">
                  {currentAluno.nome.charAt(0).toUpperCase()}
                </div>
                <div>
                  <div className="flex items-center gap-2.5">
                    <h2 className="text-xl font-extrabold text-white tracking-tight">
                      {currentAluno.nome}
                    </h2>
                    <span className="text-xs font-mono px-2 py-0.5 rounded-md bg-slate-800 text-slate-300 border border-slate-700">
                      RA: {currentAluno.matricula}
                    </span>
                  </div>
                  <div className="flex flex-wrap items-center gap-3 mt-1 text-xs text-slate-400">
                    <span className="flex items-center gap-1">
                      <GraduationCap className="w-3.5 h-3.5 text-indigo-400" />
                      {currentAluno.curso}
                    </span>
                    <span>•</span>
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5 text-cyan-400" />
                      {currentAluno.semestreAtual}º Semestre
                    </span>
                  </div>
                </div>
              </div>

              {/* Institutional Norms Tags */}
              <div className="flex flex-wrap items-center gap-2">
                <div className="px-3 py-1.5 rounded-xl bg-slate-900/90 border border-slate-800 text-xs">
                  <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                    Corte Institucional
                  </span>
                  <span className="font-bold text-white">
                    Nota {currentAluno.mediaMinimaAprovacao.toFixed(1)}
                  </span>
                </div>
                <div className="px-3 py-1.5 rounded-xl bg-slate-900/90 border border-slate-800 text-xs">
                  <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                    Assiduidade Mínima
                  </span>
                  <span className="font-bold text-white">
                    {currentAluno.frequenciaMinimaExigida ?? 75.0}%
                  </span>
                </div>
              </div>
            </div>

            {/* Check if student has disciplines */}
            {dashboard.disciplinas.length === 0 ? (
              <EmptyState
                type="no-disciplines"
                studentName={currentAluno.nome}
                onOpenNovaDisciplina={handleOpenNovaDisciplina}
                onOpenImportCsv={() => setIsCsvModalOpen(true)}
              />
            ) : (
              <>
                {/* 1. Global KPIs Cards */}
                <KpiCards dashboard={dashboard} />

                {/* 2. Predictive AI Diagnostics and Recommendations */}
                <AlertasBanner
                  alertas={dashboard.analisePreditiva?.alertasPreditivos || []}
                />

                {/* 3. Analytics Charts */}
                <ChartsSection dashboard={dashboard} />

                {/* 4. Disciplines Interactive Table */}
                <DisciplinasTable
                  disciplinas={dashboard.disciplinas}
                  mediaCorte={currentAluno.mediaMinimaAprovacao}
                  freqMinima={currentAluno.frequenciaMinimaExigida ?? 75.0}
                  onNovaDisciplina={handleOpenNovaDisciplina}
                  onEditarDisciplina={handleOpenEditarDisciplina}
                  onExcluirDisciplina={handleDeleteDisciplina}
                  onAbrirImportacaoCsv={() => setIsCsvModalOpen(true)}
                  onBaixarModeloCsv={handleDownloadCsvTemplate}
                />
              </>
            )}
          </>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800/80 bg-slate-950 py-4 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>Sistema Pessoal de Monitoramento de Desempenho e Risco Acadêmico (Data-Driven)</span>
          <span>Java 25 • Spring Boot 4 • MongoDB • Spring Batch • Python AI</span>
        </div>
      </footer>

      {/* Modals */}
      <AlunoModal
        isOpen={isNovoAlunoModalOpen}
        onClose={() => setIsNovoAlunoModalOpen(false)}
        onSave={handleSaveNovoAluno}
      />

      <AlunoModal
        isOpen={isEditarAlunoModalOpen}
        onClose={() => setIsEditarAlunoModalOpen(false)}
        onSave={handleUpdateAluno}
        onDelete={handleDeleteAluno}
        initialData={currentAluno}
      />

      <DisciplinaModal
        isOpen={isDisciplinaModalOpen}
        onClose={() => {
          setIsDisciplinaModalOpen(false);
          setEditingDisciplina(null);
        }}
        onSave={handleSaveDisciplina}
        alunoId={selectedAlunoId || ''}
        initialData={editingDisciplina}
        mediaCorte={currentAluno?.mediaMinimaAprovacao ?? 6.0}
      />

      <CsvImportModal
        isOpen={isCsvModalOpen}
        onClose={() => setIsCsvModalOpen(false)}
        onImport={handleImportCsv}
        onDownloadTemplate={handleDownloadCsvTemplate}
      />

      {/* Toast Notifications */}
      <ToastContainer toasts={toasts} onDismiss={dismissToast} />
    </div>
  );
};
