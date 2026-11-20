import React from 'react';
import { GraduationCap, PlusCircle, FileSpreadsheet, ShieldAlert } from 'lucide-react';

interface EmptyStateProps {
  type: 'no-students' | 'no-disciplines';
  studentName?: string;
  onOpenNovoAluno?: () => void;
  onOpenNovaDisciplina?: () => void;
  onOpenImportCsv?: () => void;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  type,
  studentName,
  onOpenNovoAluno,
  onOpenNovaDisciplina,
  onOpenImportCsv,
}) => {
  if (type === 'no-students') {
    return (
      <div className="max-w-2xl mx-auto my-16 text-center px-4">
        <div className="glass-panel p-8 sm:p-12 rounded-3xl border border-slate-800 shadow-2xl relative overflow-hidden">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-indigo-600 to-indigo-400 text-white flex items-center justify-center mx-auto mb-6 shadow-xl shadow-indigo-600/30">
            <GraduationCap className="w-9 h-9" />
          </div>

          <span className="inline-block px-3 py-1 rounded-full text-xs font-semibold bg-indigo-500/10 text-indigo-400 border border-indigo-500/30 mb-3">
            Plataforma Data-Driven Limpa
          </span>

          <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight mb-3">
            Bem-vindo ao Sistema Pessoal de Monitoramento Acadêmico
          </h2>

          <p className="text-slate-400 text-sm max-w-lg mx-auto mb-8 leading-relaxed">
            Nossa plataforma opera exclusivamente com <strong>dados reais fornecidos por você</strong>, sem massas de dados fictícias.
            Comece cadastrando seu perfil universitário e configurando a média de aprovação da sua instituição.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
            <button
              onClick={onOpenNovoAluno}
              className="w-full sm:w-auto flex items-center justify-center gap-2 px-6 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-semibold shadow-lg shadow-indigo-600/30 transition-all active:scale-95"
            >
              <PlusCircle className="w-4 h-4" />
              <span>Cadastrar Meu Perfil Discente</span>
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-xl mx-auto my-12 text-center px-4">
      <div className="glass-panel p-8 rounded-2xl border border-slate-800">
        <div className="w-12 h-12 rounded-xl bg-slate-800 text-indigo-400 flex items-center justify-center mx-auto mb-4">
          <ShieldAlert className="w-6 h-6" />
        </div>

        <h3 className="text-lg font-bold text-white mb-2">
          Nenhuma disciplina registrada para {studentName || 'este aluno'}
        </h3>

        <p className="text-slate-400 text-xs max-w-md mx-auto mb-6">
          O cálculo do IRA, frequência global e análise preditiva de risco serão consolidados automaticamente após o lançamento da primeira matéria.
        </p>

        <div className="flex flex-wrap items-center justify-center gap-3">
          <button
            onClick={onOpenNovaDisciplina}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/20 transition-all active:scale-95"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Lançar Disciplina Manualmente</span>
          </button>

          <button
            onClick={onOpenImportCsv}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold border border-slate-700 transition-colors"
          >
            <FileSpreadsheet className="w-4 h-4 text-indigo-400" />
            <span>Importar Histórico via CSV</span>
          </button>
        </div>
      </div>
    </div>
  );
};
