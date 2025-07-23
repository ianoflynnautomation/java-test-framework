package solutions.bjjeire.api.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable record that holds the results of an API call.
 * It captures the raw OkHttp response, execution time, and provides access
 * to the raw response body and the ObjectMapper used for potential deserialization.
 * This class is a simple, thread-safe data carrier.
 */
public record MeasuredResponse(
        Response rawResponse,
        Duration executionTime,
        ObjectMapper objectMapper,
        String responseBodyAsString,
        String requestUrl,
        int statusCode
) {

    /**
     * Primary constructor that processes the raw OkHttp Response.
     *
     * @param rawResponse    The raw response from OkHttp.
     * @param executionTime  The measured duration of the request.
     * @param objectMapper   The ObjectMapper instance for deserialization.
     */
    public MeasuredResponse(Response rawResponse, Duration executionTime, ObjectMapper objectMapper) {
        this(
                Objects.requireNonNull(rawResponse),
                Objects.requireNonNull(executionTime),
                Objects.requireNonNull(objectMapper),
                extractBody(rawResponse), // Extracts the body as a string
                rawResponse.request().url().toString(),
                rawResponse.code()
        );
    }

    /**
     * A private helper method to safely extract the response body as a string.
     * This is done once in the constructor to avoid issues with the response body
     * stream being consumed multiple times.
     *
     * @param response The raw OkHttp response.
     * @return The response body as a string, or an empty string if the body is null or cannot be read.
     */
    private static String extractBody(Response response) {
        try (ResponseBody body = response.body()) {
            return (body != null) ? body.string() : "";
        } catch (IOException e) {
            // In a test framework, it's often better to know that reading the body failed.
            // Throwing a runtime exception makes the failure explicit.
            throw new IllegalStateException("Failed to read response body for request: " + response.request().url(), e);
        }
    }

    /**
     * Provides the ObjectMapper for downstream processing (e.g., deserialization).
     *
     * @return The configured ObjectMapper instance.
     */
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }
}