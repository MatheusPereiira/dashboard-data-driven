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

  // Normaliza meta de aprovação para escala 100 pontos
  const metaAprovacao = mediaCorte <= 10.0 ? mediaCorte * 10 : mediaCorte;
  const frequenciaExigida = freqMinima ?? 80.0;

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
            <CheckCircle className="w-3 h-3" /> Em Curso
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
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-800 text-slate-400">
            Pendente
          </span>
        );
    }
  };

  return (
    <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
      {/* Table Header and Action Strip */}
      <div className="p-5 border-b border-slate-800 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            Desempenho por Componente Curricular
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Métrica institucional FANS: 3 Etapas somando 100 pontos • Aprovação: 60 pts • Presença mínima: 80% (máx. 20% faltas)
          </p>
        </div>

        {/* Buttons */}
        <div className="flex items-center gap-2.5">
          <button
            onClick={onBaixarModeloCsv}
            title="Baixar planilha modelo de exemplo para preenchimento"
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-300 hover:text-white text-xs font-medium transition-colors border border-slate-700/60"
          >
            <Download className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Modelo CSV</span>
          </button>

          <button
            onClick={onAbrirImportacaoCsv}
            title="Importar histórico do semestre via arquivo CSV"
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-300 hover:text-indigo-200 text-xs font-semibold transition-colors border border-indigo-500/30"
          >
            <FileSpreadsheet className="w-3.5 h-3.5" />
            <span>Importar CSV</span>
          </button>

          <button
            onClick={onNovaDisciplina}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold transition-all shadow-md shadow-indigo-600/20"
          >
            <Plus className="w-3.5 h-3.5" />
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
              className="bg-slate-900 text-slate-300 rounded-lg px-2.5 py-1.5 border border-slate-750 focus:outline-none focus:ring-1 focus:ring-indigo-500"
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
              <option value="REGULAR">Em Curso</option>
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
              <th className="py-3 px-3 text-center">1ª Etapa (25p)</th>
              <th className="py-3 px-3 text-center">2ª Etapa (30p)</th>
              <th className="py-3 px-3 text-center">3ª Etapa (45p)</th>
              <th className="py-3 px-3 text-center">Total (100p)</th>
              <th className="py-3 px-4">Frequência & Faltas</th>
              <th className="py-3 px-4">Metas e Projeções</th>
              <th className="py-3 px-4 text-center">Situação</th>
              <th className="py-3 px-4 text-right">Ações</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {filteredDisciplinas.length === 0 ? (
              <tr>
                <td colSpan={9} className="py-12 text-center text-slate-500">
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
                const e1 = disc.notaEtapa1 ?? disc.nota1;
                const e2 = disc.notaEtapa2 ?? disc.nota2;
                const e3 = disc.notaEtapa3;
                const total = disc.mediaAtual ?? ((e1 ?? 0) + (e2 ?? 0) + (e3 ?? 0));
                const freq = disc.percentualFrequencia ?? 100;
                const isNotaOk = total >= metaAprovacao;
                const isFreqOk = freq >= frequenciaExigida;

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

                    {/* Etapa 1 */}
                    <td className="py-3.5 px-3 text-center font-mono font-medium text-slate-200">
                      {e1 !== undefined && e1 !== null ? (
                        <span>{e1.toFixed(1)} <span className="text-[10px] text-slate-500">/25</span></span>
                      ) : (
                        <span className="text-slate-500 italic text-[11px]">Pendente</span>
                      )}
                    </td>

                    {/* Etapa 2 */}
                    <td className="py-3.5 px-3 text-center font-mono font-medium text-slate-200">
                      {e2 !== undefined && e2 !== null ? (
                        <span>{e2.toFixed(1)} <span className="text-[10px] text-slate-500">/30</span></span>
                      ) : (
                        <span className="text-slate-500 italic text-[11px]">Pendente</span>
                      )}
                    </td>

                    {/* Etapa 3 */}
                    <td className="py-3.5 px-3 text-center font-mono font-medium text-slate-200">
                      {e3 !== undefined && e3 !== null ? (
                        <span>{e3.toFixed(1)} <span className="text-[10px] text-slate-500">/45</span></span>
                      ) : (
                        <span className="text-slate-500 italic text-[11px]">Pendente</span>
                      )}
                    </td>

                    {/* Total Acumulado */}
                    <td className="py-3.5 px-3 text-center">
                      <span
                        className={`font-mono font-bold text-sm px-2.5 py-0.5 rounded-md ${isNotaOk
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                          : total >= 40
                            ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30'
                            : 'bg-indigo-500/10 text-indigo-300 border border-indigo-500/30'
                          }`}
                      >
                        {total.toFixed(1)}
                      </span>
                    </td>

                    {/* Frequência & Faltas */}
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-2">
                        <span
                          className={`font-semibold ${isFreqOk ? 'text-cyan-400' : 'text-rose-400'
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
                          className={`h-full rounded-full ${isFreqOk ? 'bg-cyan-500' : 'bg-rose-500'
                            }`}
                          style={{ width: `${Math.min(100, freq)}%` }}
                        />
                      </div>
                    </td>

                    {/* Projeções */}
                    <td className="py-3.5 px-4">
                      {disc.statusSituacao === 'APROVADO' ? (
                        <span className="text-emerald-400 font-medium">Meta de 60 pts alcançada</span>
                      ) : disc.statusSituacao === 'REPROVADO' ? (
                        <span className="text-rose-400 font-medium">Reprovação consolidada</span>
                      ) : (
                        <div className="space-y-0.5 text-[11px]">
                          {disc.notaNecessariaParaPassar !== undefined &&
                            disc.notaNecessariaParaPassar !== null &&
                            disc.notaNecessariaParaPassar > 0 && (
                              <div className="text-amber-300">
                                Faltam <strong className="font-semibold text-white">{disc.notaNecessariaParaPassar.toFixed(1)} pts</strong> para 60
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

                    {/* Ações */}
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => onEditarDisciplina(disc)}
                          title="Editar notas ou faltas"
                          className="p-1.5 rounded-lg text-slate-400 hover:text-indigo-400 hover:bg-slate-800 transition-colors"
                        >
                          <Edit2 className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => onExcluirDisciplina(disc.id!, disc.nomeDisciplina)}
                          title="Excluir disciplina"
                          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-800 transition-colors"
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
