package solutions.bjjeire.api.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import solutions.bjjeire.api.models.ApiAssertionException;
import solutions.bjjeire.api.models.errors.ValidationErrorResponse;
import solutions.bjjeire.api.telemetry.MetricsCollector;
import solutions.bjjeire.api.telemetry.TracingManager;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ResponseValidator {
    private final ApiResponse response;
    private final MetricsCollector metricsCollector;
    private final TracingManager tracingManager;
    private final Tracer tracer;

    public ResponseValidator(ApiResponse response, MetricsCollector metricsCollector, TracingManager tracingManager, Tracer tracer) {
        this.response = response;
        this.metricsCollector = metricsCollector;
        this.tracingManager = tracingManager;
        this.tracer = tracer;
    }

    private String getCurrentTestName() {
        return Baggage.current().getEntryValue("test.name") != null ? Baggage.current().getEntryValue("test.name") : "unknown_test";
    }

    private String getCurrentTestSuite() {
        return Baggage.current().getEntryValue("test.suite") != null ? Baggage.current().getEntryValue("test.suite") : "unknown_suite";
    }

    public ResponseValidator statusCode(int expectedStatusCode) {
        String testName = getCurrentTestName();
        String testSuite = getCurrentTestSuite();
        Span span = tracer.spanBuilder("assertion.status_code")
                .setAttribute("assertion.type", "status_code") // Add assertion type
                .setAttribute("expected.status_code", expectedStatusCode)
                .setAttribute("actual.status_code", response.getStatusCode())
                .startSpan();
        try (var scope = span.makeCurrent()) {
            if (response.getStatusCode() != expectedStatusCode) {
                throw new ApiAssertionException(
                        String.format("Expected status code <%d> but was <%d>.", expectedStatusCode, response.getStatusCode()),
                        response.getRequestPath(), response.getBodyAsString());
            }
            metricsCollector.recordAssertionSuccess(testName, testSuite);
            tracingManager.recordAssertionSuccess(span, testName);
            span.setAttribute("assertion.status", "success");
            span.setStatus(StatusCode.OK);
            return this;
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    public ResponseValidator validationError() {
        return statusCode(400);
    }

    public ResponseValidator contentContains(String expectedSubstring) {
        String testName = getCurrentTestName();
        String testSuite = getCurrentTestSuite();
        Span span = tracer.spanBuilder("assertion.content_contains")
                .setAttribute("assertion.type", "content_contains") // Add assertion type
                .setAttribute("expected.substring", expectedSubstring)
                .startSpan();
        try (var scope = span.makeCurrent()) {
            String body = response.getBodyAsString();
            if (body == null || !body.contains(expectedSubstring)) {
                throw new ApiAssertionException(
                        String.format("Response content did not contain the expected substring '%s'.", expectedSubstring),
                        response.getRequestPath(), body);
            }
            metricsCollector.recordAssertionSuccess(testName, testSuite);
            tracingManager.recordAssertionSuccess(span, testName);
            span.setAttribute("assertion.status", "success");
            span.setStatus(StatusCode.OK);
            return this;
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    public ResponseValidator executionTimeUnder(Duration expectedMaxDuration) {
        String testName = getCurrentTestName();
        String testSuite = getCurrentTestSuite();
        Span span = tracer.spanBuilder("assertion.execution_time")
                .setAttribute("assertion.type", "execution_time") // Add assertion type
                .setAttribute("expected.max_duration_ms", expectedMaxDuration.toMillis())
                .setAttribute("actual.duration_ms", response.getExecutionTime().toMillis())
                .startSpan();
        try (var scope = span.makeCurrent()) {
            if (response.getExecutionTime().compareTo(expectedMaxDuration) > 0) {
                throw new ApiAssertionException(
                        String.format("Request execution time %s was over the expected max of %s.", response.getExecutionTime(), expectedMaxDuration),
                        response.getRequestPath(), response.getBodyAsString());
            }
            metricsCollector.recordAssertionSuccess(testName, testSuite);
            tracingManager.recordAssertionSuccess(span, testName);
            span.setAttribute("assertion.status", "success");
            span.setStatus(StatusCode.OK);
            return this;
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    public ResponseValidator matchesJsonSchemaInClasspath(String schemaPath) {
        String testName = getCurrentTestName();
        String testSuite = getCurrentTestSuite();
        Span span = tracer.spanBuilder("assertion.json_schema")
                .setAttribute("assertion.type", "json_schema") // Add assertion type
                .setAttribute("schema.path", schemaPath)
                .startSpan();
        try (var scope = span.makeCurrent()) {
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
                metricsCollector.recordAssertionSuccess(testName, testSuite);
                tracingManager.recordAssertionSuccess(span, testName);
                span.setAttribute("assertion.status", "success");
                span.setStatus(StatusCode.OK);
                return this;
            } catch (ApiAssertionException e) {
                metricsCollector.recordAssertionFailure(testName, testSuite);
                tracingManager.recordAssertionFailure(span, testName, e.getMessage());
                span.setAttribute("assertion.status", "failure");
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, e.getMessage());
                throw e;
            } catch (Exception e) {
                metricsCollector.recordAssertionFailure(testName, testSuite);
                tracingManager.recordAssertionFailure(span, testName, e.getMessage());
                span.setAttribute("assertion.status", "failure");
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, e.getMessage());
                throw new ApiAssertionException("Failed during JSON schema validation.", response.getRequestPath(), response.getBodyAsString(), e);
            }
        } finally {
            span.end();
        }
    }

    public ResponseValidator errorCount(int expectedCount) {
        String testName = getCurrentTestName();
        String testSuite = getCurrentTestSuite();
        Span span = tracer.spanBuilder("assertion.error_count")
                .setAttribute("assertion.type", "error_count") // Add assertion type
                .setAttribute("expected.error_count", expectedCount)
                .startSpan();
        try (var scope = span.makeCurrent()) {
            ValidationErrorResponse errorResponse = response.as(ValidationErrorResponse.class);
            int actualCount = errorResponse.errors().size();
            if (actualCount != expectedCount) {
                throw new ApiAssertionException(
                        String.format("Expected <%d> validation errors but found <%d>.", expectedCount, actualCount),
                        response.getRequestPath(), response.getBodyAsString());
            }
            metricsCollector.recordAssertionSuccess(testName, testSuite);
            tracingManager.recordAssertionSuccess(span, testName);
            span.setAttribute("assertion.status", "success");
            span.setStatus(StatusCode.OK);
            return this;
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    public ResponseValidator containsErrorForField(String field, String expectedMessage) {
        String testName = getCurrentTestName();
        String testSuite = getCurrentTestSuite();
        Span span = tracer.spanBuilder("assertion.error_for_field")
                .setAttribute("assertion.type", "error_for_field") // Add assertion type
                .setAttribute("field", field)
                .setAttribute("expected.message", expectedMessage)
                .startSpan();
        try (var scope = span.makeCurrent()) {
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
            metricsCollector.recordAssertionSuccess(testName, testSuite);
            tracingManager.recordAssertionSuccess(span, testName);
            span.setAttribute("assertion.status", "success");
            span.setStatus(StatusCode.OK);
            return this;
        } catch (ApiAssertionException e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    public ResponseValidator containsAllErrors(Map<String, String> expectedErrors) {
        expectedErrors.forEach(this::containsErrorForField);
        return this;
    }

    public ResponseValidator jsonPath(String jsonPathExpression, Object expectedValue) {
        String testName = getCurrentTestName();
        String testSuite = getCurrentTestSuite();
        Span span = tracer.spanBuilder("assertion.json_path")
                .setAttribute("assertion.type", "json_path") // Add assertion type
                .setAttribute("jsonpath.expression", jsonPathExpression)
                .setAttribute("expected.value", String.valueOf(expectedValue))
                .startSpan();
        String body = null;
        try (var scope = span.makeCurrent()) {
            body = response.getBodyAsString();
            if (body == null || body.isBlank()) {
                throw new ApiAssertionException("Cannot perform JSONPath assertion on an empty response body.", response.getRequestPath(), "");
            }
            Object actualValue = JsonPath.read(body, jsonPathExpression);
            span.setAttribute("actual.value", String.valueOf(actualValue));
            assertThat(actualValue)
                    .as("JSONPath '%s' value assertion failed. Expected <%s> but was <%s>.", jsonPathExpression, expectedValue, actualValue)
                    .isEqualTo(expectedValue);
            metricsCollector.recordAssertionSuccess(testName, testSuite);
            tracingManager.recordAssertionSuccess(span, testName);
            span.setAttribute("assertion.status", "success");
            span.setStatus(StatusCode.OK);
            return this;
        } catch (PathNotFoundException e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new ApiAssertionException(
                    String.format("JSONPath expression '%s' not found in response body.", jsonPathExpression),
                    response.getRequestPath(), body, e);
        } catch (AssertionError e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new ApiAssertionException(
                    String.format("JSONPath '%s' value assertion failed: %s", jsonPathExpression, e.getMessage()),
                    response.getRequestPath(), body, e);
        } catch (Exception e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new ApiAssertionException(
                    String.format("Failed to evaluate JSONPath '%s'. Error: %s", jsonPathExpression, e.getMessage()),
                    response.getRequestPath(), body, e);
        } finally {
            span.end();
        }
    }

    public ResponseValidator jsonPath(String jsonPathExpression, Consumer<Object> consumer) {
        String testName = getCurrentTestName();
        String testSuite = getCurrentTestSuite();
        Span span = tracer.spanBuilder("assertion.json_path_custom")
                .setAttribute("assertion.type", "json_path_custom") // Add assertion type
                .setAttribute("jsonpath.expression", jsonPathExpression)
                .startSpan();
        String body = null;
        try (var scope = span.makeCurrent()) {
            body = response.getBodyAsString();
            if (body == null || body.isBlank()) {
                throw new ApiAssertionException("Cannot perform JSONPath assertion on an empty response body.", response.getRequestPath(), "");
            }
            Object extractedValue = JsonPath.read(body, jsonPathExpression);
            consumer.accept(extractedValue);
            metricsCollector.recordAssertionSuccess(testName, testSuite);
            tracingManager.recordAssertionSuccess(span, testName);
            span.setAttribute("assertion.status", "success");
            span.setStatus(StatusCode.OK);
            return this;
        } catch (PathNotFoundException e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new ApiAssertionException(
                    String.format("JSONPath expression '%s' not found in response body.", jsonPathExpression),
                    response.getRequestPath(), body, e);
        } catch (AssertionError e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new ApiAssertionException(
                    String.format("Custom JSONPath assertion failed for '%s': %s", jsonPathExpression, e.getMessage()),
                    response.getRequestPath(), body, e);
        } catch (Exception e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new ApiAssertionException(
                    String.format("Failed to evaluate JSONPath '%s'. Error: %s", jsonPathExpression, e.getMessage()),
                    response.getRequestPath(), body, e);
        } finally {
            span.end();
        }
    }

    public <T> ResponseValidator bodySatisfies(Class<T> type, Consumer<T> consumer) {
        String testName = getCurrentTestName();
        String testSuite = getCurrentTestSuite();
        Span span = tracer.spanBuilder("assertion.body_satisfies")
                .setAttribute("assertion.type", "body_satisfies") // Add assertion type
                .setAttribute("body.type", type.getName())
                .startSpan();
        try (var scope = span.makeCurrent()) {
            T bodyAsObject = response.as(type);
            consumer.accept(bodyAsObject);
            metricsCollector.recordAssertionSuccess(testName, testSuite);
            tracingManager.recordAssertionSuccess(span, testName);
            span.setAttribute("assertion.status", "success");
            span.setStatus(StatusCode.OK);
            return this;
        } catch (AssertionError e) {
            metricsCollector.recordAssertionFailure(testName, testSuite);
            tracingManager.recordAssertionFailure(span, testName, e.getMessage());
            span.setAttribute("assertion.status", "failure");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new ApiAssertionException("Custom body assertion failed: " + e.getMessage(),
                    response.getRequestPath(), response.getBodyAsString(), e);
        } finally {
            span.end();
        }
    }

    public ApiResponse andReturn() {
        return response;
    }
}
