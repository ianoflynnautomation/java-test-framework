package solutions.bjjeire.api.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.models.MeasuredResponse;
import solutions.bjjeire.api.validation.ResponseAsserter;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A stateless, thread-safe, singleton service for executing HTTP requests.
 * It manages a shared OkHttpClient and implements retry logic for network-level
 * failures using Resilience4j. Application-level failures (e.g., 4xx, 5xx) are
 * not retried here but are handled by the ResponseAsserter.
 */
@Service
public class ApiClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private final OkHttpClient httpClient;
    private final Retry retry;
    @Getter
    private final ObjectMapper objectMapper;
    @Getter
    private final ApiSettings settings;

    @Autowired
    public ApiClient(ApiSettings settings) {
        this.settings = Objects.requireNonNull(settings);
        logger.info("Initializing ApiClient with base URL '{}', timeout of {}s, and {} retry attempts.",
                settings.getBaseUrl(), settings.getClientTimeoutSeconds(), settings.getMaxRetryAttempts());

        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(settings.getClientTimeoutSeconds()))
                .readTimeout(Duration.ofSeconds(settings.getClientTimeoutSeconds()))
                .writeTimeout(Duration.ofSeconds(settings.getClientTimeoutSeconds()))
                .build();

        // Configure retry logic ONLY for network/IO exceptions.
        // Application errors (4xx/5xx) should be handled as test failures, not retried.
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(settings.getMaxRetryAttempts())
                .intervalFunction(IntervalFunction.of(Duration.ofMillis(settings.getPauseBetweenFailuresMillis())))
                .retryOnException(e -> e instanceof IOException)
                .failAfterMaxAttempts(true)
                .build();
        this.retry = RetryRegistry.of(retryConfig).retry("api-request");
    }

    /**
     * Executes a request and wraps the result in a ResponseAsserter.
     *
     * @param request The OkHttp request to execute.
     * @return A ResponseAsserter for fluent validation.
     */
    public ResponseAsserter execute(Request request) {
        Supplier<MeasuredResponse> measuredRequestSupplier = () -> {
            logger.info("Executing request: {} {}", request.method(), request.url());
            long startTime = System.nanoTime();
            try (Response response = httpClient.newCall(request).execute()) {
                long endTime = System.nanoTime();
                Duration executionTime = Duration.ofNanos(endTime - startTime);
                var measuredResponse = new MeasuredResponse(response, executionTime, objectMapper);
                logger.info("Request completed. Status: {}, URL: {}, Duration: {}ms",
                        response.code(), request.url(), executionTime.toMillis());
                return measuredResponse;
            } catch (IOException e) {
                logger.error("API request execution failed for {}: {}", request.url(), e.getMessage());
                // This re-throw is critical for the retry mechanism to catch it.
                throw new RuntimeException("API request execution failed due to network error for " + request.url(), e);
            }
        };

        // Decorate the supplier with retry logic and execute it.
        MeasuredResponse response = Retry.decorateSupplier(this.retry, measuredRequestSupplier).get();
        return new ResponseAsserter(response);
    }

    @Override
    @PreDestroy
    public void close() {
        logger.info("Closing ApiClient resources...");
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        logger.info("ApiClient resources closed.");
    }
}
