package solutions.bjjeire.api.telemetry;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import solutions.bjjeire.api.client.ApiRequest;
import solutions.bjjeire.api.validation.ApiResponse;

import java.util.concurrent.atomic.AtomicInteger;

public class MetricsCollector {

    public final MeterRegistry meterRegistry;
    private final AtomicInteger currentRunningTestsGauge;

    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.currentRunningTestsGauge = new AtomicInteger(0);

        Gauge.builder("current_running_tests", currentRunningTestsGauge, AtomicInteger::get)
                .description("Number of tests currently in execution")
                .tag("application", "api-tests")
                .register(meterRegistry);
    }

    /**
     * Increments the gauge for currently running tests.
     */
    public void incrementRunningTests() {
        currentRunningTestsGauge.incrementAndGet();
    }

    /**
     * Decrements the gauge for currently running tests.
     */
    public void decrementRunningTests() {
        currentRunningTestsGauge.decrementAndGet();
    }

    /**
     * Records a successful API response.
     * @param response The API response.
     * @param request The API request.
     */
    public void recordSuccess(ApiResponse response, ApiRequest request) {
        String statusCodeGroup = String.valueOf(response.getStatusCode()).charAt(0) + "xx";
        meterRegistry.counter("api_request_total",
                        "endpoint", request.getPath(),
                        "method", request.getMethod().name(),
                        "status_code_group", statusCodeGroup,
                        "status_code", String.valueOf(response.getStatusCode()))
                .increment();

        meterRegistry.timer("api_request_duration_seconds",
                        "endpoint", request.getPath(),
                        "method", request.getMethod().name(),
                        "status_code_group", statusCodeGroup,
                        "status_code", String.valueOf(response.getStatusCode()))
                .record(response.getExecutionTime());

        if (response.getStatusCode() >= 400) {
            meterRegistry.counter("api_request_error_total",
                            "endpoint", request.getPath(),
                            "method", request.getMethod().name(),
                            "status_code", String.valueOf(response.getStatusCode()))
                    .increment();
        }
    }

    /**
     * Records an error during an API request (e.g., network error).
     * @param error The throwable that occurred.
     * @param request The API request.
     */
    public void recordError(Throwable error, ApiRequest request) {
        meterRegistry.counter("api_request_network_error_total",
                        "error_type", error.getClass().getSimpleName(),
                        "endpoint", request.getPath(),
                        "method", request.getMethod().name())
                .increment();
    }

    /**
     * Records a cancelled API request.
     * @param request The API request.
     */
    public void recordCancellation(ApiRequest request) {
        meterRegistry.counter("api_request_cancelled_total",
                        "endpoint", request.getPath(),
                        "method", request.getMethod().name())
                .increment();
    }

    /**
     * Records a successful assertion.
     * @param testName The name of the test where the assertion occurred.
     */
    public void recordAssertionSuccess(String testName) {
        meterRegistry.counter("assertion_success_total", "test_name", testName)
                .increment();
    }

    /**
     * Records a failed assertion.
     * @param testName The name of the test where the assertion occurred.
     */
    public void recordAssertionFailure(String testName) {
        meterRegistry.counter("assertion_failure_total", "test_name", testName)
                .increment();
    }
}