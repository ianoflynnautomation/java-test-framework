package solutions.bjjeire.api.validation;

import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.telemetry.MetricsCollector;
import solutions.bjjeire.api.telemetry.TracingManager;

@Component
public class ResponseValidatorFactory {
    private final MetricsCollector metricsCollector;
    private final TracingManager tracingManager;
    private final Tracer tracer;

    @Autowired
    public ResponseValidatorFactory(MetricsCollector metricsCollector, TracingManager tracingManager, Tracer tracer) {
        this.metricsCollector = metricsCollector;
        this.tracingManager = tracingManager;
        this.tracer = tracer;
    }

    public ResponseValidator validate(ApiResponse apiResponse) {
        return new ResponseValidator(apiResponse, metricsCollector, tracingManager, tracer);
    }
}