package solutions.bjjeire.api.services;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.models.MeasuredResponse;
import solutions.bjjeire.api.models.NotSuccessfulRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * A stateless, thread-safe service for executing HTTP requests.
 * It manages a shared OkHttpClient and implements retry logic using Resilience4j.
 * This class is designed to be a singleton managed by the DI container.
 */
@Getter
public class ApiClientService implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ApiClientService.class);
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Retry retry;

    public ApiClientService(ApiSettings settings) {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(settings.getClientTimeoutSeconds()))
                .readTimeout(Duration.ofSeconds(settings.getClientTimeoutSeconds()))
                .writeTimeout(Duration.ofSeconds(settings.getClientTimeoutSeconds()))
                .build();

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(settings.getMaxRetryAttempts())
                .intervalFunction(IntervalFunction.of(Duration.ofMillis(settings.getPauseBetweenFailuresMillis())))
                .retryOnException(e -> e instanceof NotSuccessfulRequestException || e instanceof IOException)
                .build();
        this.retry = RetryRegistry.of(retryConfig).retry("api-request");
    }

    public <T> MeasuredResponse<T> execute(Request request, Class<T> responseType) {
        Supplier<MeasuredResponse<T>> measuredRequestSupplier = () -> {
            logger.info("Executing request: {} {}", request.method(), request.url());
            long startTime = System.nanoTime();
            try (Response response = httpClient.newCall(request).execute()) {
                long endTime = System.nanoTime();
                Duration executionTime = Duration.ofNanos(endTime - startTime);
                var measuredResponse = new MeasuredResponse<>(response, executionTime, objectMapper, responseType);

                if (!measuredResponse.isSuccessful()) {
                    logger.warn("Request was not successful. Status: {}, URL: {}", response.code(), request.url());
                    throw new NotSuccessfulRequestException(
                            String.format("Request failed with status: %d. Body: %s",
                                    response.code(), measuredResponse.getResponseBodyAsString())
                    );
                }
                logger.info("Request successful. Status: {}, URL: {}, Duration: {}ms",
                        response.code(), request.url(), executionTime.toMillis());
                return measuredResponse;
            } catch (IOException e) {
                throw new RuntimeException("API request execution failed for " + request.url(), e);
            }
        };

        return Retry.decorateSupplier(this.retry, measuredRequestSupplier).get();
    }

    @Override
    public void close() {
        logger.info("Closing ApiClientService resources...");
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        logger.info("ApiClientService resources closed.");
    }
}