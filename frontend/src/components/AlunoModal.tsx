import React, { useState, useEffect } from 'react';
import { AlunoDTO } from '../types/api';
import { X, UserCheck, AlertCircle, Trash2 } from 'lucide-react';

interface AlunoModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: AlunoDTO) => Promise<void>;
  onDelete?: (id: string) => Promise<void>;
  initialData?: AlunoDTO | null;
}

export const AlunoModal: React.FC<AlunoModalProps> = ({
  isOpen,
  onClose,
  onSave,
  onDelete,
  initialData,
}) => {
  const isEditing = !!initialData?.id;

  const [nome, setNome] = useState('');
  const [matricula, setMatricula] = useState('');
  const [curso, setCurso] = useState('');
  const [semestreAtual, setSemestreAtual] = useState<number>(1);
  const [mediaMinimaAprovacao, setMediaMinimaAprovacao] = useState<string>('6.0');
  const [frequenciaMinimaExigida, setFrequenciaMinimaExigida] = useState<string>('75.0');

  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  useEffect(() => {
    if (initialData) {
      setNome(initialData.nome);
      setMatricula(initialData.matricula);
      setCurso(initialData.curso);
      setSemestreAtual(initialData.semestreAtual);
      setMediaMinimaAprovacao(initialData.mediaMinimaAprovacao.toString());
      setFrequenciaMinimaExigida(
        initialData.frequenciaMinimaExigida !== undefined
          ? initialData.frequenciaMinimaExigida.toString()
          : '75.0'
      );
    } else {
      setNome('');
      setMatricula('');
      setCurso('');
      setSemestreAtual(1);
      setMediaMinimaAprovacao('6.0');
      setFrequenciaMinimaExigida('75.0');
    }
    setError(null);
    setConfirmDelete(false);
  }, [initialData, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const media = parseFloat(mediaMinimaAprovacao);
    const freq = parseFloat(frequenciaMinimaExigida);

    if (!nome.trim() || nome.trim().length < 2) {
      setError('O nome do estudante deve ter ao menos 2 caracteres.');
      return;
    }
    if (!matricula.trim() || matricula.trim().length < 3) {
      setError('A matrícula do estudante deve ter ao menos 3 caracteres.');
      return;
    }
    if (!curso.trim()) {
      setError('O curso de graduação é obrigatório.');
      return;
    }
    if (semestreAtual < 1 || semestreAtual > 20) {
      setError('O semestre atual deve estar entre 1 e 20.');
      return;
    }
    if (isNaN(media) || media <= 0 || media > 10) {
      setError('A média mínima de aprovação deve ser maior que 0.0 e no máximo 10.0.');
      return;
    }
    if (isNaN(freq) || freq < 0 || freq > 100) {
      setError('O percentual de frequência mínima exigida deve estar entre 0.0% e 100.0%.');
      return;
    }

    const payload: AlunoDTO = {
      ...(initialData?.id ? { id: initialData.id } : {}),
      nome: nome.trim(),
      matricula: matricula.trim(),
      curso: curso.trim(),
      semestreAtual,
      mediaMinimaAprovacao: media,
      frequenciaMinimaExigida: freq,
    };

    try {
      setIsSubmitting(true);
      await onSave(payload);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Erro ao salvar o perfil do discente.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!initialData?.id || !onDelete) return;
    try {
      setIsSubmitting(true);
      await onDelete(initialData.id);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Erro ao excluir o aluno.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fade-in">
      <div className="glass-panel w-full max-w-lg rounded-2xl shadow-2xl border border-slate-700/80 overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
              <UserCheck className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">
                {isEditing ? 'Parâmetros e Perfil do Aluno' : 'Cadastrar Estudante'}
              </h3>
              <p className="text-xs text-slate-400">
                Configure os critérios normativos da sua instituição
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

        {/* Body */}
        <form onSubmit={handleSubmit} className="p-5 overflow-y-auto space-y-4 text-xs">
          {error && (
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 flex items-start gap-2">
              <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Nome */}
          <div>
            <label className="block font-semibold text-slate-300 mb-1">
              Nome Completo *
            </label>
            <input
              type="text"
              required
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="ex.: Matheus Pereira da Silva"
              className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3.5 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
            />
          </div>

          {/* Grid: Matrícula & Curso */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Matrícula *
              </label>
              <input
                type="text"
                required
                value={matricula}
                onChange={(e) => setMatricula(e.target.value)}
                placeholder="ex.: 202410123"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3.5 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
            </div>

            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Curso de Graduação *
              </label>
              <input
                type="text"
                required
                value={curso}
                onChange={(e) => setCurso(e.target.value)}
                placeholder="ex.: Engenharia de Software"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3.5 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
            </div>
          </div>

          {/* Grid: Semestre Atual, Média Mínima, Frequência Mínima */}
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Semestre Atual *
              </label>
              <input
                type="number"
                min="1"
                max="20"
                required
                value={semestreAtual}
                onChange={(e) => setSemestreAtual(parseInt(e.target.value) || 1)}
                placeholder="1"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
            </div>

            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Média de Corte *
              </label>
              <input
                type="number"
                step="0.1"
                min="0.1"
                max="10"
                required
                value={mediaMinimaAprovacao}
                onChange={(e) => setMediaMinimaAprovacao(e.target.value)}
                placeholder="6.0 ou 7.0"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
            </div>

            <div>
              <label className="block font-semibold text-slate-300 mb-1">
                Freq. Mínima (%) *
              </label>
              <input
                type="number"
                step="1"
                min="0"
                max="100"
                required
                value={frequenciaMinimaExigida}
                onChange={(e) => setFrequenciaMinimaExigida(e.target.value)}
                placeholder="75.0"
                className="w-full bg-slate-950/70 border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none transition-colors"
              />
            </div>
          </div>

          {/* Delete Option for Editing Mode */}
          {isEditing && onDelete && (
            <div className="pt-2">
              {!confirmDelete ? (
                <button
                  type="button"
                  onClick={() => setConfirmDelete(true)}
                  className="flex items-center gap-1.5 text-xs text-rose-400 hover:text-rose-300 transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  <span>Excluir este perfil e todas as disciplinas vinculadas</span>
                </button>
              ) : (
                <div className="p-3 rounded-xl bg-rose-950/50 border border-rose-600/50 flex items-center justify-between">
                  <span className="text-xs text-rose-200 font-semibold">
                    Tem certeza? Todos os dados serão perdidos.
                  </span>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => setConfirmDelete(false)}
                      className="px-2.5 py-1 rounded-lg bg-slate-800 text-slate-300 text-xs"
                    >
                      Cancelar
                    </button>
                    <button
                      type="button"
                      onClick={handleDelete}
                      disabled={isSubmitting}
                      className="px-2.5 py-1 rounded-lg bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold"
                    >
                      Confirmar
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Actions */}
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
              {isSubmitting ? 'Salvando...' : isEditing ? 'Salvar Alterações' : 'Criar Perfil Discente'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
