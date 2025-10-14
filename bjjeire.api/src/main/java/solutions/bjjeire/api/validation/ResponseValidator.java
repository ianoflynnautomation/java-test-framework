package solutions.bjjeire.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.InputStream;
import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import solutions.bjjeire.api.exceptions.ApiAssertionException;
import solutions.bjjeire.api.models.errors.ValidationErrorResponse;

@RequiredArgsConstructor
public class ResponseValidator {

  private final ApiResponse response;

  public ResponseValidator and() {
    return this;
  }

  public ResponseValidator haveStatusCode(int expectedStatusCode) {
    return executeAssertion(
        () ->
            assertThat(response.getStatusCode())
                .withFailMessage(
                    "Expected status code <%d> but was <%d>",
                    expectedStatusCode, response.getStatusCode())
                .isEqualTo(expectedStatusCode));
  }

  public ResponseValidator beOk() {
    return haveStatusCode(200);
  }

  public ResponseValidator beCreated() {
    return haveStatusCode(201);
  }

  public ResponseValidator beBadRequest() {
    return haveStatusCode(400);
  }

  public ResponseValidator beUnauthorized() {
    return haveStatusCode(401);
  }

  public ResponseValidator beForbidden() {
    return haveStatusCode(403);
  }

  public ResponseValidator beNotFound() {
    return haveStatusCode(404);
  }

  public ResponseValidator haveHeader(String headerName) {
    return executeAssertion(
        () ->
            assertThat(response.getHeaders().containsKey(headerName))
                .withFailMessage(
                    "Expected header '%s' to be present, but it was not found.", headerName)
                .isTrue());
  }

  public ResponseValidator haveHeader(String headerName, String expectedValue) {
    return executeAssertion(
        () -> {
          haveHeader(headerName);
          assertThat(response.header(headerName))
              .withFailMessage(
                  "Expected header '%s' to have value '%s', but was '%s'.",
                  headerName, expectedValue, response.header(headerName))
              .isEqualTo(expectedValue);
        });
  }

  public ResponseValidator containText(String expectedSubstring) {
    return executeAssertion(
        () -> {
          String body = getAndValidateBody();
          assertThat(body)
              .withFailMessage(
                  "Response content did not contain the expected substring '%s'", expectedSubstring)
              .contains(expectedSubstring);
        });
  }

  public ResponseValidator matchSchema(String schemaPath) {
    try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(schemaPath)) {
      if (schemaStream == null) {
        throw new ApiAssertionException(
            String.format("Schema file not found in classpath: %s", schemaPath),
            response.getRequestPath(),
            "Schema validation could not be performed.");
      }

      JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
      JsonSchema schema = factory.getSchema(schemaStream);
      JsonNode jsonNode = response.asJsonNode();

      Set<ValidationMessage> errors = schema.validate(jsonNode);

      if (!errors.isEmpty()) {
        String validationErrors =
            errors.stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.joining("\n- ", "\n- ", ""));
        throw new AssertionError("JSON schema validation failed with errors:" + validationErrors);
      }

      return this;
    } catch (Exception e) {
      throw new ApiAssertionException(
          "An unexpected error occurred during JSON schema validation.",
          response.getRequestPath(),
          response.getBodyAsString(),
          e);
    }
  }

  public ResponseValidator containErrorForField(String field, String expectedMessage) {
    return executeAssertion(
        () -> {
          ValidationErrorResponse errorResponse = response.as(ValidationErrorResponse.class);
          assertThat(errorResponse.errors())
              .withFailMessage(
                  "Expected to find error for field '%s' with message '%s', but it was not found.",
                  field, expectedMessage)
              .anyMatch(
                  error -> field.equals(error.field()) && expectedMessage.equals(error.message()));
        });
  }

  public ResponseValidator haveJsonPathValue(String jsonPathExpression, Object expectedValue) {
    return executeAssertion(
        () -> {
          String body = getAndValidateBody();
          try {
            Object actualValue = JsonPath.read(body, jsonPathExpression);
            assertThat(actualValue)
                .withFailMessage(
                    "JSONPath '%s' assertion failed. Expected <%s> but was <%s>.",
                    jsonPathExpression, expectedValue, actualValue)
                .isEqualTo(expectedValue);
          } catch (PathNotFoundException e) {
            throw new AssertionError(
                String.format(
                    "JSONPath expression '%s' not found in response body.", jsonPathExpression),
                e);
          }
        });
  }

  public <T> ResponseValidator satisfyBody(Class<T> type, Consumer<T> consumer) {
    return executeAssertion(
        () -> {
          T bodyAsObject = response.as(type);
          consumer.accept(bodyAsObject);
        });
  }

  public ResponseValidator takeLessThan(Duration maxDuration) {
    return executeAssertion(
        () ->
            assertThat(response.getExecutionTime())
                .withFailMessage(
                    "Request execution time <%s> was not less than or equal to <%s>",
                    response.getExecutionTime(), maxDuration)
                .isLessThanOrEqualTo(maxDuration));
  }

  private String getAndValidateBody() {
    String body = response.getBodyAsString();
    if (body == null || body.isBlank()) {
      throw new ApiAssertionException(
          "Cannot perform assertion because the response body is empty.",
          response.getRequestPath(),
          body);
    }
    return body;
  }

  private ResponseValidator executeAssertion(Runnable assertion) {
    try {
      assertion.run();
      return this;
    } catch (AssertionError e) {
      throw new ApiAssertionException(
          e.getMessage(), response.getRequestPath(), response.getBodyAsString(), e);
    }
  }
}
