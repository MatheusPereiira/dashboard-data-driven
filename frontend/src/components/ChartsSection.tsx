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

  const rawCorte = aluno?.mediaMinimaAprovacao ?? 60.0;
  const mediaCorte = rawCorte <= 10.0 ? rawCorte * 10 : rawCorte;
  const freqMinima = aluno?.frequenciaMinimaExigida ?? 80.0;

  // Data for Chart 1: Grades comparison (100 points scale)
  const gradesData = disciplinas.map((d) => {
    const e1 = d.notaEtapa1 ?? d.nota1 ?? 0;
    const e2 = d.notaEtapa2 ?? d.nota2 ?? 0;
    const e3 = d.notaEtapa3 ?? 0;
    const total = d.mediaAtual ?? (e1 + e2 + e3);

    return {
      name: d.nomeDisciplina.length > 14 ? `${d.nomeDisciplina.substring(0, 12)}...` : d.nomeDisciplina,
      fullName: d.nomeDisciplina,
      etapa1: d.notaEtapa1 ?? d.nota1 ?? null,
      etapa2: d.notaEtapa2 ?? d.nota2 ?? null,
      etapa3: d.notaEtapa3 ?? null,
      total: total,
      status: d.statusSituacao,
      meta: d.notaNecessariaParaPassar ?? 0,
    };
  });

  // Data for Chart 2: Absence limit consumption (max 20% allowed)
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

  // Data for Chart 3: Correlation Radar (Pontos x Frequência)
  const radarData = disciplinas.map((d) => {
    const e1 = d.notaEtapa1 ?? d.nota1 ?? 0;
    const e2 = d.notaEtapa2 ?? d.nota2 ?? 0;
    const e3 = d.notaEtapa3 ?? 0;
    const total = d.mediaAtual ?? (e1 + e2 + e3);

    return {
      subject: d.nomeDisciplina.length > 12 ? `${d.nomeDisciplina.substring(0, 10)}...` : d.nomeDisciplina,
      fullName: d.nomeDisciplina,
      PontuacaoTotal: total,
      Frequencia: d.percentualFrequencia ?? 100,
    };
  });

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Chart 1: Comparativo de Pontos Acumulados vs Meta FANS (60 pts) */}
      <div className="glass-panel rounded-2xl p-5 flex flex-col justify-between">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-indigo-500/10 text-indigo-400 flex items-center justify-center">
              <BarChart3 className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">
                Pontuação Acumulada vs Meta de Aprovação
              </h3>
              <p className="text-xs text-slate-400">
                Pontos somados nas 3 etapas em relação ao corte ({mediaCorte.toFixed(0)} pts / 100)
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
              <YAxis
                stroke="#94a3b8"
                fontSize={11}
                tickLine={false}
                domain={[0, 100]}
                ticks={[0, 25, 55, 60, 100]}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#0f172a',
                  borderColor: '#334155',
                  borderRadius: '0.75rem',
                  fontSize: '0.75rem',
                  boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.5)',
                }}
                formatter={(value: any, name: string) => {
                  if (name === 'total') return [`${value} pts`, 'Pontos Acumulados'];
                  return [`${value} pts`, name];
                }}
                labelFormatter={(label, payload) => {
                  if (payload && payload.length > 0) {
                    return payload[0].payload.fullName;
                  }
                  return label;
                }}
              />
              <ReferenceLine
                y={mediaCorte}
                stroke="#10b981"
                strokeDasharray="4 4"
                label={{
                  value: `Meta: ${mediaCorte.toFixed(0)} pts`,
                  fill: '#10b981',
                  fontSize: 10,
                  position: 'insideTopRight',
                }}
              />
              <Bar dataKey="total" fill="#6366f1" radius={[4, 4, 0, 0]} name="total" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Chart 2: Consumo do Limite de Faltas (Máx 20% ausências) */}
      <div className="glass-panel rounded-2xl p-5 flex flex-col justify-between">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center">
              <PieChart className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">
                Consumo do Limite de Ausências (Máx 20% Faltas)
              </h3>
              <p className="text-xs text-slate-400">
                Percentual gasto do teto regulamentar de ausências permitidas
              </p>
            </div>
          </div>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={absenceData} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
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
              <YAxis
                stroke="#94a3b8"
                fontSize={11}
                tickLine={false}
                domain={[0, 100]}
                unit="%"
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#0f172a',
                  borderColor: '#334155',
                  borderRadius: '0.75rem',
                  fontSize: '0.75rem',
                  boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.5)',
                }}
                formatter={(val: any, _name: any, props: any) => [
                  `${props.payload.faltas} de ${props.payload.maxFaltas} faltas (${val}%)`,
                  'Consumo do Limite',
                ]}
                labelFormatter={(_label: any, payload: any) => {
                  if (payload && payload.length > 0) {
                    return payload[0].payload.fullName;
                  }
                  return '';
                }}
              />
              <ReferenceLine
                y={75}
                stroke="#f59e0b"
                strokeDasharray="3 3"
                label={{ value: 'Alerta (75%)', fill: '#f59e0b', fontSize: 10, position: 'insideTopLeft' }}
              />
              <ReferenceLine
                y={100}
                stroke="#ef4444"
                strokeDasharray="3 3"
                label={{ value: 'Reprovação (100%)', fill: '#ef4444', fontSize: 10, position: 'insideTopRight' }}
              />
              <Bar
                dataKey="consumoPct"
                fill="#06b6d4"
                radius={[4, 4, 0, 0]}
              />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Chart 3: Radar Multidimensional Rendimento vs Frequência */}
      <div className="glass-panel rounded-2xl p-5 lg:col-span-2 flex flex-col justify-between">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
              <Activity className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">
                Equilíbrio Multidimensional: Rendimento x Assiduidade
              </h3>
              <p className="text-xs text-slate-400">
                Visualização integrada de notas somadas e % de presenças por disciplina
              </p>
            </div>
          </div>
        </div>

        <div className="h-72 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <RadarChart cx="50%" cy="50%" outerRadius="75%" data={radarData}>
              <PolarGrid stroke="#334155" />
              <PolarAngleAxis dataKey="subject" stroke="#94a3b8" fontSize={11} />
              <PolarRadiusAxis angle={30} domain={[0, 100]} stroke="#64748b" fontSize={10} />
              <Radar
                name="Pontuação Total (0-100)"
                dataKey="PontuacaoTotal"
                stroke="#6366f1"
                fill="#6366f1"
                fillOpacity={0.4}
              />
              <Radar
                name="Frequência (%)"
                dataKey="Frequencia"
                stroke="#10b981"
                fill="#10b981"
                fillOpacity={0.3}
              />
              <Legend
                wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }}
                iconType="circle"
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#0f172a',
                  borderColor: '#334155',
                  borderRadius: '0.75rem',
                  fontSize: '0.75rem',
                }}
              />
            </RadarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};
