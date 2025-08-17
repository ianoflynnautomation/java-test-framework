package solutions.bjjeire.api.utils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import reactor.util.retry.Retry;
import solutions.bjjeire.api.config.ApiSettings;
import solutions.bjjeire.api.exceptions.ApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryPolicy {

    private final ApiSettings apiSettings;

    public Retry getRetrySpec(String endpoint, HttpMethod method) {
        if (!isIdempotent(method) || apiSettings.getMaxRetryAttempts() <= 0) {
            return Retry.fixedDelay(0, Duration.ZERO);
        }

        return Retry
                .backoff(apiSettings.getMaxRetryAttempts(),
                        Duration.ofMillis(apiSettings.getPauseBetweenFailuresMillis()))
                .filter(this::isRetryableException)
                .doBeforeRetry(retrySignal -> {
                    long attempt = retrySignal.totalRetriesInARow() + 1;
                    log.warn("Retrying API request",
                            StructuredArguments.kv("eventType", "api_retry"),
                            StructuredArguments.kv("endpoint", endpoint),
                            StructuredArguments.kv("method", method.name()),
                            StructuredArguments.kv("attempt", attempt),
                            StructuredArguments.kv("reason", retrySignal.failure().getMessage()));
                })
                .onRetryExhaustedThrow((retrySpec, retrySignal) -> {
                    log.error("Retry attempts exhausted for API request",
                            StructuredArguments.kv("eventType", "api_retry_exhausted"),
                            StructuredArguments.kv("endpoint", endpoint),
                            StructuredArguments.kv("method", method.name()),
                            StructuredArguments.kv("total_attempts", apiSettings.getMaxRetryAttempts()),
                            retrySignal.failure());
                    return retrySignal.failure();
                });
    }

    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof ApiException) {
            return false;
        }

        if (throwable instanceof WebClientRequestException) {
            Throwable cause = throwable.getCause();
            return cause instanceof SocketTimeoutException ||
                    cause instanceof UnknownHostException || cause instanceof IOException;
        }
        if (throwable instanceof WebClientResponseException webClientResponseException) {
            int statusCode = webClientResponseException.getStatusCode().value();
            return statusCode == 429 || (statusCode >= 500 && statusCode < 600);
        }

        return false;
    }

    private boolean isIdempotent(HttpMethod method) {
        return method == HttpMethod.GET || method == HttpMethod.PUT || method == HttpMethod.DELETE ||
                method == HttpMethod.HEAD || method == HttpMethod.OPTIONS;
    }
}