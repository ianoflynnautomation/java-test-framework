package solutions.bjjeire.api.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.telemetry.MetricsCollector;

@Component
public class ResponseValidatorFactory {

    private final MetricsCollector metricsCollector;

    @Autowired
    public ResponseValidatorFactory(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    /**
     * Creates a new instance of ResponseValidator for the given ApiResponse.
     * @param apiResponse The ApiResponse to be validated.
     * @return A new ResponseValidator instance.
     */
    public ResponseValidator validate(ApiResponse apiResponse) {
        return new ResponseValidator(apiResponse, metricsCollector);
    }
}