import React from 'react';
import { AlunoDTO } from '../types/api';
import { GraduationCap, Users, PlusCircle, Settings2, RefreshCw } from 'lucide-react';

interface NavbarProps {
  alunos: AlunoDTO[];
  currentAluno: AlunoDTO | null;
  onSelectAluno: (id: string) => void;
  onOpenNovoAlunoModal: () => void;
  onOpenEditarAlunoModal: () => void;
  onRefresh: () => void;
  isLoading: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({
  alunos,
  currentAluno,
  onSelectAluno,
  onOpenNovoAlunoModal,
  onOpenEditarAlunoModal,
  onRefresh,
  isLoading,
}) => {
  return (
    <header className="sticky top-0 z-30 bg-slate-900/80 backdrop-blur-md border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        {/* Brand / Logo */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-indigo-400 flex items-center justify-center shadow-lg shadow-indigo-500/20 text-white">
            <GraduationCap className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-lg tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
                Data-Driven Monitor
              </span>
              <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                Data-Driven
              </span>
            </div>
            <p className="text-xs text-slate-400 hidden sm:block">
              Desempenho, Retenção & Análise Preditiva de Risco
            </p>
          </div>
        </div>

        {/* Right side controls */}
        <div className="flex items-center gap-3">
          {/* Student Selector */}
          {alunos.length > 0 && (
            <div className="flex items-center gap-2">
              <div className="relative">
                <select
                  value={currentAluno?.id || ''}
                  onChange={(e) => onSelectAluno(e.target.value)}
                  className="bg-slate-800/90 text-slate-200 text-sm rounded-lg px-3 py-1.5 pr-8 border border-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all cursor-pointer appearance-none"
                >
                  {alunos.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.nome} ({a.matricula})
                    </option>
                  ))}
                </select>
                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-slate-400">
                  <Users className="w-4 h-4" />
                </div>
              </div>

              {currentAluno && (
                <button
                  onClick={onOpenEditarAlunoModal}
                  title="Configurar critérios e perfil institucional"
                  className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 transition-colors"
                >
                  <Settings2 className="w-4 h-4" />
                </button>
              )}
            </div>
          )}

          {/* New Student Button */}
          <button
            onClick={onOpenNovoAlunoModal}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-medium transition-all shadow-md shadow-indigo-600/20 active:scale-95"
          >
            <PlusCircle className="w-4 h-4" />
            <span className="hidden sm:inline">Novo Aluno</span>
          </button>

          {/* Refresh Data */}
          {currentAluno && (
            <button
              onClick={onRefresh}
              disabled={isLoading}
              title="Recarregar dados"
              className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 transition-all disabled:opacity-50"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin text-indigo-400' : ''}`} />
            </button>
          )}
        </div>
      </div>
    </header>
  );
};
