package solutions.bjjeire.api.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.http.ResponseEntity;
import solutions.bjjeire.api.models.ApiAssertionException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ValidatableResponse {
    private final ResponseEntity<String> responseEntity;
    private final Duration executionTime;
    private final ObjectMapper objectMapper;
    private final String requestPath;

    public ValidatableResponse(ResponseEntity<String> responseEntity, Duration executionTime, ObjectMapper objectMapper, String requestPath) {
        this.responseEntity = responseEntity;
        this.executionTime = executionTime;
        this.objectMapper = objectMapper;
        this.requestPath = requestPath;
    }

    public int getStatusCode() { return responseEntity.getStatusCode().value(); }
    public String getBody() { return responseEntity.getBody(); }
    public String getRequestPath() { return requestPath; }
    public Duration getExecutionTime() { return executionTime; }

    public ValidatableResponse then() { return this; }
    public ValidatableResponse and() { return this; }

    public ValidatableResponse hasStatusCode(int expectedStatusCode) {
        if (getStatusCode() != expectedStatusCode) {
            throw new ApiAssertionException(
                    String.format("Expected status code <%d> but was <%d>.", expectedStatusCode, getStatusCode()),
                    requestPath, getBody());
        }
        return this;
    }

    public ValidatableResponse contentContains(String expectedSubstring) {
        String body = getBody();
        if (body == null || !body.contains(expectedSubstring)) {
            throw new ApiAssertionException(
                    String.format("Response content did not contain the expected substring '%s'.", expectedSubstring),
                    requestPath, body);
        }
        return this;
    }

    public ValidatableResponse hasExecutionTimeUnder(Duration expectedMaxDuration) {
        if (this.executionTime.compareTo(expectedMaxDuration) > 0) {
            throw new ApiAssertionException(
                    String.format("Request execution time %s was over the expected max of %s.", this.executionTime, expectedMaxDuration),
                    requestPath, getBody());
        }
        return this;
    }

    public ValidatableResponse matchesJsonSchemaInClasspath(String schemaPath) {
        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(schemaPath)) {
            if (schemaStream == null) {
                throw new ApiAssertionException("Schema file not found in classpath: " + schemaPath, requestPath, getBody());
            }
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaStream);
            JsonNode jsonNode = objectMapper.readTree(getBody());

            Set<ValidationMessage> errors = schema.validate(jsonNode);
            if (!errors.isEmpty()) {
                String errorDetails = errors.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\n- ", "\n- ", ""));
                throw new ApiAssertionException("JSON schema validation failed:" + errorDetails, requestPath, getBody());
            }
        } catch (Exception e) {
            throw new ApiAssertionException("Failed during JSON schema validation.", requestPath, getBody(), e);
        }
        return this;
    }

    public <T> ValidatableResponse bodySatisfies(Class<T> type, Consumer<T> consumer) {
        T bodyAsObject = as(type);
        try {
            consumer.accept(bodyAsObject);
        } catch (AssertionError e) {
            throw new ApiAssertionException("Custom body assertion failed: " + e.getMessage(),
                    requestPath, getBody(), e);
        }
        return this;
    }

    public <T> T as(Class<T> type) {
        String body = getBody();
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
}