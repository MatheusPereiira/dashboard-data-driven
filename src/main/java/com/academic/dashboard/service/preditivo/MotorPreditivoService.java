package com.academic.dashboard.service.preditivo;

import com.academic.dashboard.model.Aluno;
import com.academic.dashboard.model.DisciplinaDesempenho;
import com.academic.dashboard.service.CalculadoraDesempenho;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MotorPreditivoService {

    private final PythonPredictiveClient pythonClient;
    private final MotorPreditivoLocal motorLocal;
    private final CalculadoraDesempenho calculadora;

    public MotorPreditivoService(PythonPredictiveClient pythonClient,
                                 MotorPreditivoLocal motorLocal,
                                 CalculadoraDesempenho calculadora) {
        this.pythonClient = pythonClient;
        this.motorLocal = motorLocal;
        this.calculadora = calculadora;
    }

    public ResultadoPredicao avaliar(Aluno aluno, List<DisciplinaDesempenho> disciplinas) {
        PredicaoRequest request = montarRequest(aluno, disciplinas);
        if (request.disciplinas().isEmpty()) {
            return motorLocal.avaliar(request);
        }
        return pythonClient.avaliar(request).orElseGet(() -> motorLocal.avaliar(request));
    }

    PredicaoRequest montarRequest(Aluno aluno, List<DisciplinaDesempenho> disciplinas) {
        List<PredicaoRequest.DisciplinaFeature> features = disciplinas.stream()
                .map(d -> new PredicaoRequest.DisciplinaFeature(
                        d.getNomeDisciplina(),
                        d.getNotaEtapa1(),
                        d.getNotaEtapa2(),
                        d.getNotaEtapa3(),
                        d.getMediaAtual(),
                        d.getNotaNecessariaParaPassar(),
                        d.getQuantidadeFaltas(),
                        d.getTotalAulasSemestre(),
                        calculadora.calcularLimiteFaltas(d, aluno),
                        d.getFaltasRestantesPermitidas() != null ? d.getFaltasRestantesPermitidas() : 0,
                        d.getPercentualFrequencia() != null ? d.getPercentualFrequencia() : 0.0,
                        d.getStatusSituacao() != null ? d.getStatusSituacao().name() : null))
                .toList();
        return new PredicaoRequest(
                aluno.getId(),
                CalculadoraDesempenho.normalizarMediaMinima(aluno),
                CalculadoraDesempenho.frequenciaMinima(aluno),
                features);
    }
}
