package solutions.bjjeire.api.utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import net.logstash.logback.argument.StructuredArguments;
import reactor.util.retry.Retry;
import solutions.bjjeire.api.configuration.ApiSettings;

@Component
public class RetryPolicy {
    private static final Logger logger = LoggerFactory.getLogger(RetryPolicy.class);
    private final ApiSettings apiSettings;
    private final MeterRegistry meterRegistry;
    private final Counter apiRequestRetryCounter;

    public RetryPolicy(ApiSettings apiSettings, MeterRegistry meterRegistry) {
        this.apiSettings = apiSettings;
        this.meterRegistry = meterRegistry;
        this.apiRequestRetryCounter = Counter.builder("api_request_retry_total")
                .description("Total number of API request retries")
                .tag("application", "api-tests")
                .register(meterRegistry);
    }

    public Retry getRetrySpec(HttpMethod method, String endpoint) {
        if (apiSettings.getMaxRetryAttempts() > 0 && isIdempotent(method)) {
            return Retry
                    .backoff(apiSettings.getMaxRetryAttempts(),
                            Duration.ofMillis(apiSettings.getPauseBetweenFailuresMillis()))
                    .filter(throwable -> {
                        // Retry on network errors
                        if (throwable instanceof WebClientRequestException) {
                            Throwable cause = throwable.getCause();
                            boolean isNetworkIssue = cause instanceof ConnectException ||
                                    cause instanceof SocketTimeoutException ||
                                    cause instanceof UnknownHostException ||
                                    cause instanceof IOException;
                            if (isNetworkIssue) {
                                logger.debug("Eligible for retry: Network error detected",
                                        StructuredArguments.kv("endpoint", endpoint),
                                        StructuredArguments.kv("method", method.name()),
                                        StructuredArguments.kv("originalError", throwable.getMessage()),
                                        StructuredArguments.kv("networkCauseType",
                                                cause != null ? cause.getClass().getSimpleName() : "unknown"));
                                return true;
                            }
                        }

                        if (throwable instanceof WebClientResponseException) {
                            WebClientResponseException responseException = (WebClientResponseException) throwable;
                            int statusCode = responseException.getStatusCode().value();
                            boolean isRetryableHttpStatus = statusCode == 429
                                    || (statusCode >= 500 && statusCode < 600);
                            if (isRetryableHttpStatus) {
                                logger.debug("Eligible for retry: Retryable HTTP status code detected",
                                        StructuredArguments.kv("endpoint", endpoint),
                                        StructuredArguments.kv("method", method.name()),
                                        StructuredArguments.kv("statusCode", statusCode),
                                        StructuredArguments.kv("originalError", responseException.getMessage()));
                                return true;
                            }
                        }

                        logger.debug("Not eligible for retry: Non-retryable error type",
                                StructuredArguments.kv("endpoint", endpoint),
                                StructuredArguments.kv("method", method.name()),
                                StructuredArguments.kv("originalError", throwable.getMessage()),
                                StructuredArguments.kv("exceptionType", throwable.getClass().getSimpleName()));
                        return false;
                    })
                    .doBeforeRetry(retrySignal -> {
                        long attempt = retrySignal.totalRetriesInARow() + 1;
                        logger.debug("Retrying API request",
                                StructuredArguments.kv("endpoint", endpoint),
                                StructuredArguments.kv("method", method.name()),
                                StructuredArguments.kv("attempt", attempt),
                                StructuredArguments.kv("reason", retrySignal.failure().getMessage()));
                        apiRequestRetryCounter.increment();
                    })
                    .onRetryExhaustedThrow((retrySpec, retrySignal) -> {
                        String message = String.format(
                                "Retry attempts exhausted for API request to %s %s. Final failure: %s",
                                method.name(), endpoint, retrySignal.failure().getMessage());
                        logger.error(message,
                                StructuredArguments.kv("endpoint", endpoint),
                                StructuredArguments.kv("method", method.name()),
                                StructuredArguments.kv("failure", retrySignal.failure().getMessage()),
                                retrySignal.failure());
                        return retrySignal.failure();
                    });
        }
        return Retry.fixedDelay(0, Duration.ZERO);
    }

    private boolean isIdempotent(HttpMethod method) {
        return method == HttpMethod.GET || method == HttpMethod.PUT || method == HttpMethod.DELETE ||
                method == HttpMethod.HEAD || method == HttpMethod.OPTIONS;
    }
}