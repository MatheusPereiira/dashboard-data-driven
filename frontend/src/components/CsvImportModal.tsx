import React, { useState, useRef } from 'react';
import { BatchImportReportDTO } from '../types/api';
import {
  X,
  UploadCloud,
  FileSpreadsheet,
  Download,
  CheckCircle2,
  AlertTriangle,
  AlertCircle,
  Loader2,
} from 'lucide-react';

interface CsvImportModalProps {
  isOpen: boolean;
  onClose: () => void;
  onImport: (file: File) => Promise<BatchImportReportDTO>;
  onDownloadTemplate: () => void;
}

export const CsvImportModal: React.FC<CsvImportModalProps> = ({
  isOpen,
  onClose,
  onImport,
  onDownloadTemplate,
}) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [report, setReport] = useState<BatchImportReportDTO | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const handleClose = () => {
    setSelectedFile(null);
    setError(null);
    setReport(null);
    onClose();
  };

  const validateAndSetFile = (file: File) => {
    setError(null);
    setReport(null);

    if (!file.name.toLowerCase().endsWith('.csv')) {
      setError('Apenas arquivos no formato .csv são permitidos.');
      return;
    }
    // 5MB limit
    if (file.size > 5 * 1024 * 1024) {
      setError('O tamanho do arquivo excede o limite máximo permitido de 5MB.');
      return;
    }

    setSelectedFile(file);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      validateAndSetFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      validateAndSetFile(e.target.files[0]);
    }
  };

  const handleSubmit = async () => {
    if (!selectedFile) return;
    try {
      setIsUploading(true);
      setError(null);
      const res = await onImport(selectedFile);
      setReport(res);
    } catch (err: any) {
      setError(err.message || 'Falha ao processar a importação em lote.');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fade-in">
      <div className="glass-panel w-full max-w-lg rounded-2xl shadow-2xl border border-slate-700/80 overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">Importação em Lote via CSV</h3>
              <p className="text-xs text-slate-400">
                Padrão FANS: nomeDisciplina, semestreLetivo, totalAulasSemestre, quantidadeFaltas, notaEtapa1, notaEtapa2, notaEtapa3
              </p>
            </div>
          </div>
          <button
            onClick={handleClose}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body */}
        <div className="p-5 overflow-y-auto space-y-4 text-xs">
          {error && (
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 flex items-start gap-2">
              <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Instructions & Template download */}
          <div className="p-3.5 rounded-xl bg-slate-900/80 border border-slate-800 flex items-center justify-between gap-3">
            <div>
              <p className="font-semibold text-slate-200">Precisa da planilha padrão?</p>
              <p className="text-slate-400 text-[11px] mt-0.5">
                Baixe o template pré-formatado com as colunas das 3 etapas.
              </p>
            </div>
            <button
              type="button"
              onClick={onDownloadTemplate}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-300 text-xs font-semibold border border-indigo-500/30 transition-colors flex-shrink-0"
            >
              <Download className="w-3.5 h-3.5" />
              <span>Baixar Modelo</span>
            </button>
          </div>

          {/* Report if already executed */}
          {report ? (
            <div className="space-y-3 p-4 rounded-xl bg-slate-900/90 border border-slate-800">
              <div className="flex items-center gap-2 text-emerald-400 font-bold text-sm">
                <CheckCircle2 className="w-5 h-5" />
                Processamento do Spring Batch Concluído!
              </div>

              <div className="grid grid-cols-3 gap-2 text-center py-2">
                <div className="p-2.5 rounded-lg bg-slate-800/80 border border-slate-700/60">
                  <div className="text-slate-400 text-[10px] uppercase font-bold">Lidos</div>
                  <div className="text-lg font-bold text-white mt-0.5">{report.totalLidos}</div>
                </div>
                <div className="p-2.5 rounded-lg bg-emerald-950/40 border border-emerald-500/30">
                  <div className="text-emerald-400 text-[10px] uppercase font-bold">Importados</div>
                  <div className="text-lg font-bold text-emerald-300 mt-0.5">{report.totalImportados}</div>
                </div>
                <div className="p-2.5 rounded-lg bg-rose-950/40 border border-rose-500/30">
                  <div className="text-rose-400 text-[10px] uppercase font-bold">Rejeitados</div>
                  <div className="text-lg font-bold text-rose-300 mt-0.5">{report.totalRejeitados}</div>
                </div>
              </div>

              {report.avisos && report.avisos.length > 0 && (
                <div className="p-3 rounded-lg bg-amber-950/30 border border-amber-500/30 text-amber-300 text-[11px] space-y-1">
                  <div className="flex items-center gap-1.5 font-bold">
                    <AlertTriangle className="w-3.5 h-3.5" /> Avisos de Importação:
                  </div>
                  <ul className="list-disc list-inside space-y-0.5">
                    {report.avisos.map((aviso, idx) => (
                      <li key={idx}>{aviso}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ) : (
            /* Dropzone */
            <div
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
              onClick={() => fileInputRef.current?.click()}
              className={`border-2 border-dashed rounded-2xl p-8 text-center cursor-pointer transition-all flex flex-col items-center justify-center gap-2 ${
                isDragging
                  ? 'border-indigo-400 bg-indigo-500/10'
                  : selectedFile
                  ? 'border-emerald-500/50 bg-emerald-500/5'
                  : 'border-slate-700 hover:border-slate-600 bg-slate-900/40 hover:bg-slate-900/70'
              }`}
            >
              <input
                ref={fileInputRef}
                type="file"
                accept=".csv"
                onChange={handleFileChange}
                className="hidden"
              />

              <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center mb-1">
                <UploadCloud className="w-6 h-6" />
              </div>

              {selectedFile ? (
                <div>
                  <p className="font-semibold text-white text-sm">
                    {selectedFile.name}
                  </p>
                  <p className="text-slate-400 text-[11px] mt-0.5">
                    {(selectedFile.size / 1024).toFixed(1)} KB — Clique para substituir
                  </p>
                </div>
              ) : (
                <div>
                  <p className="font-semibold text-white text-sm">
                    Arraste e solte o arquivo .CSV aqui
                  </p>
                  <p className="text-slate-400 text-[11px] mt-0.5">
                    ou clique para procurar no seu computador (Máx: 5MB)
                  </p>
                </div>
              )}
            </div>
          )}

          {/* Security Note */}
          <p className="text-[10px] text-slate-500 leading-relaxed text-center">
            O backend possui proteção contra formula injection e validação estrita de integridade para cada linha do CSV.
          </p>

          {/* Actions */}
          <div className="pt-3 flex items-center justify-end gap-2.5 border-t border-slate-800">
            <button
              type="button"
              onClick={handleClose}
              disabled={isUploading}
              className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium transition-colors"
            >
              {report ? 'Fechar' : 'Cancelar'}
            </button>
            {!report && (
              <button
                type="button"
                onClick={handleSubmit}
                disabled={!selectedFile || isUploading}
                className="flex items-center gap-2 px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/30 transition-all disabled:opacity-50"
              >
                {isUploading ? (
                  <>
                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    <span>Processando Spring Batch...</span>
                  </>
                ) : (
                  <span>Iniciar Importação</span>
                )}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
