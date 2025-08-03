package solutions.bjjeire.api.telemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.client.ApiRequest;
import solutions.bjjeire.api.utils.CorrelationIdGenerator;
import solutions.bjjeire.api.validation.ApiResponse;

import java.util.function.Function;


public class TracingManager {
    public <T> Mono<T> withSpan(Tracer tracer, String spanName, ApiRequest request, Function<Span, Mono<T>> function) {
        return Mono.defer(() -> {
            Span span = tracer.spanBuilder(spanName)
                    .setAttribute("api.request.method", request.getMethod().name())
                    .setAttribute("api.request.path", request.getPath())
                    .setAttribute("correlation_id", CorrelationIdGenerator.get()) // Ensure correlation_id is set
                    .startSpan();

            try (Scope scope = span.makeCurrent()) {
                return function.apply(span)
                        .doOnSuccess(result -> {
                            if (result instanceof ApiResponse response) {
                                span.setAttribute("http.status_code", response.getStatusCode());
                                span.setAttribute("api.execution_time_ms", response.getExecutionTime().toMillis());
                            }
                            span.end();
                        })
                        .doOnError(throwable -> {
                            span.setAttribute("error", true);
                            span.setAttribute("error.message", throwable.getMessage());
                            span.recordException(throwable);
                            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, throwable.getMessage());
                            span.end();
                        })
                        .doOnCancel(() -> {
                            span.addEvent("request_cancelled");
                            span.end();
                        });
            } catch (Exception e) {
                span.setAttribute("error", true);
                span.recordException(e);
                span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, "Failed to initiate request: " + e.getMessage());
                span.end();
                return Mono.error(e);
            }
        });
    }

}