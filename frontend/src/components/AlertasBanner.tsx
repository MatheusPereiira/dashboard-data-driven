import React from 'react';
import { Sparkles, AlertCircle, CheckCircle2 } from 'lucide-react';

interface AlertasBannerProps {
  alertas: string[];
}

export const AlertasBanner: React.FC<AlertasBannerProps> = ({ alertas }) => {
  if (!alertas || alertas.length === 0) {
    return (
      <div className="glass-panel rounded-2xl p-4 flex items-center gap-3 border-emerald-500/20 bg-emerald-950/20">
        <div className="w-8 h-8 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center flex-shrink-0">
          <CheckCircle2 className="w-5 h-5" />
        </div>
        <div>
          <h4 className="text-sm font-semibold text-emerald-300">
            Nenhum alerta crítico no momento
          </h4>
          <p className="text-xs text-slate-400">
            Todas as métricas de frequência e rendimento estão dentro da margem de segurança institucional.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="glass-panel rounded-2xl p-5 border-amber-500/30 bg-gradient-to-r from-amber-950/30 via-slate-900/50 to-indigo-950/30 relative overflow-hidden">
      <div className="flex items-center gap-2 mb-3">
        <div className="w-7 h-7 rounded-lg bg-amber-500/20 text-amber-400 flex items-center justify-center">
          <Sparkles className="w-4 h-4" />
        </div>
        <h3 className="text-sm font-bold tracking-tight text-white flex items-center gap-2">
          Diagnósticos e Metas Preditivas
          <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30">
            {alertas.length} recomendaç{alertas.length > 1 ? 'ões' : 'ão'}
          </span>
        </h3>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-2.5">
        {alertas.map((alerta, index) => (
          <div
            key={index}
            className="flex items-start gap-2.5 p-3 rounded-xl bg-slate-900/70 border border-slate-800 hover:border-amber-500/30 transition-colors"
          >
            <AlertCircle className="w-4 h-4 text-amber-400 mt-0.5 flex-shrink-0" />
            <p className="text-xs text-slate-200 leading-relaxed font-medium">
              {alerta}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
};
