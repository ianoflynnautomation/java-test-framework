package solutions.bjjeire.api.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.validation.ApiResponse;

import java.time.Duration;

@Component
public class ApiTestRunner {
    private static final Logger log = LoggerFactory.getLogger(ApiTestRunner.class);

    private final WebClient webClient;
    private final ApiSettings settings;
    private final ObjectMapper objectMapper;

    public ApiTestRunner(WebClient webClient, ApiSettings settings, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.settings = settings;
        this.objectMapper = objectMapper;
    }

    public ApiResponse run(ApiRequest spec) {
        long startTime = System.nanoTime();

        WebClient.RequestBodySpec requestBodySpec = webClient
                .method(spec.getMethod())
                .uri(uriBuilder -> uriBuilder.path(spec.getPath()).queryParams(spec.getQueryParams()).build())
                .headers(httpHeaders -> {
                    httpHeaders.addAll(spec.getHeaders());
                    // REFACTOR: Apply the authentication strategy.
                    spec.getAuthentication().apply(httpHeaders);
                });

        WebClient.RequestHeadersSpec<?> finalRequestSpec = (spec.getBody() != null)
                ? requestBodySpec.contentType(spec.getContentType()).bodyValue(spec.getBody())
                : requestBodySpec;

        Mono<ApiResponse> responseMono = finalRequestSpec
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class).map(responseEntity -> {
                    long endTime = System.nanoTime();
                    Duration executionTime = Duration.ofNanos(endTime - startTime);
                    return new ApiResponse(
                            responseEntity,
                            executionTime,
                            objectMapper,
                            spec.getPath()
                    );
                }))
                .retryWhen(getRetrySpec(spec.getMethod()));

        return responseMono.block();
    }

    private Retry getRetrySpec(HttpMethod method) {
        if (settings.getMaxRetryAttempts() > 0 && isIdempotent(method)) {
            return Retry.backoff(settings.getMaxRetryAttempts(), Duration.ofMillis(settings.getPauseBetweenFailuresMillis()))
                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure())
                    .doBeforeRetry(retrySignal -> log.warn("Request failed, retrying... Attempt #{}. Cause: {}",
                            retrySignal.totalRetries() + 1, retrySignal.failure().getMessage()));
        }
        return Retry.max(0); // No retries
    }

    private boolean isIdempotent(HttpMethod method) {
        return method == HttpMethod.GET || method == HttpMethod.PUT || method == HttpMethod.DELETE
                || method == HttpMethod.OPTIONS || method == HttpMethod.HEAD || method == HttpMethod.TRACE;
    }
}