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
import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ValidatableResponse {
    private final ResponseEntity<String> responseEntity;
    private final Duration executionTime;
    private final ObjectMapper objectMapper;
    private final String requestUrl;

    public ValidatableResponse(ResponseEntity<String> responseEntity, Duration executionTime, ObjectMapper objectMapper, String requestUrl) {
        this.responseEntity = responseEntity;
        this.executionTime = executionTime;
        this.objectMapper = objectMapper;
        this.requestUrl = requestUrl;
    }

    public int getStatusCode() {
        return responseEntity.getStatusCode().value();
    }

    public String getBody() {
        return responseEntity.getBody();
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public ValidatableResponse then() { return this; }
    public ValidatableResponse and() { return this; }

    
    public ValidatableResponse hasStatusCode(int expectedStatusCode) {
        if (getStatusCode() != expectedStatusCode) {
            throw new ApiAssertionException(
                    String.format("Expected status code <%d> but was <%d>.", expectedStatusCode, getStatusCode()),
                    requestUrl, getBody());
        }
        return this;
    }

    public ValidatableResponse contentContains(String expectedSubstring) {
        String body = getBody();
        if (body == null || !body.contains(expectedSubstring)) {
            throw new ApiAssertionException(
                    String.format("Response content did not contain the expected substring '%s'.", expectedSubstring),
                    requestUrl, body);
        }
        return this;
    }

    public ValidatableResponse hasExecutionTimeUnder(Duration expectedMaxDuration) {
        if (this.executionTime.compareTo(expectedMaxDuration) > 0) {
            throw new ApiAssertionException(
                    String.format("Request execution time %s was over the expected max of %s.", this.executionTime, expectedMaxDuration),
                    requestUrl, getBody());
        }
        return this;
    }

    public ValidatableResponse matchesJsonSchema(String schemaContent) {
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaContent);
            JsonNode jsonNode = objectMapper.readTree(getBody());

            Set<ValidationMessage> errors = schema.validate(jsonNode);
            if (!errors.isEmpty()) {
                String errorDetails = errors.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\n- ", "\n- ", ""));
                throw new ApiAssertionException("JSON schema validation failed:" + errorDetails, requestUrl, getBody());
            }
        } catch (Exception e) {
            throw new ApiAssertionException("Failed during JSON schema validation.", requestUrl, getBody(), e);
        }
        return this;
    }

    public <T> ValidatableResponse bodySatisfies(Class<T> type, Consumer<T> consumer) {
        T body = as(type);
        try {
            consumer.accept(body);
        } catch (AssertionError e) {
            throw new ApiAssertionException("Custom body assertion failed: " + e.getMessage(),
                    requestUrl, getBody(), e);
        }
        return this;
    }

    public <T> T as(Class<T> type) {
        String body = getBody();
        if (body == null || body.isEmpty()) {
            throw new ApiAssertionException("Cannot deserialize response body because it is empty.", requestUrl, "");
        }
        try {
            return objectMapper.readValue(body, type);
        } catch (IOException e) {
            throw new ApiAssertionException(
                    String.format("Failed to deserialize response body to type '%s'. Error: %s", type.getSimpleName(), e.getMessage()),
                    requestUrl, body, e);
        }
    }
}