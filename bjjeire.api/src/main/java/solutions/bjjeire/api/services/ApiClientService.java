package solutions.bjjeire.api.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.Getter;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.models.MeasuredResponse;
import solutions.bjjeire.api.models.NotSuccessfulRequestException;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * A stateless, thread-safe service for executing HTTP requests.
 * It manages a shared OkHttpClient and implements retry logic using Resilience4j.
 * This class is a Spring-managed singleton service.
 */
@Service
public class ApiClientService implements IApiClientService {

    private static final Logger logger = LoggerFactory.getLogger(ApiClientService.class);
    private final OkHttpClient httpClient;
    @Getter
    private final ObjectMapper objectMapper;
    private final Retry retry;

    /**
     * Constructor for Spring dependency injection.
     * @param settings The application's API settings, injected by Spring.
     */
    @Autowired
    public ApiClientService(ApiSettings settings) {
        logger.info("Initializing ApiClientService with timeout of {}s and {} retry attempts.",
                settings.getClientTimeoutSeconds(), settings.getMaxRetryAttempts());

        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

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

    @Override
    public <T> MeasuredResponse<T> execute(Request request, Class<T> responseType) {
        Supplier<MeasuredResponse<T>> measuredRequestSupplier = () -> {
            logger.info("Executing request: {} {}", request.method(), request.url());
            long startTime = System.nanoTime();
            try (Response response = httpClient.newCall(request).execute()) {
                long endTime = System.nanoTime();
                Duration executionTime = Duration.ofNanos(endTime - startTime);

                if (!response.isSuccessful()) {
                    String errorBody = getErrorBody(response);
                    logger.warn("Request failed. Status: {}, URL: {}, Body: {}", response.code(), request.url(), errorBody);
                    throw new NotSuccessfulRequestException(
                            String.format("Request failed with status: %d. Body: %s", response.code(), errorBody)
                    );
                }

                var measuredResponse = new MeasuredResponse<>(response, executionTime, objectMapper, responseType);
                logger.info("Request successful. Status: {}, URL: {}, Duration: {}ms",
                        response.code(), request.url(), executionTime.toMillis());
                return measuredResponse;

            } catch (IOException e) {
                logger.error("API request execution failed for {}", request.url(), e);
                throw new RuntimeException("API request execution failed for " + request.url(), e);
            }
        };

        return Retry.decorateSupplier(this.retry, measuredRequestSupplier).get();
    }

    private String getErrorBody(Response response) {
        try (ResponseBody body = response.body()) {
            if (body != null) {
                return body.string();
            }
        } catch (IOException e) {
            logger.error("Could not read error response body for request to {}", response.request().url(), e);
        }
        return "[No response body or body could not be read]";
    }

    @Override
    public void close() {
        logger.info("Closing ApiClientService resources...");
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        logger.info("ApiClientService resources closed.");
    }
}
