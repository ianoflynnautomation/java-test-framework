package solutions.bjjeire.api.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import solutions.bjjeire.api.models.ApiAssertionException;
import solutions.bjjeire.api.models.errors.ValidationErrorResponse;

import java.io.InputStream;
import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class ResponseValidator {

    private final ApiResponse response;

    public ResponseValidator statusCode(int expectedStatusCode) {
        return executeAssertion(() -> assertThat(response.getStatusCode())
                .withFailMessage("Expected status code <%d> but was <%d>.", expectedStatusCode,
                        response.getStatusCode())
                .isEqualTo(expectedStatusCode));
    }

    public ResponseValidator contentContains(String expectedSubstring) {
        return executeAssertion(() -> {
            String body = getAndValidateBody();
            assertThat(body)
                    .withFailMessage("Response content did not contain the expected substring '%s'.", expectedSubstring)
                    .contains(expectedSubstring);
        });
    }

    public ResponseValidator executionTimeUnder(Duration maxDuration) {
        return executeAssertion(() -> assertThat(response.getExecutionTime())
                .withFailMessage("Request execution time %s was over the expected max of %s.",
                        response.getExecutionTime(), maxDuration)
                .isLessThanOrEqualTo(maxDuration));
    }

    public ResponseValidator matchesJsonSchemaInClasspath(String schemaPath) {
        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(schemaPath)) {
            assertThat(schemaStream)
                    .withFailMessage("Schema file not found in classpath: %s", schemaPath)
                    .isNotNull();

            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaStream);
            JsonNode jsonNode = response.as(JsonNode.class);

            Set<ValidationMessage> errors = schema.validate(jsonNode);
            assertThat(errors)
                    .withFailMessage(() -> "JSON schema validation failed with errors:\n- " +
                            errors.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\n- ")))
                    .isEmpty();

            return this;
        } catch (Exception e) {
            throw new ApiAssertionException("Failed during JSON schema validation.", response.getRequestPath(),
                    response.getBodyAsString(), e);
        }
    }

    public ResponseValidator containsErrorForField(String field, String expectedMessage) {
        return executeAssertion(() -> {
            ValidationErrorResponse errorResponse = response.as(ValidationErrorResponse.class);
            assertThat(errorResponse.errors())
                    .withFailMessage("Expected to find error for field '%s' with message '%s', but it was not found.",
                            field, expectedMessage)
                    .anyMatch(error -> field.equals(error.field()) && expectedMessage.equals(error.message()));
        });
    }

    public ResponseValidator jsonPath(String jsonPathExpression, Object expectedValue) {
        return executeAssertion(() -> {
            String body = getAndValidateBody();
            try {
                Object actualValue = JsonPath.read(body, jsonPathExpression);
                assertThat(actualValue)
                        .withFailMessage("JSONPath '%s' assertion failed.", jsonPathExpression)
                        .isEqualTo(expectedValue);
            } catch (PathNotFoundException e) {
                throw new AssertionError(
                        String.format("JSONPath expression '%s' not found in response body.", jsonPathExpression), e);
            }
        });
    }

    public <T> ResponseValidator bodySatisfies(Class<T> type, Consumer<T> consumer) {
        return executeAssertion(() -> {
            T bodyAsObject = response.as(type);
            consumer.accept(bodyAsObject);
        });
    }

    private String getAndValidateBody() {
        String body = response.getBodyAsString();
        assertThat(body).withFailMessage("Cannot perform assertion on an empty response body.").isNotNull()
                .isNotBlank();
        return body;
    }

    private ResponseValidator executeAssertion(Runnable assertion) {
        try {
            assertion.run();
            return this;
        } catch (AssertionError e) {
            throw new ApiAssertionException("Custom body assertion failed: " + e.getMessage(),
                    response.getRequestPath(), response.getBodyAsString(), e);
        }
    }
}