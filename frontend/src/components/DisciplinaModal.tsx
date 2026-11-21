import React, { useState, useEffect } from 'react';
import { DisciplinaDesempenhoDTO } from '../types/api';
import { X, BookOpen, AlertCircle, Calculator, Info } from 'lucide-react';

interface DisciplinaModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: DisciplinaDesempenhoDTO) => Promise<void>;
  alunoId: string;
  initialData?: DisciplinaDesempenhoDTO | null;
  mediaCorte: number;
}

export const DisciplinaModal: React.FC<DisciplinaModalProps> = ({
  isOpen,
  onClose,
  onSave,
  alunoId,
  initialData,
  mediaCorte,
}) => {
  const isEditing = !!initialData?.id;

  const [nomeDisciplina, setNomeDisciplina] = useState('');
  const [semestreLetivo, setSemestreLetivo] = useState('2026.1');
  const [totalAulasSemestre, setTotalAulasSemestre] = useState<number>(80);
  const [quantidadeFaltas, setQuantidadeFaltas] = useState<number>(0);
  const [notaEtapa1, setNotaEtapa1] = useState<string>('');
  const [notaEtapa2, setNotaEtapa2] = useState<string>('');
  const [notaEtapa3, setNotaEtapa3] = useState<string>('');

  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (initialData) {
      setNomeDisciplina(initialData.nomeDisciplina);
      setSemestreLetivo(initialData.semestreLetivo);
      setTotalAulasSemestre(initialData.totalAulasSemestre);
      setQuantidadeFaltas(initialData.quantidadeFaltas);
      setNotaEtapa1(
        initialData.notaEtapa1 !== null && initialData.notaEtapa1 !== undefined
          ? initialData.notaEtapa1.toString()
          : initialData.nota1 !== undefined && initialData.nota1 !== null
          ? initialData.nota1.toString()
          : ''
      );
      setNotaEtapa2(
        initialData.notaEtapa2 !== null && initialData.notaEtapa2 !== undefined
          ? initialData.notaEtapa2.toString()
          : initialData.nota2 !== undefined && initialData.nota2 !== null
          ? initialData.nota2.toString()
          : ''
      );
      setNotaEtapa3(
        initialData.notaEtapa3 !== null && initialData.notaEtapa3 !== undefined
          ? initialData.notaEtapa3.toString()
          : ''
      );
    } else {
      setNomeDisciplina('');
      setSemestreLetivo('2026.1');
      setTotalAulasSemestre(80);
      setQuantidadeFaltas(0);
      setNotaEtapa1('');
      setNotaEtapa2('');
      setNotaEtapa3('');
    }
    setError(null);
  }, [initialData, isOpen]);

  if (!isOpen) return null;

  // Real-time calculation previews
  const e1Val = notaEtapa1 !== '' ? parseFloat(notaEtapa1) : null;
  const e2Val = notaEtapa2 !== '' ? parseFloat(notaEtapa2) : null;
  const e3Val = notaEtapa3 !== '' ? parseFloat(notaEtapa3) : null;

  const isE1Valid = e1Val === null || (!isNaN(e1Val) && e1Val >= 0 && e1Val <= 25);
  const isE2Valid = e2Val === null || (!isNaN(e2Val) && e2Val >= 0 && e2Val <= 30);
  const isE3Valid = e3Val === null || (!isNaN(e3Val) && e3Val >= 0 && e3Val <= 45);

  let pontosAcumulados = 0;
  let etapasLancadas = 0;
  if (e1Val !== null && !isNaN(e1Val)) {
    pontosAcumulados += e1Val;
    etapasLancadas++;
  }
  if (e2Val !== null && !isNaN(e2Val)) {
    pontosAcumulados += e2Val;
    etapasLancadas++;
  }
  if (e3Val !== null && !isNaN(e3Val)) {
    pontosAcumulados += e3Val;
    etapasLancadas++;
  }

  const metaFinal = mediaCorte <= 10.0 ? mediaCorte * 10 : mediaCorte; // 60 pts
  const pontosRestantesParaMeta = Math.max(0, metaFinal - pontosAcumulados);

  let previewFreq: number | null = null;
  if (totalAulasSemestre > 0 && quantidadeFaltas >= 0) {
    previewFreq = Math.max(0, ((totalAulasSemestre - quantidadeFaltas) / totalAulasSemestre) * 100);
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!nomeDisciplina.trim()) {
      setError('O nome da disciplina é obrigatório.');
      return;
    }
    if (!semestreLetivo.trim()) {
      setError('O semestre letivo é obrigatório (ex.: 2026.1).');
      return;
    }
    if (totalAulasSemestre <= 0) {
      setError('O total de aulas deve ser maior que zero.');
      return;
    }
    if (quantidadeFaltas < 0) {
      setError('A quantidade de faltas não pode ser negativa.');
      return;
    }
    if (quantidadeFaltas > totalAulasSemestre) {
      setError('A quantidade de faltas não pode exceder o total de aulas do semestre.');
      return;
    }
    if (e1Val !== null && (isNaN(e1Val) || e1Val < 0 || e1Val > 25)) {
      setError('A pontuação da Etapa 1 deve ser um número entre 0.0 e 25.0 pontos.');
      return;
    }
    if (e2Val !== null && (isNaN(e2Val) || e2Val < 0 || e2Val > 30)) {
      setError('A pontuação da Etapa 2 deve ser um número entre 0.0 e 30.0 pontos.');
      return;
    }
    if (e3Val !== null && (isNaN(e3Val) || e3Val < 0 || e3Val > 45)) {
      setError('A pontuação da Etapa 3 deve ser um número entre 0.0 e 45.0 pontos.');
      return;
    }

    const payload: DisciplinaDesempenhoDTO = {
      ...(initialData?.id ? { id: initialData.id } : {}),
      alunoId,
      nomeDisciplina: nomeDisciplina.trim(),
      semestreLetivo: semestreLetivo.trim(),
      totalAulasSemestre,
      quantidadeFaltas,
      notaEtapa1: e1Val,
      notaEtapa2: e2Val,
      notaEtapa3: e3Val,
      nota1: e1Val ?? 0,
      nota2: e2Val,
    };

    try {
      setIsSubmitting(true);
      await onSave(payload);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Erro ao salvar a disciplina.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fade-in">
      <div className="glass-panel w-full max-w-xl rounded-2xl shadow-2xl border border-slate-700/80 overflow-hidden flex flex-col max-h-[92vh]">
        {/* Modal Header */}
        <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
              <BookOpen className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">
                {isEditing ? 'Editar Disciplina' : 'Lançar Nova Disciplina'}
              </h3>
              <p className="text-xs text-slate-400">
                Padrão institucional: 3 etapas (100 pontos no total) • 60 pts para passar
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <form onSubmit={handleSubmit} className="p-5 overflow-y-auto space-y-4 text-xs">
          {error && (
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 flex items-start gap-2">
              <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Nome da Disciplina */}
          <div>
            <label className="block font-semibold text-slate-300 mb-1">
              Nome da Disciplina *
            </label>
            <input
              type="text"
              required
              value={nomeDisciplina}
              onChange={(e) => setNomeDisciplina(e.target.value)}
              placeholder="ex.: Estrutura de Dados, Banco de Dados, Redes de Computadores..."
              className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3.5 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
            />
          </div>

          {/* Grid 3 cols: Semestre Letivo, Carga Horária, Faltas */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Semestre Letivo *
              </label>
              <input
                type="text"
                required
                value={semestreLetivo}
                onChange={(e) => setSemestreLetivo(e.target.value)}
                placeholder="ex.: 2026.1"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
              <span className="block text-[10px] text-slate-500 mt-1">
                Período em curso
              </span>
            </div>

            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Carga Horária (Aulas) *
              </label>
              <input
                type="number"
                min="1"
                required
                value={totalAulasSemestre}
                onChange={(e) => setTotalAulasSemestre(parseInt(e.target.value) || 0)}
                placeholder="ex.: 80 ou 40"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
              <span className="block text-[10px] text-slate-500 mt-1">
                Geralmente 80h ou 40h
              </span>
            </div>

            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Horas / Aulas Faltadas *
              </label>
              <input
                type="number"
                min="0"
                max={totalAulasSemestre}
                required
                value={quantidadeFaltas}
                onChange={(e) => setQuantidadeFaltas(parseInt(e.target.value) || 0)}
                placeholder="0"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
              <span className="block text-[10px] text-slate-500 mt-1">
                Máx 20% ausências permitidas
              </span>
            </div>
          </div>

          {/* Seção das 3 Etapas Institucionais */}
          <div className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800 space-y-3">
            <div className="flex items-center gap-1.5 text-indigo-300 font-semibold text-xs">
              <Info className="w-3.5 h-3.5" />
              <span>Pontuação por Etapa (Total: 100 pontos no período)</span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {/* Etapa 1 */}
              <div>
                <label className="block font-semibold text-slate-300 mb-1">
                  1ª Etapa (0 a 25 pts)
                </label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  max="25"
                  value={notaEtapa1}
                  onChange={(e) => setNotaEtapa1(e.target.value)}
                  placeholder="Pendente"
                  className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
                />
                <span className="block text-[10px] text-slate-400 mt-1 leading-snug">
                  15p prova + 5p sala + 5p interdisciplinar
                </span>
              </div>

              {/* Etapa 2 */}
              <div>
                <label className="block font-semibold text-slate-300 mb-1">
                  2ª Etapa (0 a 30 pts)
                </label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  max="30"
                  value={notaEtapa2}
                  onChange={(e) => setNotaEtapa2(e.target.value)}
                  placeholder="Pendente"
                  className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
                />
                <span className="block text-[10px] text-slate-400 mt-1 leading-snug">
                  15p prova + 10p interdisc. + 5p sala
                </span>
              </div>

              {/* Etapa 3 */}
              <div>
                <label className="block font-semibold text-slate-300 mb-1">
                  3ª Etapa (0 a 45 pts)
                </label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  max="45"
                  value={notaEtapa3}
                  onChange={(e) => setNotaEtapa3(e.target.value)}
                  placeholder="Pendente"
                  className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
                />
                <span className="block text-[10px] text-slate-400 mt-1 leading-snug">
                  25p prova + 10p sala + 10p Prova F
                </span>
              </div>
            </div>
          </div>

          {/* Real-time Calculation Card Preview */}
          <div className="p-3.5 rounded-xl bg-slate-900/90 border border-slate-800 flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <Calculator className="w-4 h-4 text-indigo-400" />
              <span className="font-semibold text-slate-300">Simulação Imediata:</span>
            </div>
            <div className="flex items-center gap-4 text-xs font-mono">
              <div>
                <span className="text-slate-400">Acumulado: </span>
                <span
                  className={`font-bold ${
                    pontosAcumulados >= metaFinal
                      ? 'text-emerald-400'
                      : pontosAcumulados >= 40
                      ? 'text-amber-400'
                      : 'text-indigo-300'
                  }`}
                >
                  {pontosAcumulados.toFixed(1)} / 100 pts
                </span>
              </div>
              <div>
                <span className="text-slate-400">
                  {pontosRestantesParaMeta > 0 ? 'Faltam: ' : 'Situação: '}
                </span>
                <span
                  className={`font-bold ${
                    pontosRestantesParaMeta === 0 ? 'text-emerald-400' : 'text-amber-300'
                  }`}
                >
                  {pontosRestantesParaMeta === 0
                    ? 'Aprovado na Nota!'
                    : `${pontosRestantesParaMeta.toFixed(1)} pts`}
                </span>
              </div>
              <div>
                <span className="text-slate-400">Presença: </span>
                <span
                  className={`font-bold ${
                    previewFreq !== null && previewFreq >= 80 ? 'text-cyan-400' : 'text-rose-400'
                  }`}
                >
                  {previewFreq !== null ? `${previewFreq.toFixed(1)}%` : '--'}
                </span>
              </div>
            </div>
          </div>

          {/* Modal Actions */}
          <div className="pt-3 flex items-center justify-end gap-2.5 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-medium shadow-md shadow-indigo-600/30 transition-all disabled:opacity-50"
            >
              {isSubmitting ? 'Salvando...' : isEditing ? 'Atualizar Dados' : 'Cadastrar Disciplina'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
