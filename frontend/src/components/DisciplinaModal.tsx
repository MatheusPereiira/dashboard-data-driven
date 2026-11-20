import React, { useState, useEffect } from 'react';
import { DisciplinaDesempenhoDTO } from '../types/api';
import { X, BookOpen, AlertCircle, Calculator } from 'lucide-react';

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
  const [totalAulasSemestre, setTotalAulasSemestre] = useState<number>(60);
  const [quantidadeFaltas, setQuantidadeFaltas] = useState<number>(0);
  const [nota1, setNota1] = useState<string>('');
  const [nota2, setNota2] = useState<string>('');

  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (initialData) {
      setNomeDisciplina(initialData.nomeDisciplina);
      setSemestreLetivo(initialData.semestreLetivo);
      setTotalAulasSemestre(initialData.totalAulasSemestre);
      setQuantidadeFaltas(initialData.quantidadeFaltas);
      setNota1(initialData.nota1.toString());
      setNota2(initialData.nota2 !== null && initialData.nota2 !== undefined ? initialData.nota2.toString() : '');
    } else {
      setNomeDisciplina('');
      setSemestreLetivo('2026.1');
      setTotalAulasSemestre(60);
      setQuantidadeFaltas(0);
      setNota1('');
      setNota2('');
    }
    setError(null);
  }, [initialData, isOpen]);

  if (!isOpen) return null;

  // Real-time calculation previews
  const n1Val = parseFloat(nota1);
  const n2Val = nota2 !== '' ? parseFloat(nota2) : null;
  const isN1Valid = !isNaN(n1Val) && n1Val >= 0 && n1Val <= 10;
  const isN2Valid = n2Val === null || (!isNaN(n2Val) && n2Val >= 0 && n2Val <= 10);

  let previewMedia: number | null = null;
  if (isN1Valid) {
    if (n2Val !== null && !isNaN(n2Val)) {
      previewMedia = (n1Val + n2Val) / 2;
    } else {
      previewMedia = n1Val;
    }
  }

  let previewFreq: number | null = null;
  if (totalAulasSemestre > 0 && quantidadeFaltas >= 0) {
    previewFreq = Math.max(0, ((totalAulasSemestre - quantidadeFaltas) / totalAulasSemestre) * 100);
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Validation
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
    if (isNaN(n1Val) || n1Val < 0 || n1Val > 10) {
      setError('A Nota 1 deve ser um número entre 0.0 e 10.0.');
      return;
    }
    if (nota2 !== '' && (n2Val === null || isNaN(n2Val) || n2Val < 0 || n2Val > 10)) {
      setError('A Nota 2 deve ser um número entre 0.0 e 10.0 ou ser deixada vazia caso pendente.');
      return;
    }

    const payload: DisciplinaDesempenhoDTO = {
      ...(initialData?.id ? { id: initialData.id } : {}),
      alunoId,
      nomeDisciplina: nomeDisciplina.trim(),
      semestreLetivo: semestreLetivo.trim(),
      totalAulasSemestre,
      quantidadeFaltas,
      nota1: n1Val,
      nota2: n2Val,
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
      <div className="glass-panel w-full max-w-lg rounded-2xl shadow-2xl border border-slate-700/80 overflow-hidden flex flex-col max-h-[90vh]">
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
                {isEditing
                  ? 'Atualize notas parciais ou faltas acumuladas'
                  : 'Cadastre o componente curricular com dados reais'}
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
              placeholder="ex.: Estrutura de Dados, Cálculo II, Banco de Dados..."
              className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3.5 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
            />
          </div>

          {/* Grid 2 cols: Semestre Letivo e Total de Aulas */}
          <div className="grid grid-cols-2 gap-3">
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
            </div>

            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Total de Aulas (Semestre) *
              </label>
              <input
                type="number"
                min="1"
                required
                value={totalAulasSemestre}
                onChange={(e) => setTotalAulasSemestre(parseInt(e.target.value) || 0)}
                placeholder="ex.: 60, 80"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
            </div>
          </div>

          {/* Grid 3 cols: Faltas, Nota 1, Nota 2 */}
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Faltas Acumuladas *
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
            </div>

            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Nota 1 (0.0 a 10.0) *
              </label>
              <input
                type="number"
                step="0.1"
                min="0"
                max="10"
                required
                value={nota1}
                onChange={(e) => setNota1(e.target.value)}
                placeholder="ex.: 7.5"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
            </div>

            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Nota 2 (Opcional)
              </label>
              <input
                type="number"
                step="0.1"
                min="0"
                max="10"
                value={nota2}
                onChange={(e) => setNota2(e.target.value)}
                placeholder="Pendente"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
            </div>
          </div>

          {/* Real-time Calculation Card Preview */}
          <div className="p-3.5 rounded-xl bg-slate-900/90 border border-slate-800 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Calculator className="w-4 h-4 text-indigo-400" />
              <span className="font-semibold text-slate-300">Simulação Imediata:</span>
            </div>
            <div className="flex items-center gap-4 text-xs font-mono">
              <div>
                <span className="text-slate-400">Média: </span>
                <span
                  className={`font-bold ${
                    previewMedia !== null && previewMedia >= mediaCorte
                      ? 'text-emerald-400'
                      : 'text-amber-400'
                  }`}
                >
                  {previewMedia !== null ? previewMedia.toFixed(1) : '--'}
                </span>
              </div>
              <div>
                <span className="text-slate-400">Frequência: </span>
                <span
                  className={`font-bold ${
                    previewFreq !== null && previewFreq >= 75 ? 'text-cyan-400' : 'text-rose-400'
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
