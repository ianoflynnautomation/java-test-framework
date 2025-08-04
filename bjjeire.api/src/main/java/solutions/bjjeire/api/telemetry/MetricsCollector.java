package solutions.bjjeire.api.telemetry;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import solutions.bjjeire.api.client.ApiRequest;
import solutions.bjjeire.api.validation.ApiResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MetricsCollector {
    private final MeterRegistry meterRegistry;
    private final AtomicInteger currentRunningTestsGauge;
    private final String serviceName;
    private final String environment;

    public MetricsCollector(MeterRegistry meterRegistry, String serviceName, String environment) {
        this.meterRegistry = meterRegistry;
        this.serviceName = serviceName;
        this.environment = environment;
        this.currentRunningTestsGauge = new AtomicInteger(0);

        Gauge.builder("current_running_tests", currentRunningTestsGauge, AtomicInteger::get)
                .description("Number of tests currently in execution")
                .tags("service.name", serviceName, "environment", environment)
                .register(meterRegistry);
    }

    public void incrementRunningTests() {
        currentRunningTestsGauge.incrementAndGet();
    }

    public void decrementRunningTests() {
        currentRunningTestsGauge.decrementAndGet();
    }

    public void recordSuccess(ApiResponse response, ApiRequest request, Span span) {
        String statusCodeGroup = getStatusCodeGroup(response.getStatusCode());
        List<Tag> tags = new ArrayList<>(List.of(
                Tag.of("service.name", serviceName),
                Tag.of("environment", environment),
                Tag.of("endpoint", request.getPath()),
                Tag.of("method", request.getMethod().name()),
                Tag.of("status_code_group", statusCodeGroup),
                Tag.of("status_code", String.valueOf(response.getStatusCode()))));

        if (span != null && span.getSpanContext().isValid()) {
            SpanContext context = span.getSpanContext();
            tags.add(Tag.of("traceId", context.getTraceId()));
            tags.add(Tag.of("spanId", context.getSpanId()));
        }

        meterRegistry.counter("api_request_total", tags).increment();
        meterRegistry.timer("api_request_duration_seconds", tags)
                .record(Duration.ofMillis(response.getExecutionTime().toMillis()));

        if (response.getStatusCode() >= 400) {
            meterRegistry.counter("api_request_error_total", tags).increment();
        }
    }

    public void recordError(Throwable error, ApiRequest request, Span span) {
        List<Tag> tags = new ArrayList<>(List.of(
                Tag.of("service.name", serviceName),
                Tag.of("environment", environment),
                Tag.of("error_type", error.getClass().getSimpleName()),
                Tag.of("endpoint", request.getPath()),
                Tag.of("method", request.getMethod().name())));

        if (span != null && span.getSpanContext().isValid()) {
            SpanContext context = span.getSpanContext();
            tags.add(Tag.of("traceId", context.getTraceId()));
            tags.add(Tag.of("spanId", context.getSpanId()));
        }

        meterRegistry.counter("api_request_network_error_total", tags).increment();
    }

    public void recordCancellation(ApiRequest request, Span span) {
        List<Tag> tags = new ArrayList<>(List.of(
                Tag.of("service.name", serviceName),
                Tag.of("environment", environment),
                Tag.of("endpoint", request.getPath()),
                Tag.of("method", request.getMethod().name())));

        if (span != null && span.getSpanContext().isValid()) {
            SpanContext context = span.getSpanContext();
            tags.add(Tag.of("traceId", context.getTraceId()));
            tags.add(Tag.of("spanId", context.getSpanId()));
        }

        meterRegistry.counter("api_request_cancelled_total", tags).increment();
    }

    public void recordAssertionSuccess(String testName, String testSuite) {
        meterRegistry.counter("assertion_success_total",
                "service.name", serviceName,
                "environment", environment,
                "test_name", testName,
                "test_suite", testSuite).increment();
    }

    public void recordAssertionFailure(String testName, String testSuite) {
        meterRegistry.counter("assertion_failure_total",
                "service.name", serviceName,
                "environment", environment,
                "test_name", testName,
                "test_suite", testSuite).increment();
    }

    public void recordTestStart(String testName, String testSuite, String testType) {
        meterRegistry.counter("test_runs_total", "service.name", serviceName, "environment", environment, "test_type",
                testType).increment();
        meterRegistry.counter("test_cases_total", "service.name", serviceName, "environment", environment, "test_name",
                testName, "test_suite", testSuite, "test_type", testType).increment();
    }

    public void recordTestSuccess(String testName, String testSuite, String testType, long durationNanos) {
        meterRegistry.counter("test_cases_passed_total", "service.name", serviceName, "environment", environment,
                "test_name", testName, "test_suite", testSuite, "test_type", testType).increment();
        meterRegistry
                .timer("test_case_duration_seconds", "service.name", serviceName, "environment", environment,
                        "test_name", testName, "test_suite", testSuite, "status", "passed", "test_type", testType)
                .record(durationNanos, TimeUnit.NANOSECONDS);
    }

    public void recordTestFailure(String testName, String testSuite, String testType, long durationNanos) {
        meterRegistry.counter("test_cases_failed_total", "service.name", serviceName, "environment", environment,
                "test_name", testName, "test_suite", testSuite, "test_type", testType).increment();
        meterRegistry
                .timer("test_case_duration_seconds", "service.name", serviceName, "environment", environment,
                        "test_name", testName, "test_suite", testSuite, "status", "failed", "test_type", testType)
                .record(durationNanos, TimeUnit.NANOSECONDS);
    }

    public void recordTestAborted(String testName, String testSuite, String testType, long durationNanos) {
        meterRegistry.counter("test_cases_aborted_total", "service.name", serviceName, "environment", environment,
                "test_name", testName, "test_suite", testSuite, "test_type", testType).increment();
        meterRegistry
                .timer("test_case_duration_seconds", "service.name", serviceName, "environment", environment,
                        "test_name", testName, "test_suite", testSuite, "status", "aborted", "test_type", testType)
                .record(durationNanos, TimeUnit.NANOSECONDS);
    }

    public void recordTestSkipped(String testName, String testSuite, String testType, long durationNanos) {
        meterRegistry.counter("test_cases_skipped_total", "service.name", serviceName, "environment", environment,
                "test_name", testName, "test_suite", testSuite, "test_type", testType).increment();
        meterRegistry
                .timer("test_case_duration_seconds", "service.name", serviceName, "environment", environment,
                        "test_name", testName, "test_suite", testSuite, "status", "skipped", "test_type", testType)
                .record(durationNanos, TimeUnit.NANOSECONDS);
    }

    private String getStatusCodeGroup(int statusCode) {
        if (statusCode < 100 || statusCode > 999) {
            return "unknown";
        }
        return statusCode / 100 + "xx";
    }
}