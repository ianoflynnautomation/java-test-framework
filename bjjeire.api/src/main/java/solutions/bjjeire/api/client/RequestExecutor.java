package solutions.bjjeire.api.client;

import io.opentelemetry.api.trace.Tracer;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.telemetry.MetricsCollector;
import solutions.bjjeire.api.telemetry.TracingManager;
import solutions.bjjeire.api.utils.RetryPolicy;
import solutions.bjjeire.api.validation.ApiResponse;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RequestExecutor.class);
    private final WebClient webClient;
    private final Tracer tracer;
    private final RetryPolicy retryPolicy;
    private final MetricsCollector metricsCollector;
    private final RequestBodyHandler bodyHandler;
    private final WebClientRequestFactory requestFactory;
    private final TracingManager tracingManager;

    public RequestExecutor(WebClient webClient, Tracer tracer, RetryPolicy retryPolicy,
                           MetricsCollector metricsCollector, TracingManager tracingManager,
                           RequestBodyHandler bodyHandler) {
        this.webClient = webClient;
        this.tracer = tracer;
        this.retryPolicy = retryPolicy;
        this.metricsCollector = metricsCollector;
        this.tracingManager = tracingManager;
        this.bodyHandler = bodyHandler;
        this.requestFactory = new WebClientRequestFactory(webClient, bodyHandler);
    }

    public Mono<ApiResponse> execute(ApiRequest request) {
        return tracingManager.withSpan(tracer, "http." + request.getMethod().name().toLowerCase(), request, span -> {
            span.setAttribute("http.url", request.getPath());
            span.setAttribute("http.method", request.getMethod().name());

            long startTime = System.nanoTime();
            WebClient.RequestHeadersSpec<?> finalRequestSpec = requestFactory.create(request);

            return finalRequestSpec.exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)
                            .map(responseEntity -> {
                                long endTime = System.nanoTime();
                                ApiResponse apiResponse = new ApiResponse(responseEntity, Duration.ofNanos(endTime - startTime),
                                        bodyHandler.getObjectMapper(), request.getPath());

                                metricsCollector.recordSuccess(apiResponse, request);
                                // Log API request-response cycle
                                logger.info("API Request-Response Cycle",
                                        StructuredArguments.kv("eventType", "api_interaction"),
                                        StructuredArguments.kv("httpMethod", request.getMethod().name()),
                                        StructuredArguments.kv("url", request.getPath()),
                                        StructuredArguments.kv("requestHeaders", request.getHeaders().entrySet().stream()
                                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))),
                                        StructuredArguments.kv("requestBody", safeSerializeBody(request.getBody())),
                                        StructuredArguments.kv("responseStatusCode", apiResponse.getStatusCode()),
                                        StructuredArguments.kv("responseHeaders", apiResponse.getHeaders().entrySet().stream()
                                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))),
                                        StructuredArguments.kv("responseBody", truncateBody(apiResponse.getBodyAsString(), 1000)),
                                        StructuredArguments.kv("executionTimeMillis", apiResponse.getExecutionTime().toMillis()));

                                span.setAttribute("http.status_code", apiResponse.getStatusCode());
                                return apiResponse;
                            }))
                    .doOnError(error -> {
                        metricsCollector.recordError(error, request);
                        logger.error("WebClient request failed",
                                StructuredArguments.kv("eventType", "api_failure"),
                                StructuredArguments.kv("url", request.getPath()),
                                StructuredArguments.kv("method", request.getMethod().name()),
                                StructuredArguments.kv("error", error.getMessage()),
                                error);
                    })
                    .doOnCancel(() -> {
                        metricsCollector.recordCancellation(request);
                        logger.warn("API request cancelled",
                                StructuredArguments.kv("eventType", "api_cancellation"),
                                StructuredArguments.kv("url", request.getPath()),
                                StructuredArguments.kv("method", request.getMethod().name()));
                    });
        }).retryWhen(retryPolicy.getRetrySpec(request.getMethod(), request.getPath()));
    }

    private Object safeSerializeBody(Object body) {
        if (body == null) return null;
        try {
            return bodyHandler.getObjectMapper().convertValue(body, Object.class);
        } catch (Exception e) {
            logger.warn("Failed to serialize request body for logging",
                    StructuredArguments.kv("error", e.getMessage()));
            return "unserializable";
        }
    }

    private String truncateBody(String body, int maxLength) {
        if (body == null) return null;
        return body.length() > maxLength ? body.substring(0, maxLength) + "..." : body;
    }
}