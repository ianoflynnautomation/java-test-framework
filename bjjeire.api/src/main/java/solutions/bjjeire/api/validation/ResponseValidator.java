package solutions.bjjeire.api.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.opentelemetry.api.baggage.Baggage;
import lombok.RequiredArgsConstructor;
import solutions.bjjeire.api.models.ApiAssertionException;
import solutions.bjjeire.api.models.errors.ValidationErrorResponse;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RequiredArgsConstructor
public class ResponseValidator {
    private final ApiResponse response;

    private String getCurrentTestName() {
        return Baggage.current().getEntryValue("test.name") != null ? Baggage.current().getEntryValue("test.name")
                : "unknown_test";
    }

    private String getCurrentTestSuite() {
        return Baggage.current().getEntryValue("test.suite") != null ? Baggage.current().getEntryValue("test.suite")
                : "unknown_suite";
    }

    public ResponseValidator statusCode(int expectedStatusCode) {
        if (response.getStatusCode() != expectedStatusCode) {
            throw new ApiAssertionException(
                    String.format("Expected status code <%d> but was <%d>.", expectedStatusCode, response.getStatusCode()),
                    response.getRequestPath(), response.getBodyAsString());
        }
        return this;
    }

    public ResponseValidator validationError() {
        return statusCode(400);
    }

    public ResponseValidator contentContains(String expectedSubstring) {
        String body = response.getBodyAsString();
        if (body == null || !body.contains(expectedSubstring)) {
            throw new ApiAssertionException(
                    String.format("Response content did not contain the expected substring '%s'.", expectedSubstring),
                    response.getRequestPath(), body);
        }
        return this;
    }

    public ResponseValidator executionTimeUnder(Duration expectedMaxDuration) {
        if (response.getExecutionTime().compareTo(expectedMaxDuration) > 0) {
            throw new ApiAssertionException(
                    String.format("Request execution time %s was over the expected max of %s.",
                            response.getExecutionTime(), expectedMaxDuration),
                    response.getRequestPath(), response.getBodyAsString());
        }
        return this;
    }

    public ResponseValidator matchesJsonSchemaInClasspath(String schemaPath) {
        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(schemaPath)) {
            if (schemaStream == null) {
                throw new ApiAssertionException("Schema file not found in classpath: " + schemaPath,
                        response.getRequestPath(), response.getBodyAsString());
            }
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaStream);
            JsonNode jsonNode = response.as(JsonNode.class);
            Set<ValidationMessage> errors = schema.validate(jsonNode);
            if (!errors.isEmpty()) {
                String errorDetails = errors.stream().map(ValidationMessage::getMessage)
                        .collect(Collectors.joining("\n- ", "\n- ", ""));
                throw new ApiAssertionException("JSON schema validation failed:" + errorDetails,
                        response.getRequestPath(), response.getBodyAsString());
            }
            return this;
        } catch (Exception e) {
            throw new ApiAssertionException("Failed during JSON schema validation.", response.getRequestPath(),
                    response.getBodyAsString(), e);
        }
    }

    public ResponseValidator errorCount(int expectedCount) {

        try {
            ValidationErrorResponse errorResponse = response.as(ValidationErrorResponse.class);
            int actualCount = errorResponse.errors().size();
            if (actualCount != expectedCount) {
                throw new ApiAssertionException(
                        String.format("Expected <%d> validation errors but found <%d>.", expectedCount, actualCount),
                        response.getRequestPath(), response.getBodyAsString());
            }

            return this;
        } catch (ApiAssertionException e) {

            throw e;
        } finally {

        }
    }

    public ResponseValidator containsErrorForField(String field, String expectedMessage) {

        try {
            ValidationErrorResponse errorResponse = response.as(ValidationErrorResponse.class);
            boolean matchFound = errorResponse.errors().stream()
                    .anyMatch(error -> field.equals(error.field()) && expectedMessage.equals(error.message()));
            if (!matchFound) {
                String availableErrors = errorResponse.errors().stream()
                        .map(e -> String.format("  - Field: '%s', Message: '%s'", e.field(), e.message()))
                        .collect(Collectors.joining("\n"));
                throw new ApiAssertionException(
                        String.format(
                                "Expected to find error for field '%s' with message '%s', but it was not found.\nAvailable errors:\n%s",
                                field, expectedMessage, availableErrors),
                        response.getRequestPath(), response.getBodyAsString());
            }
            return this;
        } catch (ApiAssertionException e) {
            throw e;
        } finally {

        }
    }

    public ResponseValidator containsAllErrors(Map<String, String> expectedErrors) {
        expectedErrors.forEach(this::containsErrorForField);
        return this;
    }

    public ResponseValidator jsonPath(String jsonPathExpression, Object expectedValue) {
        String body = response.getBodyAsString();
        if (body == null || body.isBlank()) {
            throw new ApiAssertionException("Cannot perform JSONPath assertion on an empty response body.",
                    response.getRequestPath(), "");
        }
        try {
            Object actualValue = JsonPath.read(body, jsonPathExpression);
            assertThat(actualValue)
                    .as("JSONPath '%s' assertion failed.", jsonPathExpression)
                    .isEqualTo(expectedValue);
            return this;
        } catch (PathNotFoundException e) {
            throw new ApiAssertionException(
                    String.format("JSONPath expression '%s' not found in response body.", jsonPathExpression),
                    response.getRequestPath(), body, e);
        } catch (AssertionError e) {
            throw new ApiAssertionException(e.getMessage(), response.getRequestPath(), body, e);
        }
    }

    public ResponseValidator jsonPath(String jsonPathExpression, Consumer<Object> consumer) {

        String body = null;
        try {
            body = response.getBodyAsString();
            if (body == null || body.isBlank()) {
                throw new ApiAssertionException("Cannot perform JSONPath assertion on an empty response body.",
                        response.getRequestPath(), "");
            }
            Object extractedValue = JsonPath.read(body, jsonPathExpression);
            consumer.accept(extractedValue);
            return this;
        } catch (PathNotFoundException e) {
            throw new ApiAssertionException(
                    String.format("JSONPath expression '%s' not found in response body.", jsonPathExpression),
                    response.getRequestPath(), body, e);
        } catch (AssertionError e) {
            throw new ApiAssertionException(
                    String.format("Custom JSONPath assertion failed for '%s': %s", jsonPathExpression, e.getMessage()),
                    response.getRequestPath(), body, e);
        } catch (Exception e) {
            throw new ApiAssertionException(
                    String.format("Failed to evaluate JSONPath '%s'. Error: %s", jsonPathExpression, e.getMessage()),
                    response.getRequestPath(), body, e);
        } finally {

        }
    }

    public <T> ResponseValidator bodySatisfies(Class<T> type, Consumer<T> consumer) {

        try{
            T bodyAsObject = response.as(type);
            consumer.accept(bodyAsObject);
            return this;
        } catch (AssertionError e) {
            throw new ApiAssertionException("Custom body assertion failed: " + e.getMessage(),
                    response.getRequestPath(), response.getBodyAsString(), e);
        } finally {

        }
    }

    public ApiResponse andReturn() {
        return response;
    }
}
