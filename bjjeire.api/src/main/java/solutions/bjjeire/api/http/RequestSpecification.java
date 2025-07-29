package solutions.bjjeire.api.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.validation.ValidatableResponse;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;


public class RequestSpecification {
    private static final Logger log = LoggerFactory.getLogger(RequestSpecification.class);

    private final ApiClient apiClient;
    private final Map<String, String> headers;
    private final Map<String, Object> queryParams;
    private final Object body;
    private final Consumer<HttpHeaders> authAction;

    public RequestSpecification(ApiClient apiClient, Map<String, String> headers, Map<String, Object> queryParams, Object body, Consumer<HttpHeaders> authAction) {
        this.apiClient = Objects.requireNonNull(apiClient);
        this.headers = headers;
        this.queryParams = queryParams;
        this.body = body;
        this.authAction = authAction;
    }

    public RequestSpecification withHeader(String name, String value) {
        Map<String, String> newHeaders = new HashMap<>(this.headers);
        newHeaders.put(name, value);
        return new RequestSpecification(this.apiClient, newHeaders, this.queryParams, this.body, this.authAction);
    }

    public RequestSpecification withQueryParams(Map<String, ?> params) {
        Map<String, Object> newParams = new HashMap<>(this.queryParams);
        newParams.putAll(params);
        return new RequestSpecification(this.apiClient, this.headers, newParams, this.body, this.authAction);
    }

    public RequestSpecification withAuthToken(String token) {
        Consumer<HttpHeaders> newAuthAction = httpHeaders -> {
            if (token != null && !token.isBlank()) {
                httpHeaders.setBearerAuth(token);
            }
        };
        return new RequestSpecification(this.apiClient, this.headers, this.queryParams, this.body, newAuthAction);
    }

    public RequestSpecification withBody(Object body) {
        return new RequestSpecification(this.apiClient, this.headers, this.queryParams, body, this.authAction);
    }

    public ValidatableResponse get(String path) { return send(HttpMethod.GET, path); }
    public ValidatableResponse post(String path) { return send(HttpMethod.POST, path); }
    public ValidatableResponse put(String path) { return send(HttpMethod.PUT, path); }
    public ValidatableResponse delete(String path) { return send(HttpMethod.DELETE, path); }
    public ValidatableResponse patch(String path) { return send(HttpMethod.PATCH, path); }

    private ValidatableResponse send(HttpMethod method, String path) {
        long startTime = System.nanoTime();

        log.info("Sending {} request to path '{}' with query params: {}", method, path, queryParams);

        WebClient.RequestBodySpec requestSpec = apiClient.getWebClient()
                .method(method)
                .uri(uriBuilder -> uriBuilder.path(path).queryParams(toMultiValueMap(queryParams)).build())
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.setAll(headers);
                    if (authAction != null) {
                        authAction.accept(httpHeaders);
                    }
                });

        WebClient.RequestHeadersSpec<?> finalRequestSpec;
        if (body != null) {
            finalRequestSpec = requestSpec
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        } else {
            finalRequestSpec = requestSpec;
        }

        Mono<ValidatableResponse> responseMono = finalRequestSpec
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class).map(responseEntity -> {
                    long endTime = System.nanoTime();
                    Duration executionTime = Duration.ofNanos(endTime - startTime);
                    return new ValidatableResponse(
                            responseEntity,
                            executionTime,
                            apiClient.getObjectMapper(),
                            path
                    );
                }))
                .retryWhen(getRetrySpec(method));

        return responseMono.block();
    }

    private Retry getRetrySpec(HttpMethod method) {
        ApiSettings settings = apiClient.getSettings();
        if (method == HttpMethod.GET || method == HttpMethod.PUT || method == HttpMethod.DELETE) {
            return Retry.backoff(settings.getMaxRetryAttempts(), Duration.ofMillis(settings.getPauseBetweenFailuresMillis()))
                    .doBeforeRetry(retrySignal -> log.warn("Request failed, retrying... Attempt #{}. Cause: {}",
                            retrySignal.totalRetries() + 1, retrySignal.failure().getMessage()));
        }
        return Retry.max(0);
    }

    private org.springframework.util.MultiValueMap<String, String> toMultiValueMap(Map<String, Object> source) {
        org.springframework.util.LinkedMultiValueMap<String, String> map = new org.springframework.util.LinkedMultiValueMap<>();
        source.forEach((key, value) -> map.add(key, String.valueOf(value)));
        return map;
    }
}