package solutions.bjjeire.api.telemetry;

import io.opentelemetry.api.trace.Span;
import solutions.bjjeire.api.client.ApiRequest;
import solutions.bjjeire.api.validation.ApiResponse;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

public class TracingManager {
    public void recordAssertionSuccess(Span span, String testName) {
        span.addEvent("assertion.success", io.opentelemetry.api.common.Attributes.of(
                stringKey("event.name"), "assertion.success",
                stringKey("test.name"), testName
        ));
    }

    public void recordAssertionFailure(Span span, String testName, String errorMessage) {
        span.addEvent("assertion.failure", io.opentelemetry.api.common.Attributes.of(
                stringKey("event.name"), "assertion.failure",
                stringKey("test.name"), testName,
                stringKey("error.message"), errorMessage
        ));
    }
}