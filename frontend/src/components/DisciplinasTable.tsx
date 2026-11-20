import React, { useState, useMemo } from 'react';
import { DisciplinaDesempenhoDTO, StatusSituacao } from '../types/api';
import {
  Search,
  Filter,
  Plus,
  FileSpreadsheet,
  Download,
  Edit2,
  Trash2,
  AlertCircle,
  CheckCircle,
  HelpCircle,
  Flame,
} from 'lucide-react';

interface DisciplinasTableProps {
  disciplinas: DisciplinaDesempenhoDTO[];
  mediaCorte: number;
  freqMinima: number;
  onNovaDisciplina: () => void;
  onEditarDisciplina: (disciplina: DisciplinaDesempenhoDTO) => void;
  onExcluirDisciplina: (id: string, nome: string) => void;
  onAbrirImportacaoCsv: () => void;
  onBaixarModeloCsv: () => void;
}

export const DisciplinasTable: React.FC<DisciplinasTableProps> = ({
  disciplinas,
  mediaCorte,
  freqMinima,
  onNovaDisciplina,
  onEditarDisciplina,
  onExcluirDisciplina,
  onAbrirImportacaoCsv,
  onBaixarModeloCsv,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedSemester, setSelectedSemester] = useState<string>('TODOS');
  const [selectedStatus, setSelectedStatus] = useState<string>('TODOS');

  // Extract unique semesters
  const semestresDisponiveis = useMemo(() => {
    const set = new Set<string>();
    disciplinas.forEach((d) => {
      if (d.semestreLetivo) set.add(d.semestreLetivo);
    });
    return Array.from(set).sort().reverse();
  }, [disciplinas]);

  // Filtered list
  const filteredDisciplinas = useMemo(() => {
    return disciplinas.filter((d) => {
      const matchSearch = d.nomeDisciplina.toLowerCase().includes(searchTerm.toLowerCase());
      const matchSemester = selectedSemester === 'TODOS' || d.semestreLetivo === selectedSemester;
      const matchStatus = selectedStatus === 'TODOS' || d.statusSituacao === selectedStatus;
      return matchSearch && matchSemester && matchStatus;
    });
  }, [disciplinas, searchTerm, selectedSemester, selectedStatus]);

  // Status Badge Helper
  const renderStatusBadge = (status?: StatusSituacao) => {
    switch (status) {
      case 'APROVADO':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            <CheckCircle className="w-3 h-3" /> Aprovado
          </span>
        );
      case 'REGULAR':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/30">
            <CheckCircle className="w-3 h-3" /> Regular
          </span>
        );
      case 'ATENCAO_NOTA':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/30">
            <AlertCircle className="w-3 h-3" /> Atenção Nota
          </span>
        );
      case 'ATENCAO_FALTA':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-orange-500/10 text-orange-400 border border-orange-500/30">
            <AlertCircle className="w-3 h-3" /> Atenção Falta
          </span>
        );
      case 'RISCO_CRITICO':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-500/20 text-rose-300 border border-rose-500/40 animate-pulse">
            <Flame className="w-3 h-3 text-rose-400" /> Risco Crítico
          </span>
        );
      case 'REPROVADO':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-red-900/40 text-red-400 border border-red-700/50">
            Reprovado
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-800 text-slate-400 border border-slate-700">
            <HelpCircle className="w-3 h-3" /> Pendente
          </span>
        );
    }
  };

  return (
    <div className="glass-panel rounded-2xl overflow-hidden border border-slate-800 flex flex-col">
      {/* Header and Toolbar */}
      <div className="p-5 border-b border-slate-800/80 flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <div>
          <h3 className="text-base font-bold text-white tracking-tight flex items-center gap-2">
            Desempenho por Componente Curricular
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
              {filteredDisciplinas.length} {filteredDisciplinas.length === 1 ? 'matéria' : 'matérias'}
            </span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Gerenciamento detalhado de avaliações, frequência e projeções automáticas de aprovação
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={onBaixarModeloCsv}
            title="Baixar planilha modelo CSV para importação"
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-medium border border-slate-700 transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Modelo CSV</span>
          </button>

          <button
            onClick={onAbrirImportacaoCsv}
            className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-indigo-950/60 hover:bg-indigo-900/80 text-indigo-300 text-xs font-medium border border-indigo-500/30 transition-all hover:border-indigo-500/50"
          >
            <FileSpreadsheet className="w-4 h-4 text-indigo-400" />
            <span>Importar Boletim CSV</span>
          </button>

          <button
            onClick={onNovaDisciplina}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-medium shadow-md shadow-indigo-600/20 transition-all active:scale-95"
          >
            <Plus className="w-4 h-4" />
            <span>Lançar Disciplina</span>
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="px-5 py-3.5 bg-slate-900/60 border-b border-slate-800 flex flex-wrap items-center justify-between gap-3 text-xs">
        {/* Search */}
        <div className="relative flex-1 min-w-[220px]">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Buscar por nome da disciplina..."
            className="w-full bg-slate-950/60 text-slate-200 placeholder-slate-500 rounded-lg pl-9 pr-3 py-1.5 border border-slate-800 focus:outline-none focus:border-indigo-500 transition-all"
          />
        </div>

        {/* Filters */}
        <div className="flex items-center gap-2">
          {/* Semester Filter */}
          <div className="flex items-center gap-1.5">
            <span className="text-slate-400 hidden sm:inline">Semestre:</span>
            <select
              value={selectedSemester}
              onChange={(e) => setSelectedSemester(e.target.value)}
              className="bg-slate-850 bg-slate-900 text-slate-300 rounded-lg px-2.5 py-1.5 border border-slate-850 border-slate-750 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              <option value="TODOS">Todos os Semestres</option>
              {semestresDisponiveis.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>

          {/* Status Filter */}
          <div className="flex items-center gap-1.5">
            <Filter className="w-3.5 h-3.5 text-slate-400 hidden sm:inline" />
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="bg-slate-900 text-slate-300 rounded-lg px-2.5 py-1.5 border border-slate-750 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              <option value="TODOS">Todos os Status</option>
              <option value="APROVADO">Aprovado</option>
              <option value="REGULAR">Regular</option>
              <option value="ATENCAO_NOTA">Atenção Nota</option>
              <option value="ATENCAO_FALTA">Atenção Falta</option>
              <option value="RISCO_CRITICO">Risco Crítico</option>
              <option value="REPROVADO">Reprovado</option>
            </select>
          </div>
        </div>
      </div>

      {/* Disciplines Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-900/90 text-slate-400 border-b border-slate-800 uppercase tracking-wider font-semibold text-[11px]">
            <tr>
              <th className="py-3 px-4">Disciplina / Período</th>
              <th className="py-3 px-3 text-center">N1</th>
              <th className="py-3 px-3 text-center">N2</th>
              <th className="py-3 px-3 text-center">Média Atual</th>
              <th className="py-3 px-4">Frequência & Faltas</th>
              <th className="py-3 px-4">Metas e Projeções</th>
              <th className="py-3 px-4 text-center">Situação</th>
              <th className="py-3 px-4 text-right">Ações</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {filteredDisciplinas.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-12 text-center text-slate-500">
                  <p className="text-sm">Nenhuma disciplina encontrada com os filtros selecionados.</p>
                  <button
                    onClick={onNovaDisciplina}
                    className="mt-3 inline-flex items-center gap-1.5 text-indigo-400 hover:text-indigo-300 text-xs font-semibold"
                  >
                    <Plus className="w-3.5 h-3.5" /> Lançar primeira disciplina agora
                  </button>
                </td>
              </tr>
            ) : (
              filteredDisciplinas.map((disc) => {
                const media = disc.mediaAtual ?? disc.nota1;
                const freq = disc.percentualFrequencia ?? 100;
                const isNotaOk = media >= mediaCorte;
                const isFreqOk = freq >= freqMinima;

                return (
                  <tr key={disc.id} className="hover:bg-slate-800/40 transition-colors">
                    {/* Name & Semester */}
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-white text-sm">
                        {disc.nomeDisciplina}
                      </div>
                      <div className="text-[11px] text-slate-400 flex items-center gap-2 mt-0.5">
                        <span className="px-1.5 py-0.2 rounded bg-slate-800 text-slate-300">
                          {disc.semestreLetivo}
                        </span>
                        <span>{disc.totalAulasSemestre} aulas totais</span>
                      </div>
                    </td>

                    {/* Nota 1 */}
                    <td className="py-3.5 px-3 text-center font-mono font-medium text-slate-200">
                      {disc.nota1.toFixed(1)}
                    </td>

                    {/* Nota 2 */}
                    <td className="py-3.5 px-3 text-center font-mono font-medium text-slate-200">
                      {disc.nota2 !== null && disc.nota2 !== undefined ? (
                        disc.nota2.toFixed(1)
                      ) : (
                        <span className="text-slate-500 italic">Pendente</span>
                      )}
                    </td>

                    {/* Média Atual */}
                    <td className="py-3.5 px-3 text-center">
                      <span
                        className={`font-mono font-bold text-sm px-2 py-0.5 rounded-md ${
                          isNotaOk
                            ? 'bg-emerald-500/10 text-emerald-400'
                            : 'bg-amber-500/10 text-amber-400'
                        }`}
                      >
                        {media.toFixed(1)}
                      </span>
                    </td>

                    {/* Frequência & Faltas */}
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-2">
                        <span
                          className={`font-semibold ${
                            isFreqOk ? 'text-cyan-400' : 'text-rose-400'
                          }`}
                        >
                          {freq.toFixed(1)}%
                        </span>
                        <span className="text-slate-400 text-[11px]">
                          ({disc.quantidadeFaltas} faltas)
                        </span>
                      </div>
                      <div className="w-24 bg-slate-800 rounded-full h-1 mt-1 overflow-hidden">
                        <div
                          className={`h-full rounded-full ${
                            isFreqOk ? 'bg-cyan-500' : 'bg-rose-500'
                          }`}
                          style={{ width: `${Math.min(100, freq)}%` }}
                        />
                      </div>
                    </td>

                    {/* Projeções */}
                    <td className="py-3.5 px-4">
                      {disc.statusSituacao === 'APROVADO' ? (
                        <span className="text-emerald-400 font-medium">Meta alcançada</span>
                      ) : disc.statusSituacao === 'REPROVADO' ? (
                        <span className="text-rose-400 font-medium">Reprovação consolidada</span>
                      ) : (
                        <div className="space-y-0.5 text-[11px]">
                          {disc.notaNecessariaParaPassar !== undefined &&
                            disc.notaNecessariaParaPassar !== null &&
                            disc.notaNecessariaParaPassar > 0 && (
                              <div className="text-amber-300">
                                Precisa de <strong className="font-semibold text-white">{disc.notaNecessariaParaPassar.toFixed(1)}</strong> na N2
                              </div>
                            )}
                          {disc.faltasRestantesPermitidas !== undefined &&
                            disc.faltasRestantesPermitidas !== null && (
                              <div
                                className={
                                  disc.faltasRestantesPermitidas <= 2
                                    ? 'text-rose-400 font-bold'
                                    : 'text-slate-400'
                                }
                              >
                                Restam <strong className="font-semibold text-slate-200">{disc.faltasRestantesPermitidas}</strong> faltas permitidas
                              </div>
                            )}
                        </div>
                      )}
                    </td>

                    {/* Situação */}
                    <td className="py-3.5 px-4 text-center">
                      {renderStatusBadge(disc.statusSituacao)}
                    </td>

                    {/* Actions */}
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => onEditarDisciplina(disc)}
                          title="Editar notas ou faltas"
                          className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors"
                        >
                          <Edit2 className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => disc.id && onExcluirDisciplina(disc.id, disc.nomeDisciplina)}
                          title="Excluir disciplina"
                          className="p-1.5 rounded-lg bg-slate-800 hover:bg-rose-950 text-slate-400 hover:text-rose-400 transition-colors"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
