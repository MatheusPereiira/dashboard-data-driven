import React from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
  Legend,
} from 'recharts';
import { DashboardPerformanceDTO } from '../types/api';
import { BarChart3, PieChart, Activity } from 'lucide-react';

interface ChartsSectionProps {
  dashboard: DashboardPerformanceDTO;
}

export const ChartsSection: React.FC<ChartsSectionProps> = ({ dashboard }) => {
  const { disciplinas, aluno } = dashboard;

  if (!disciplinas || disciplinas.length === 0) {
    return null;
  }

  const mediaCorte = aluno?.mediaMinimaAprovacao ?? 6.0;
  const freqMinima = aluno?.frequenciaMinimaExigida ?? 75.0;

  // Data for Chart 1: Grades comparison
  const gradesData = disciplinas.map((d) => ({
    name: d.nomeDisciplina.length > 14 ? `${d.nomeDisciplina.substring(0, 12)}...` : d.nomeDisciplina,
    fullName: d.nomeDisciplina,
    nota1: d.nota1,
    nota2: d.nota2 !== null && d.nota2 !== undefined ? d.nota2 : null,
    media: d.mediaAtual ?? d.nota1,
    status: d.statusSituacao,
    meta: d.notaNecessariaParaPassar ?? 0,
  }));

  // Data for Chart 2: Absence limit consumption
  const absenceData = disciplinas.map((d) => {
    const maxFaltas = Math.floor(d.totalAulasSemestre * (1 - (freqMinima / 100)));
    const pctUsado = maxFaltas > 0 ? Math.round((d.quantidadeFaltas / maxFaltas) * 100) : 0;
    return {
      name: d.nomeDisciplina.length > 14 ? `${d.nomeDisciplina.substring(0, 12)}...` : d.nomeDisciplina,
      fullName: d.nomeDisciplina,
      faltas: d.quantidadeFaltas,
      maxFaltas,
      restantes: Math.max(0, d.faltasRestantesPermitidas ?? (maxFaltas - d.quantidadeFaltas)),
      frequencia: d.percentualFrequencia ?? 100,
      consumoPct: Math.min(100, pctUsado),
    };
  });

  // Data for Chart 3: Correlation Radar (Notas x Frequência)
  const radarData = disciplinas.map((d) => ({
    subject: d.nomeDisciplina.length > 12 ? `${d.nomeDisciplina.substring(0, 10)}...` : d.nomeDisciplina,
    fullName: d.nomeDisciplina,
    Rendimento: d.mediaAtual ?? d.nota1,
    FrequenciaRelativa: ((d.percentualFrequencia ?? 100) / 10), // Scale 0-100 to 0-10
  }));

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Chart 1: Comparativo de Notas vs Média Institucional */}
      <div className="glass-panel rounded-2xl p-5 flex flex-col justify-between">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-indigo-500/10 text-indigo-400 flex items-center justify-center">
              <BarChart3 className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">
                Rendimento por Disciplina vs Linha de Corte
              </h3>
              <p className="text-xs text-slate-400">
                Notas registradas em relação à aprovação ({mediaCorte.toFixed(1)})
              </p>
            </div>
          </div>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={gradesData} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.5} />
              <XAxis
                dataKey="name"
                stroke="#94a3b8"
                fontSize={11}
                tickLine={false}
                interval={0}
                angle={-15}
                textAnchor="end"
              />
              <YAxis stroke="#94a3b8" fontSize={11} domain={[0, 10]} ticks={[0, 2, 4, 6, 8, 10]} />
              <Tooltip
                content={({ active, payload }) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload;
                    return (
                      <div className="bg-slate-900 border border-slate-700 p-3 rounded-xl shadow-xl text-xs space-y-1">
                        <p className="font-bold text-white mb-1">{data.fullName}</p>
                        <p className="text-indigo-400">Nota 1: <span className="text-white font-medium">{data.nota1}</span></p>
                        {data.nota2 !== null && (
                          <p className="text-cyan-400">Nota 2: <span className="text-white font-medium">{data.nota2}</span></p>
                        )}
                        <p className="text-emerald-400 font-semibold">Média Atual: {data.media.toFixed(1)}</p>
                        {data.meta > 0 && (
                          <p className="text-amber-400">Precisa na N2: <span className="font-bold">{data.meta.toFixed(1)}</span></p>
                        )}
                      </div>
                    );
                  }
                  return null;
                }}
              />
              <ReferenceLine
                y={mediaCorte}
                stroke="#f43f5e"
                strokeDasharray="4 4"
                label={{
                  value: `Corte (${mediaCorte})`,
                  fill: '#f43f5e',
                  position: 'top',
                  fontSize: 10,
                }}
              />
              <Bar dataKey="media" name="Média Atual" fill="#6366f1" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Chart 2: Consumo do Limite de Faltas */}
      <div className="glass-panel rounded-2xl p-5 flex flex-col justify-between">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center">
              <Activity className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">
                Consumo do Limite de Ausências
              </h3>
              <p className="text-xs text-slate-400">
                Faltas acumuladas vs margem permitida até reprovação
              </p>
            </div>
          </div>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart
              data={absenceData}
              layout="vertical"
              margin={{ top: 10, right: 20, left: 20, bottom: 0 }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.5} />
              <XAxis type="number" stroke="#94a3b8" fontSize={11} domain={[0, 'dataMax + 2']} />
              <YAxis
                type="category"
                dataKey="name"
                stroke="#94a3b8"
                fontSize={11}
                tickLine={false}
                width={80}
              />
              <Tooltip
                content={({ active, payload }) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload;
                    return (
                      <div className="bg-slate-900 border border-slate-700 p-3 rounded-xl shadow-xl text-xs space-y-1">
                        <p className="font-bold text-white mb-1">{data.fullName}</p>
                        <p className="text-rose-400">Faltas acumuladas: <span className="font-bold text-white">{data.faltas}</span></p>
                        <p className="text-emerald-400">Faltas restantes permitidas: <span className="font-bold text-white">{data.restantes}</span></p>
                        <p className="text-cyan-400">Frequência: <span className="font-bold text-white">{data.frequencia.toFixed(1)}%</span></p>
                      </div>
                    );
                  }
                  return null;
                }}
              />
              <Bar dataKey="faltas" name="Faltas Acumuladas" stackId="a" fill="#f43f5e" radius={[0, 0, 0, 0]} />
              <Bar dataKey="restantes" name="Faltas Restantes" stackId="a" fill="#10b981" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Chart 3 (Full width): Radar de Rendimento vs Frequência */}
      <div className="glass-panel rounded-2xl p-5 lg:col-span-2">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
              <PieChart className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">
                Equilíbrio Multidimensional: Rendimento × Assiduidade
              </h3>
              <p className="text-xs text-slate-400">
                Visualização integrada de notas (escala 0-10) e assiduidade proporcional
              </p>
            </div>
          </div>
        </div>

        <div className="h-72 w-full flex items-center justify-center">
          <ResponsiveContainer width="100%" height="100%">
            <RadarChart cx="50%" cy="50%" outerRadius="75%" data={radarData}>
              <PolarGrid stroke="#334155" />
              <PolarAngleAxis dataKey="subject" stroke="#94a3b8" fontSize={11} />
              <PolarRadiusAxis angle={30} domain={[0, 10]} stroke="#475569" fontSize={10} />
              <Radar
                name="Rendimento (Nota 0-10)"
                dataKey="Rendimento"
                stroke="#6366f1"
                fill="#6366f1"
                fillOpacity={0.4}
              />
              <Radar
                name="Presença Proporcional (0-10)"
                dataKey="FrequenciaRelativa"
                stroke="#10b981"
                fill="#10b981"
                fillOpacity={0.3}
              />
              <Legend wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }} />
              <Tooltip
                content={({ active, payload }) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload;
                    return (
                      <div className="bg-slate-900 border border-slate-700 p-3 rounded-xl shadow-xl text-xs space-y-1">
                        <p className="font-bold text-white mb-1">{data.fullName}</p>
                        <p className="text-indigo-400">Rendimento Médio: {data.Rendimento.toFixed(1)} / 10</p>
                        <p className="text-emerald-400">Frequência: {(data.FrequenciaRelativa * 10).toFixed(1)}%</p>
                      </div>
                    );
                  }
                  return null;
                }}
              />
            </RadarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};
