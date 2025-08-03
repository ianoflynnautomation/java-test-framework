package solutions.bjjeire.api.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.MDC;
import solutions.bjjeire.api.models.ApiAssertionException;
import solutions.bjjeire.api.models.errors.ValidationErrorResponse;
import solutions.bjjeire.api.telemetry.MetricsCollector;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


/**
 * Provides a fluent API for validating ApiResponse objects.
 * Supports status code assertions, content checks, schema validation, and more.
 * Enhanced to record assertion metrics.
 */
public class ResponseValidator {
    private final ApiResponse response;
    private final MetricsCollector metricsCollector; // Added MetricsCollector dependency

    public ResponseValidator(ApiResponse response, MetricsCollector metricsCollector) {
        this.response = response;
        this.metricsCollector = metricsCollector;
    }

    /**
     * Helper to retrieve the current test name from MDC for metric labeling.
     * @return The current test name or "unknown_test" if not found.
     */
    private String getCurrentTestName() {
        String testName = MDC.get("test_name");
        return testName != null ? testName : "unknown_test";
    }

    /**
     * Asserts that the response status code matches the expected value.
     * @param expectedStatusCode The expected HTTP status code.
     * @return The validator instance for chaining.
     * @throws ApiAssertionException if the status codes do not match.
     */
    public ResponseValidator statusCode(int expectedStatusCode) {
        String currentTestName = getCurrentTestName();
        try {
            if (response.getStatusCode() != expectedStatusCode) {
                throw new ApiAssertionException(
                        String.format("Expected status code <%d> but was <%d>.", expectedStatusCode, response.getStatusCode()),
                        response.getRequestPath(), response.getBodyAsString());
            }
            metricsCollector.recordAssertionSuccess(currentTestName);
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw e;
        }
        return this;
    }

    /**
     * Convenience method to assert a 400 Bad Request status code, typically for validation errors.
     * @return The validator instance for chaining.
     */
    public ResponseValidator validationError() {
        return statusCode(400); // This will now also record metrics
    }

    /**
     * Asserts that the response body contains a specific substring.
     * @param expectedSubstring The substring expected in the response body.
     * @return The validator instance for chaining.
     * @throws ApiAssertionException if the substring is not found.
     */
    public ResponseValidator contentContains(String expectedSubstring) {
        String currentTestName = getCurrentTestName();
        String body = response.getBodyAsString();
        try {
            if (body == null || !body.contains(expectedSubstring)) {
                throw new ApiAssertionException(
                        String.format("Response content did not contain the expected substring '%s'.", expectedSubstring),
                        response.getRequestPath(), body);
            }
            metricsCollector.recordAssertionSuccess(currentTestName);
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw e;
        }
        return this;
    }

    /**
     * Asserts that the request execution time is under a specified maximum duration.
     * @param expectedMaxDuration The maximum allowed execution duration.
     * @return The validator instance for chaining.
     * @throws ApiAssertionException if the execution time exceeds the maximum.
     */
    public ResponseValidator executionTimeUnder(Duration expectedMaxDuration) {
        String currentTestName = getCurrentTestName();
        try {
            if (response.getExecutionTime().compareTo(expectedMaxDuration) > 0) {
                throw new ApiAssertionException(
                        String.format("Request execution time %s was over the expected max of %s.", response.getExecutionTime(), expectedMaxDuration),
                        response.getRequestPath(), response.getBodyAsString());
            }
            metricsCollector.recordAssertionSuccess(currentTestName);
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw e;
        }
        return this;
    }

    /**
     * Validates the response body against a JSON schema loaded from the classpath.
     * Uses networknt/json-schema-validator for schema validation.
     * @param schemaPath The path to the JSON schema file in the classpath (e.g., "schemas/my-response-schema.json").
     * @return The validator instance for chaining.
     * @throws ApiAssertionException if the schema file is not found or validation fails.
     */
    public ResponseValidator matchesJsonSchemaInClasspath(String schemaPath) {
        String currentTestName = getCurrentTestName();
        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(schemaPath)) {
            if (schemaStream == null) {
                throw new ApiAssertionException("Schema file not found in classpath: " + schemaPath, response.getRequestPath(), response.getBodyAsString());
            }
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7); // Using Draft 7
            JsonSchema schema = factory.getSchema(schemaStream);
            JsonNode jsonNode = response.as(JsonNode.class); // Deserialize response body to JsonNode

            Set<ValidationMessage> errors = schema.validate(jsonNode); // Perform validation
            if (!errors.isEmpty()) {
                String errorDetails = errors.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\n- ", "\n- ", ""));
                throw new ApiAssertionException("JSON schema validation failed:" + errorDetails, response.getRequestPath(), response.getBodyAsString());
            }
            metricsCollector.recordAssertionSuccess(currentTestName);
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw e;
        } catch (Exception e) {
            metricsCollector.recordAssertionFailure(currentTestName); // Also record failure for unexpected exceptions
            throw new ApiAssertionException("Failed during JSON schema validation.", response.getRequestPath(), response.getBodyAsString(), e);
        }
        return this;
    }

    /**
     * Asserts that the number of validation errors in the response matches the expected count.
     * Requires the response body to be deserializable into a ValidationErrorResponse.
     * @param expectedCount The expected number of errors.
     * @return The validator instance for chaining.
     * @throws ApiAssertionException if the error count does not match.
     */
    public ResponseValidator errorCount(int expectedCount) {
        String currentTestName = getCurrentTestName();
        try {
            ValidationErrorResponse errorResponse = response.as(ValidationErrorResponse.class);
            int actualCount = errorResponse.errors().size();
            if (actualCount != expectedCount) {
                throw new ApiAssertionException(
                        String.format("Expected <%d> validation errors but found <%d>.", expectedCount, actualCount),
                        response.getRequestPath(), response.getBodyAsString());
            }
            metricsCollector.recordAssertionSuccess(currentTestName);
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw e;
        }
        return this;
    }

    /**
     * Asserts that a specific error message exists for a given field in the validation error response.
     * @param field The field name associated with the error.
     * @param expectedMessage The expected error message for that field.
     * @return The validator instance for chaining.
     * @throws ApiAssertionException if the specific error is not found.
     */
    public ResponseValidator containsErrorForField(String field, String expectedMessage) {
        String currentTestName = getCurrentTestName();
        try {
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
            metricsCollector.recordAssertionSuccess(currentTestName);
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw e;
        }
        return this;
    }

    /**
     * Asserts that all expected field-error message pairs are present in the validation error response.
     * @param expectedErrors A map where keys are field names and values are expected error messages.
     * @return The validator instance for chaining.
     */
    public ResponseValidator containsAllErrors(Map<String, String> expectedErrors) {
        // This method calls containsErrorForField, which now handles its own metrics.
        expectedErrors.forEach(this::containsErrorForField);
        // No need to record success here, as individual calls to containsErrorForField do it.
        return this;
    }

    /**
     * Extracts a value from the JSON response body using a JSONPath expression and asserts its equality.
     *
     * @param jsonPathExpression The JSONPath expression (e.g., "$.data.id").
     * @param expectedValue The expected value to compare against.
     * @return The validator instance for chaining.
     * @throws ApiAssertionException if the JSONPath is invalid, not found, or the value does not match.
     */
    public ResponseValidator jsonPath(String jsonPathExpression, Object expectedValue) {
        String currentTestName = getCurrentTestName();
        String body = response.getBodyAsString();
        try {
            if (body == null || body.isBlank()) {
                throw new ApiAssertionException("Cannot perform JSONPath assertion on an empty response body.", response.getRequestPath(), "");
            }
            Object actualValue = JsonPath.read(body, jsonPathExpression);
            assertThat(actualValue)
                    .as("JSONPath '%s' value assertion failed. Expected <%s> but was <%s>.", jsonPathExpression, expectedValue, actualValue)
                    .isEqualTo(expectedValue);
            metricsCollector.recordAssertionSuccess(currentTestName);
        } catch (PathNotFoundException e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw new ApiAssertionException(
                    String.format("JSONPath expression '%s' not found in response body.", jsonPathExpression),
                    response.getRequestPath(), body, e);
        } catch (AssertionError e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw new ApiAssertionException(
                    String.format("JSONPath '%s' value assertion failed: %s", jsonPathExpression, e.getMessage()),
                    response.getRequestPath(), body, e);
        } catch (Exception e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw new ApiAssertionException(
                    String.format("Failed to evaluate JSONPath '%s'. Error: %s", jsonPathExpression, e.getMessage()),
                    response.getRequestPath(), body, e);
        }
        return this;
    }

    /**
     * Extracts a value from the JSON response body using a JSONPath expression and allows custom assertions
     * on the extracted value using a Consumer.
     *
     * @param jsonPathExpression The JSONPath expression (e.g., "$.data.items[0].name").
     * @param consumer A Consumer that takes the extracted object and performs assertions.
     * @return The validator instance for chaining.
     * @throws ApiAssertionException if the JSONPath is invalid, not found, or the custom assertion fails.
     */
    public ResponseValidator jsonPath(String jsonPathExpression, Consumer<Object> consumer) {
        String currentTestName = getCurrentTestName();
        String body = response.getBodyAsString();
        try {
            if (body == null || body.isBlank()) {
                throw new ApiAssertionException("Cannot perform JSONPath assertion on an empty response body.", response.getRequestPath(), "");
            }
            Object extractedValue = JsonPath.read(body, jsonPathExpression);
            consumer.accept(extractedValue);
            metricsCollector.recordAssertionSuccess(currentTestName);
        } catch (PathNotFoundException e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw new ApiAssertionException(
                    String.format("JSONPath expression '%s' not found in response body.", jsonPathExpression),
                    response.getRequestPath(), body, e);
        } catch (AssertionError e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw new ApiAssertionException(
                    String.format("Custom JSONPath assertion failed for '%s': %s", jsonPathExpression, e.getMessage()),
                    response.getRequestPath(), body, e);
        } catch (Exception e) {
            metricsCollector.recordAssertionFailure(currentTestName);
            throw new ApiAssertionException(
                    String.format("Failed to evaluate JSONPath '%s'. Error: %s", jsonPathExpression, e.getMessage()),
                    response.getRequestPath(), body, e);
        }
        return this;
    }

    /**
     * Allows custom assertions on the deserialized response body using a Consumer.
     * This provides flexibility for complex or specific body validations.
     * @param type The class to deserialize the response body into.
     * @param consumer A Consumer that takes an instance of the deserialized body and performs assertions.
     * @param <T> The generic type of the response body.
     * @return The validator instance for chaining.
     * @throws ApiAssertionException if the custom assertion fails.
     */
    public <T> ResponseValidator bodySatisfies(Class<T> type, Consumer<T> consumer) {
        String currentTestName = getCurrentTestName();
        T bodyAsObject = response.as(type);
        try {
            consumer.accept(bodyAsObject);
            metricsCollector.recordAssertionSuccess(currentTestName);
        } catch (AssertionError e) {
            metricsCollector.recordAssertionFailure(currentTestName);
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