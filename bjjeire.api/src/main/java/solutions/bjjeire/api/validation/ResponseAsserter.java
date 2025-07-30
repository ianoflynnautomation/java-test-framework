package solutions.bjjeire.api.validation;


import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import solutions.bjjeire.api.models.ApiAssertionException;
import solutions.bjjeire.api.models.errors.ValidationErrorResponse;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ResponseAsserter {
    private final ApiResponse response;

    public ResponseAsserter(ApiResponse response) {
        this.response = response;
    }

    public ResponseAsserter statusCode(int expectedStatusCode) {
        if (response.getStatusCode() != expectedStatusCode) {
            throw new ApiAssertionException(
                    String.format("Expected status code <%d> but was <%d>.", expectedStatusCode, response.getStatusCode()),
                    response.getRequestPath(), response.getBodyAsString());
        }
        return this;
    }

    public ResponseAsserter validationError() {
        return statusCode(400);
    }

    public ResponseAsserter contentContains(String expectedSubstring) {
        String body = response.getBodyAsString();
        if (body == null || !body.contains(expectedSubstring)) {
            throw new ApiAssertionException(
                    String.format("Response content did not contain the expected substring '%s'.", expectedSubstring),
                    response.getRequestPath(), body);
        }
        return this;
    }

    public ResponseAsserter executionTimeUnder(Duration expectedMaxDuration) {
        if (response.getExecutionTime().compareTo(expectedMaxDuration) > 0) {
            throw new ApiAssertionException(
                    String.format("Request execution time %s was over the expected max of %s.", response.getExecutionTime(), expectedMaxDuration),
                    response.getRequestPath(), response.getBodyAsString());
        }
        return this;
    }

    public ResponseAsserter matchesJsonSchemaInClasspath(String schemaPath) {
        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(schemaPath)) {
            if (schemaStream == null) {
                throw new ApiAssertionException("Schema file not found in classpath: " + schemaPath, response.getRequestPath(), response.getBodyAsString());
            }
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaStream);
            JsonNode jsonNode = response.as(JsonNode.class);

            Set<ValidationMessage> errors = schema.validate(jsonNode);
            if (!errors.isEmpty()) {
                String errorDetails = errors.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\n- ", "\n- ", ""));
                throw new ApiAssertionException("JSON schema validation failed:" + errorDetails, response.getRequestPath(), response.getBodyAsString());
            }
        } catch (Exception e) {
            throw new ApiAssertionException("Failed during JSON schema validation.", response.getRequestPath(), response.getBodyAsString(), e);
        }
        return this;
    }

    public ResponseAsserter errorCount(int expectedCount) {
        ValidationErrorResponse errorResponse = response.as(ValidationErrorResponse.class);
        int actualCount = errorResponse.errors().size();
        if (actualCount != expectedCount) {
            throw new ApiAssertionException(
                    String.format("Expected <%d> validation errors but found <%d>.", expectedCount, actualCount),
                    response.getRequestPath(), response.getBodyAsString());
        }
        return this;
    }

    public ResponseAsserter containsErrorForField(String field, String expectedMessage) {
        ValidationErrorResponse errorResponse = response.as(ValidationErrorResponse.class);
        boolean matchFound = errorResponse.errors().stream()
                .anyMatch(error -> field.equals(error.field()) && expectedMessage.equals(error.message()));

        if (!matchFound) {
            String availableErrors = errorResponse.errors().stream()
                    .map(e -> String.format("  - Field: '%s', Message: '%s'", e.field(), e.message()))
                    .collect(Collectors.joining("\n"));
            throw new ApiAssertionException(
                    String.format("Expected to find error for field '%s' with message '%s', but it was not found.\nAvailable errors:\n%s",
                            field, expectedMessage, availableErrors),
                    response.getRequestPath(), response.getBodyAsString());
        }
        return this;
    }

    public ResponseAsserter containsAllErrors(Map<String, String> expectedErrors) {
        expectedErrors.forEach(this::containsErrorForField);
        return this;
    }

    public <T> ResponseAsserter bodySatisfies(Class<T> type, Consumer<T> consumer) {
        T bodyAsObject = response.as(type);
        try {
            consumer.accept(bodyAsObject);
        } catch (AssertionError e) {
            throw new ApiAssertionException("Custom body assertion failed: " + e.getMessage(),
                    response.getRequestPath(), response.getBodyAsString(), e);
        }
        return this;
    }

    /**
     * A convenience method to return the underlying ApiResponse, for example,
     * to extract the body after assertions have passed.
     *
     * @return The original ApiResponse.
     */
    public ApiResponse andReturn() {
        return response;
    }
}