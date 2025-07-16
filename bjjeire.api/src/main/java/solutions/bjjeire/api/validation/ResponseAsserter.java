package solutions.bjjeire.api.validation;

import solutions.bjjeire.api.models.MeasuredResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Provides a rich, fluent API for asserting conditions on a MeasuredResponse.
 * Each assertion method returns `this` to allow for chaining multiple assertions
 * in a readable format. If an assertion fails, it throws an ApiAssertionException.
 */
public class ResponseAsserter<T> {

    private final MeasuredResponse<T> response;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public ResponseAsserter(MeasuredResponse<T> response) {
        this.response = Objects.requireNonNull(response, "MeasuredResponse cannot be null.");
    }

    public int getStatusCode() { return response.getStatusCode(); }
    public String getResponseBodyAsString() { return response.getResponseBodyAsString(); }
    public Optional<T> getData() { return response.getData(); }
    public ResponseAsserter<T> then() { return this; }
    public ResponseAsserter<T> and() { return this; }

    public ResponseAsserter<T> hasStatusCode(int expectedStatusCode) {
        if (response.getStatusCode() != expectedStatusCode) {
            throw new ApiAssertionException(
                    String.format("Expected status code <%d> but was <%d>.", expectedStatusCode, response.getStatusCode()),
                    response.getRawResponse().request().url().toString(),
                    response.getResponseBodyAsString()
            );
        }
        return this;
    }

    public ResponseAsserter<T> contentContains(String expectedSubstring) {
        String body = response.getResponseBodyAsString();
        if (body == null || !body.contains(expectedSubstring)) {
            throw new ApiAssertionException(
                    String.format("Response content did not contain the expected substring '%s'.", expectedSubstring),
                    response.getRawResponse().request().url().toString(),
                    body
            );
        }
        return this;
    }

    public ResponseAsserter<T> hasExecutionTimeUnder(Duration expectedMaxDuration) {
        if (response.getExecutionTime().compareTo(expectedMaxDuration) > 0) {
            throw new ApiAssertionException(
                    String.format("Request execution time %s was over the expected max of %s.", response.getExecutionTime(), expectedMaxDuration),
                    response.getRawResponse().request().url().toString(),
                    response.getResponseBodyAsString()
            );
        }
        return this;
    }

    public ResponseAsserter<T> matchesJsonSchema(String schemaContent) {
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaContent);
            JsonNode jsonNode = objectMapper.readTree(response.getResponseBodyAsString());

            Set<ValidationMessage> errors = schema.validate(jsonNode);
            if (!errors.isEmpty()) {
                String errorDetails = errors.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\n- ", "\n- ", ""));
                throw new ApiAssertionException("JSON schema validation failed:" + errorDetails, response.getRawResponse().request().url().toString(), response.getResponseBodyAsString());
            }
        } catch (Exception e) {
            throw new ApiAssertionException("Failed during JSON schema validation.", response.getRawResponse().request().url().toString(), response.getResponseBodyAsString(), e);
        }
        return this;
    }

    public ResponseAsserter<T> matchesXmlSchema(String schemaContent) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(new StringReader(schemaContent)));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(response.getResponseBodyAsString())));
        } catch (Exception e) {
            throw new ApiAssertionException("XML schema validation failed: " + e.getMessage(), response.getRawResponse().request().url().toString(), response.getResponseBodyAsString(), e);
        }
        return this;
    }

    public ResponseAsserter<T> bodySatisfies(Consumer<T> consumer) {
        T body = response.getData().orElseThrow(() ->
                new ApiAssertionException("Cannot perform custom body assertion because response body is null or failed to deserialize.",
                        response.getRawResponse().request().url().toString(),
                        response.getResponseBodyAsString())
        );
        try {
            consumer.accept(body);
        } catch (AssertionError e) {
            throw new ApiAssertionException("Custom body assertion failed: " + e.getMessage(),
                    response.getRawResponse().request().url().toString(),
                    response.getResponseBodyAsString(), e);
        }
        return this;
    }
}