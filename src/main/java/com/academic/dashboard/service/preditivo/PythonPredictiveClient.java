package com.academic.dashboard.service.preditivo;

import com.academic.dashboard.model.ClassificacaoRisco;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cliente HTTP do motor preditivo em Python (FastAPI).
 *
 * <p>Qualquer falha (servico desligado, timeout, resposta invalida) resulta em {@link Optional#empty()},
 * permitindo que o fluxo de consolidacao utilize o modelo local de contingencia sem interromper o usuario.
 */
@Component
public class PythonPredictiveClient {

    private static final Logger log = LoggerFactory.getLogger(PythonPredictiveClient.class);

    private final RestClient restClient;
    private final String serviceUrl;
    private final boolean enabled;
    private final AtomicBoolean indisponivelReportado = new AtomicBoolean(false);

    public PythonPredictiveClient(
            @Value("${app.predictive.python-service-url:http://localhost:8000/api/v1/predict}") String serviceUrl,
            @Value("${app.predictive.enabled:true}") boolean enabled,
            @Value("${app.predictive.timeout-ms:3000}") long timeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        this.serviceUrl = serviceUrl;
        this.enabled = enabled;
    }

    public Optional<ResultadoPredicao> avaliar(PredicaoRequest request) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            PredicaoResposta resposta = restClient.post()
                    .uri(serviceUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PredicaoResposta.class);

            Optional<ResultadoPredicao> resultado = converter(resposta);
            if (resultado.isEmpty()) {
                log.warn("Motor preditivo Python retornou resposta invalida; utilizando modelo local.");
            } else if (indisponivelReportado.compareAndSet(true, false)) {
                log.info("Motor preditivo Python disponivel novamente em {}.", serviceUrl);
            }
            return resultado;
        } catch (Exception ex) {
            if (indisponivelReportado.compareAndSet(false, true)) {
                log.warn("Motor preditivo Python indisponivel em {} ({}). Utilizando modelo local de contingencia.",
                        serviceUrl, ex.getMessage());
            } else {
                log.debug("Motor preditivo Python ainda indisponivel: {}", ex.getMessage());
            }
            return Optional.empty();
        }
    }

    static Optional<ResultadoPredicao> converter(PredicaoResposta resposta) {
        if (resposta == null || resposta.scoreRisco() == null || resposta.classificacaoRisco() == null) {
            return Optional.empty();
        }
        double score = resposta.scoreRisco();
        if (Double.isNaN(score) || score < 0.0 || score > 100.0) {
            return Optional.empty();
        }
        ClassificacaoRisco classificacao;
        try {
            classificacao = ClassificacaoRisco.valueOf(resposta.classificacaoRisco().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        List<String> recomendacoes = resposta.recomendacoes() != null
                ? resposta.recomendacoes().stream().filter(r -> r != null && !r.isBlank()).toList()
                : List.of();
        return Optional.of(new ResultadoPredicao(Math.round(score * 100.0) / 100.0, classificacao, recomendacoes, "python"));
    }
}

