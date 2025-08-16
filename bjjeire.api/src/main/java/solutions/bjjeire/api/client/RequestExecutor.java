package solutions.bjjeire.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.utils.RetryPolicy;
import solutions.bjjeire.api.validation.ApiResponse;

import java.time.Duration;
import net.logstash.logback.argument.StructuredArguments;

public class RequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RequestExecutor.class);
    private final WebClient webClient;
    private final RetryPolicy retryPolicy;
    private final RequestBodyHandler bodyHandler;
    private final WebClientRequestFactory requestFactory;

    public RequestExecutor(WebClient webClient, RetryPolicy retryPolicy,  RequestBodyHandler bodyHandler) {
        this.webClient = webClient;
        this.retryPolicy = retryPolicy;
        this.bodyHandler = bodyHandler;
        this.requestFactory = new WebClientRequestFactory(webClient, bodyHandler);
    }

    public Mono<ApiResponse> execute(ApiRequest request) {
        long startTime = System.nanoTime();

        WebClient.RequestHeadersSpec<?> requestSpec = requestFactory.create(request);

        return requestSpec.exchangeToMono(clientResponse ->
                        clientResponse.toEntity(String.class)
                                .map(responseEntity -> new ApiResponse(
                                        responseEntity,
                                        Duration.ofNanos(System.nanoTime() - startTime),
                                        bodyHandler.getObjectMapper(),
                                        request.getPath()))
                )
                .doOnSuccess(response -> logApiInteraction(request, response, Duration.ofNanos(System.nanoTime() - startTime)))
                .doOnError(error -> logger.error("API request execution failed",
                        StructuredArguments.kv("eventType", "api_failure"),
                        StructuredArguments.kv("url", request.getPath()),
                        StructuredArguments.kv("method", request.getMethod().name()),
                        StructuredArguments.kv("error", error.getMessage()),
                        error))
                .retryWhen(retryPolicy.getRetrySpec(request.getPath(), request.getMethod()));
    }

    private WebClient.RequestHeadersSpec<?> buildRequestSpec(ApiRequest request) {
        WebClient.RequestBodySpec requestBodySpec = webClient
                .method(request.getMethod())
                .uri(request.getPath())
                .headers(httpHeaders -> httpHeaders.addAll(request.getHeaders()));

        if (request.getBody() != null) {
            return requestBodySpec.bodyValue(request.getBody());
        }

        return requestBodySpec;
    }

    private void logApiInteraction(ApiRequest request, ApiResponse response, Duration duration) {
        logger.info("API Request-Response Cycle",
                StructuredArguments.kv("eventType", "api_interaction"),
                StructuredArguments.kv("http_method", request.getMethod().name()),
                StructuredArguments.kv("url", request.getPath()),
                StructuredArguments.kv("request_body", safeSerializeBody(request.getBody())),
                StructuredArguments.kv("response_status_code", response.getStatusCode()),
                StructuredArguments.kv("response_body", truncateBody(response.getBodyAsString(), 1000)),
                StructuredArguments.kv("duration_ms", duration.toMillis()));
    }

    private Object safeSerializeBody(Object body) {
        if (body == null) return null;
        try {
            return bodyHandler.getObjectMapper().convertValue(body, Object.class);
        } catch (Exception e) {
            logger.warn("Failed to serialize request body for logging", StructuredArguments.kv("error", e.getMessage()));
            return "unserializable_body";
        }
    }

    private String truncateBody(String body, int maxLength) {
        if (body == null) return null;
        return body.length() > maxLength ? body.substring(0, maxLength) + "..." : body;
    }
}