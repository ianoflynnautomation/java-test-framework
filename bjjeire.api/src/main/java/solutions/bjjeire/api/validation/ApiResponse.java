package solutions.bjjeire.api.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import solutions.bjjeire.api.models.ApiAssertionException;
import solutions.bjjeire.api.models.errors.ValidationErrorResponse;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ApiResponse {
    private final ResponseEntity<String> responseEntity;
    @Getter private final Duration executionTime;
    private final ObjectMapper objectMapper;
    @Getter private final String requestPath;

    public ApiResponse(ResponseEntity<String> responseEntity, Duration executionTime, ObjectMapper objectMapper, String requestPath) {
        this.responseEntity = responseEntity;
        this.executionTime = executionTime;
        this.objectMapper = objectMapper;
        this.requestPath = requestPath;
    }

    public int getStatusCode() { return responseEntity.getStatusCode().value(); }
    public String getBodyAsString() { return responseEntity.getBody(); }

    /**
     * Deserializes the response body into a given type.
     *
     * @param type The class to deserialize the JSON body into.
     * @param <T> The generic type of the class.
     * @return An instance of the specified type.
     * @throws ApiAssertionException if the body is empty or deserialization fails.
     */
    public <T> T as(Class<T> type) {
        String body = getBodyAsString();
        if (body == null || body.isBlank()) {
            throw new ApiAssertionException("Cannot deserialize response body because it is empty.", requestPath, "");
        }
        try {
            return objectMapper.readValue(body, type);
        } catch (IOException e) {
            throw new ApiAssertionException(
                    String.format("Failed to deserialize response body to type '%s'. Error: %s", type.getSimpleName(), e.getMessage()),
                    requestPath, body, e);
        }
    }

    /**
     * The entry point for fluent assertions on this response.
     *
     * @return A ResponseAsserter instance for chaining validation calls.
     */
    public ResponseAsserter then() {
        return new ResponseAsserter(this);
    }
}