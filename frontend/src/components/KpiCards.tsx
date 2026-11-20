import React from 'react';
import { DashboardPerformanceDTO, ClassificacaoRisco } from '../types/api';
import { Award, Percent, AlertOctagon, BookOpen, TrendingUp, AlertTriangle } from 'lucide-react';

interface KpiCardsProps {
  dashboard: DashboardPerformanceDTO;
}

export const KpiCards: React.FC<KpiCardsProps> = ({ dashboard }) => {
  const { aluno, analisePreditiva, totalDisciplinasMatriculadas, totalDisciplinasEmAlerta } = dashboard;

  const ira = analisePreditiva?.indiceRendimentoAcademico ?? 0;
  const frequencia = analisePreditiva?.frequenciaGlobal ?? 100;
  const scoreRisco = analisePreditiva?.scoreRiscoEvasaoReprovacao ?? 0;
  const classificacao: ClassificacaoRisco = analisePreditiva?.classificacaoRisco ?? 'BAIXO';

  // Semantic styles for Risk Classification
  const getRiskConfig = (risk: ClassificacaoRisco) => {
    switch (risk) {
      case 'CRITICO':
        return {
          bg: 'bg-rose-500/10 border-rose-500/30 text-rose-400',
          badgeBg: 'bg-rose-500 text-white',
          barColor: 'bg-rose-500',
          label: 'Crítico',
          pulse: 'pulse-critical ring-2 ring-rose-500/50',
        };
      case 'ALTO':
        return {
          bg: 'bg-orange-500/10 border-orange-500/30 text-orange-400',
          badgeBg: 'bg-orange-500 text-white',
          barColor: 'bg-orange-500',
          label: 'Alto Risco',
          pulse: '',
        };
      case 'MODERADO':
        return {
          bg: 'bg-amber-500/10 border-amber-500/30 text-amber-400',
          badgeBg: 'bg-amber-500 text-slate-900 font-semibold',
          barColor: 'bg-amber-500',
          label: 'Moderado',
          pulse: '',
        };
      case 'BAIXO':
      default:
        return {
          bg: 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400',
          badgeBg: 'bg-emerald-500 text-white',
          barColor: 'bg-emerald-500',
          label: 'Baixo Risco',
          pulse: '',
        };
    }
  };

  const riskConfig = getRiskConfig(classificacao);
  const mediaCorte = aluno.mediaMinimaAprovacao ?? 6.0;
  const freqMinima = aluno.frequenciaMinimaExigida ?? 75.0;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {/* KPI 1: IRA (Índice de Rendimento Acadêmico) */}
      <div className="glass-panel glass-panel-hover rounded-2xl p-5 relative overflow-hidden flex flex-col justify-between">
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold tracking-wider uppercase text-slate-400">
            Índice de Rendimento (IRA)
          </span>
          <div className="w-9 h-9 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
            <Award className="w-5 h-5" />
          </div>
        </div>

        <div className="my-3">
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold tracking-tight text-white">
              {ira.toFixed(1)}
            </span>
            <span className="text-xs text-slate-400">/ 10.0</span>
          </div>
          <div className="flex items-center gap-1.5 mt-1 text-xs">
            {ira >= mediaCorte ? (
              <span className="text-emerald-400 flex items-center gap-1">
                <TrendingUp className="w-3.5 h-3.5" />
                Acima do corte ({mediaCorte.toFixed(1)})
              </span>
            ) : (
              <span className="text-amber-400 flex items-center gap-1">
                <AlertTriangle className="w-3.5 h-3.5" />
                Abaixo do corte ({mediaCorte.toFixed(1)})
              </span>
            )}
          </div>
        </div>

        {/* Mini progress bar against 10.0 */}
        <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-500 ${
              ira >= mediaCorte ? 'bg-indigo-500' : 'bg-amber-500'
            }`}
            style={{ width: `${Math.min(100, Math.max(0, ira * 10))}%` }}
          />
        </div>
      </div>

      {/* KPI 2: Frequência Global */}
      <div className="glass-panel glass-panel-hover rounded-2xl p-5 relative overflow-hidden flex flex-col justify-between">
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold tracking-wider uppercase text-slate-400">
            Frequência Global
          </span>
          <div className="w-9 h-9 rounded-xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400">
            <Percent className="w-5 h-5" />
          </div>
        </div>

        <div className="my-3">
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold tracking-tight text-white">
              {frequencia.toFixed(1)}%
            </span>
          </div>
          <div className="flex items-center gap-1.5 mt-1 text-xs">
            {frequencia >= freqMinima ? (
              <span className="text-emerald-400">
                Assíduo (Mínimo institucional: {freqMinima}%)
              </span>
            ) : (
              <span className="text-rose-400 font-medium">
                Risco de Reprovação (Exigido: {freqMinima}%)
              </span>
            )}
          </div>
        </div>

        <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-500 ${
              frequencia >= freqMinima ? 'bg-cyan-500' : 'bg-rose-500'
            }`}
            style={{ width: `${Math.min(100, Math.max(0, frequencia))}%` }}
          />
        </div>
      </div>

      {/* KPI 3: Score Preditivo de Risco */}
      <div className={`glass-panel glass-panel-hover rounded-2xl p-5 relative overflow-hidden flex flex-col justify-between ${riskConfig.pulse}`}>
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold tracking-wider uppercase text-slate-400">
            Risco Preditivo (IA)
          </span>
          <div className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider ${riskConfig.badgeBg}`}>
            {riskConfig.label}
          </div>
        </div>

        <div className="my-3">
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold tracking-tight text-white">
              {scoreRisco.toFixed(1)}%
            </span>
            <span className="text-xs text-slate-400">probabilidade</span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Risco de retenção ou evasão escolar
          </p>
        </div>

        <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-500 ${riskConfig.barColor}`}
            style={{ width: `${Math.min(100, Math.max(0, scoreRisco))}%` }}
          />
        </div>
      </div>

      {/* KPI 4: Disciplinas em Alerta */}
      <div className="glass-panel glass-panel-hover rounded-2xl p-5 relative overflow-hidden flex flex-col justify-between">
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold tracking-wider uppercase text-slate-400">
            Disciplinas em Alerta
          </span>
          <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${
            totalDisciplinasEmAlerta > 0
              ? 'bg-amber-500/10 border border-amber-500/20 text-amber-400'
              : 'bg-emerald-500/10 border border-emerald-500/20 text-emerald-400'
          }`}>
            {totalDisciplinasEmAlerta > 0 ? (
              <AlertOctagon className="w-5 h-5" />
            ) : (
              <BookOpen className="w-5 h-5" />
            )}
          </div>
        </div>

        <div className="my-3">
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold tracking-tight text-white">
              {totalDisciplinasEmAlerta}
            </span>
            <span className="text-xs text-slate-400">
              de {totalDisciplinasMatriculadas} cursando
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            {totalDisciplinasEmAlerta === 0
              ? 'Todas as matérias regulares ou aprovadas'
              : `${totalDisciplinasEmAlerta} disciplina(s) requerem atenção urgente`}
          </p>
        </div>

        <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-500 ${
              totalDisciplinasEmAlerta > 0 ? 'bg-amber-500' : 'bg-emerald-500'
            }`}
            style={{
              width: totalDisciplinasMatriculadas > 0
                ? `${Math.min(100, (totalDisciplinasEmAlerta / totalDisciplinasMatriculadas) * 100)}%`
                : '0%',
            }}
          />
        </div>
      </div>
    </div>
  );
};
