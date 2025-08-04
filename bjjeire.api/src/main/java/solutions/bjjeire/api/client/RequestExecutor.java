package solutions.bjjeire.api.client;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import net.logstash.logback.argument.StructuredArguments;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.telemetry.MetricsCollector;
import solutions.bjjeire.api.utils.RetryPolicy;
import solutions.bjjeire.api.validation.ApiResponse;

public class RequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RequestExecutor.class);
    private final WebClient webClient;
    private final RetryPolicy retryPolicy;
    private final MetricsCollector metricsCollector;
    private final RequestBodyHandler bodyHandler;
    private final WebClientRequestFactory requestFactory;

    public RequestExecutor(WebClient webClient, RetryPolicy retryPolicy, MetricsCollector metricsCollector,
            RequestBodyHandler bodyHandler) {
        this.webClient = webClient;
        this.retryPolicy = retryPolicy;
        this.metricsCollector = metricsCollector;
        this.bodyHandler = bodyHandler;
        this.requestFactory = new WebClientRequestFactory(webClient, bodyHandler);
    }

    public Mono<ApiResponse> execute(ApiRequest request) {
   
        Span span = Span.current(); 
        Baggage.current()
                .toBuilder()
                .put("api.request.path", request.getPath())
                .put("api.request.method", request.getMethod().name())
                .build()
                .storeInContext(io.opentelemetry.context.Context.current())
                .makeCurrent();

        long startTime = System.nanoTime();
        WebClient.RequestHeadersSpec<?> finalRequestSpec = requestFactory.create(request);

        return finalRequestSpec.exchangeToMono(clientResponse -> clientResponse.toEntity(String.class)
                .map(responseEntity -> {
                    long endTime = System.nanoTime();
                    ApiResponse apiResponse = new ApiResponse(responseEntity, Duration.ofNanos(endTime - startTime),
                            bodyHandler.getObjectMapper(), request.getPath());

                    metricsCollector.recordSuccess(apiResponse, request, span);

                    span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, apiResponse.getStatusCode());
                    span.setAttribute("response.body.length", apiResponse.getBodyAsString().length());
                    span.setAttribute("http.response.status_code_group",
                            getStatusCodeGroup(apiResponse.getStatusCode()));

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
                            StructuredArguments.kv("executionTimeMillis", apiResponse.getExecutionTime().toMillis())
                    );
                    return apiResponse;
                }))
                .doOnError(error -> {
                    metricsCollector.recordError(error, request, span);
                    span.recordException(error); 
                    span.setStatus(StatusCode.ERROR, error.getMessage()); 
                    logger.error("WebClient request failed",
                            StructuredArguments.kv("eventType", "api_failure"),
                            StructuredArguments.kv("url", request.getPath()),
                            StructuredArguments.kv("method", request.getMethod().name()),
                            StructuredArguments.kv("error", error.getMessage()),
                            error
                    );
                })
                .doOnCancel(() -> {
                    metricsCollector.recordCancellation(request, span);
                    span.setStatus(StatusCode.UNSET, "Request Cancelled"); 
                    logger.warn("API request cancelled",
                            StructuredArguments.kv("eventType", "api_cancellation"),
                            StructuredArguments.kv("url", request.getPath()),
                            StructuredArguments.kv("method", request.getMethod().name())
                    );
                })
                .retryWhen(retryPolicy.getRetrySpec(request.getMethod(), request.getPath()));
    }

    private Object safeSerializeBody(Object body) {
        if (body == null)
            return null;
        try {
            return bodyHandler.getObjectMapper().convertValue(body, Object.class);
        } catch (Exception e) {
            logger.warn("Failed to serialize request body for logging",
                    StructuredArguments.kv("error", e.getMessage()));
            return "unserializable";
        }
    }

    private String truncateBody(String body, int maxLength) {
        if (body == null)
            return null;
        return body.length() > maxLength ? body.substring(0, maxLength) + "..." : body;
    }

    private String getStatusCodeGroup(int statusCode) {
        if (statusCode < 100 || statusCode > 999) {
            return "unknown";
        }
        return statusCode / 100 + "xx";
    }
}
