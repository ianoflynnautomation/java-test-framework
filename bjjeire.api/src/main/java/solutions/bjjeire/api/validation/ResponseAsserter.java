package solutions.bjjeire.api.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.Getter;
import solutions.bjjeire.api.models.ApiAssertionException;
import solutions.bjjeire.api.models.MeasuredResponse;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Provides a rich, fluent API for asserting conditions on a MeasuredResponse.
 * Each assertion method returns `this` to allow for chaining multiple assertions.
 * If an assertion fails, it throws a detailed ApiAssertionException.
 */
public class ResponseAsserter {

    @Getter // This exposes the getResponse() method
    private final MeasuredResponse response;
    private final ObjectMapper objectMapper;

    public ResponseAsserter(MeasuredResponse response) {
        this.response = Objects.requireNonNull(response, "MeasuredResponse cannot be null.");
        this.objectMapper = response.getObjectMapper();
    }

    /**
     * Syntactic sugar to improve readability of chained assertions.
     * @return The current ResponseAsserter instance.
     */
    public ResponseAsserter then() { return this; }
    public ResponseAsserter and() { return this; }

    /**
     * Deserializes the response body into a specific class.
     * @param type The class to deserialize the JSON body into.
     * @param <T> The type of the target class.
     * @return An instance of the specified class.
     * @throws ApiAssertionException if the body is empty or deserialization fails.
     */
    public <T> T as(Class<T> type) {
        String body = response.responseBodyAsString();
        if (body == null || body.isEmpty()) {
            throw new ApiAssertionException("Cannot deserialize response body because it is empty.", response.requestUrl(), "");
        }
        try {
            return objectMapper.readValue(body, type);
        } catch (IOException e) {
            throw new ApiAssertionException(
                    String.format("Failed to deserialize response body to type '%s'. Error: %s", type.getSimpleName(), e.getMessage()),
                    response.requestUrl(),
                    body,
                    e
            );
        }
    }

    public ResponseAsserter hasStatusCode(int expectedStatusCode) {
        if (response.statusCode() != expectedStatusCode) {
            throw new ApiAssertionException(
                    String.format("Expected status code <%d> but was <%d>.", expectedStatusCode, response.statusCode()),
                    response.requestUrl(),
                    response.responseBodyAsString()
            );
        }
        return this;
    }

    public ResponseAsserter contentContains(String expectedSubstring) {
        String body = response.responseBodyAsString();
        if (body == null || !body.contains(expectedSubstring)) {
            throw new ApiAssertionException(
                    String.format("Response content did not contain the expected substring '%s'.", expectedSubstring),
                    response.requestUrl(),
                    body
            );
        }
        return this;
    }

    public ResponseAsserter hasExecutionTimeUnder(Duration expectedMaxDuration) {
        if (response.executionTime().compareTo(expectedMaxDuration) > 0) {
            throw new ApiAssertionException(
                    String.format("Request execution time %s was over the expected max of %s.", response.executionTime(), expectedMaxDuration),
                    response.requestUrl(),
                    response.responseBodyAsString()
            );
        }
        return this;
    }

    public ResponseAsserter matchesJsonSchema(String schemaContent) {
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaContent);
            JsonNode jsonNode = objectMapper.readTree(response.responseBodyAsString());

            Set<ValidationMessage> errors = schema.validate(jsonNode);
            if (!errors.isEmpty()) {
                String errorDetails = errors.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\n- ", "\n- ", ""));
                throw new ApiAssertionException("JSON schema validation failed:" + errorDetails, response.requestUrl(), response.responseBodyAsString());
            }
        } catch (Exception e) {
            throw new ApiAssertionException("Failed during JSON schema validation.", response.requestUrl(), response.responseBodyAsString(), e);
        }
        return this;
    }

    public ResponseAsserter matchesXmlSchema(String schemaContent) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(new StringReader(schemaContent)));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(response.responseBodyAsString())));
        } catch (Exception e) {
            throw new ApiAssertionException("XML schema validation failed: " + e.getMessage(), response.requestUrl(), response.responseBodyAsString(), e);
        }
        return this;
    }

    /**
     * Performs custom assertions on the deserialized response body.
     * @param type The class to deserialize the body to.
     * @param consumer A consumer containing assertions (e.g., from AssertJ or JUnit).
     * @param <T> The type of the response body.
     * @return The current ResponseAsserter instance.
     */
    public <T> ResponseAsserter bodySatisfies(Class<T> type, Consumer<T> consumer) {
        T body = as(type);
        try {
            consumer.accept(body);
        } catch (AssertionError e) {
            throw new ApiAssertionException("Custom body assertion failed: " + e.getMessage(),
                    response.requestUrl(),
                    response.responseBodyAsString(), e);
        }
        return this;
    }
}