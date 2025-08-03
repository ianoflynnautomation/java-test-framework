package solutions.bjjeire.api.utils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import net.logstash.logback.argument.StructuredArguments;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.telemetry.MetricsCollector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TestLifecycleLogger implements BeforeEachCallback, AfterEachCallback, TestWatcher {

    private static final Logger logger = LoggerFactory.getLogger(TestLifecycleLogger.class);
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create("solutions", "bjjeire", "observability");

    // Static helper to hold Spring ApplicationContext for accessing beans
    public static class SpringContext {
        private static ApplicationContext applicationContext;

        public static void setApplicationContext(ApplicationContext context) {
            applicationContext = context;
        }

        public static <T> T getBean(Class<T> beanClass) {
            if (applicationContext == null) {
                throw new IllegalStateException("Spring ApplicationContext not set in TestLifecycleLogger.SpringContext.");
            }
            return applicationContext.getBean(beanClass);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        String testId = UUID.randomUUID().toString();
        String testName = context.getDisplayName();
        String testClass = context.getRequiredTestClass().getSimpleName();
        String testMethod = context.getRequiredTestMethod().getName();

        // Retrieve beans
        Tracer tracer = SpringContext.getBean(Tracer.class);
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);

        // Start OpenTelemetry span
        Span span = tracer.spanBuilder("test." + testName)
                .setAttribute("test.id", testId)
                .setAttribute("test.name", testName)
                .setAttribute("test.class", testClass)
                .setAttribute("test.method", testMethod)
                .setAttribute("test.type", "junit_test")
                .startSpan();

        // Store span and scope
        context.getStore(NAMESPACE).put("testSpan", span);
        Scope scope = span.makeCurrent();
        context.getStore(NAMESPACE).put("testScope", scope);
        context.getStore(NAMESPACE).put("startTime", System.nanoTime());

        // Set MDC properties
        MDC.put("traceId", span.getSpanContext().getTraceId());
        MDC.put("spanId", span.getSpanContext().getSpanId());
        MDC.put("test_id", testId);
        MDC.put("test_name", testName);
        MDC.put("test_stage", "setup");

        // Log test start
        logger.debug("JUnit test started",
                StructuredArguments.kv("eventType", "test_start"),
                StructuredArguments.kv("test_id", testId),
                StructuredArguments.kv("test_name", testName),
                StructuredArguments.kv("test_class", testClass),
                StructuredArguments.kv("test_method", testMethod),
                StructuredArguments.kv("test_stage", "setup"));

        // Record metrics
        metricsCollector.meterRegistry.counter("test_runs_total", "test_type", "junit").increment();
        metricsCollector.meterRegistry.counter("test_cases_total", "test_name", testName, "test_type", "junit").increment();
        metricsCollector.incrementRunningTests();

        logger.info("--- Starting JUnit Test: '{}' ---", testName);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        Span span = context.getStore(NAMESPACE).get("testSpan", Span.class);
        Scope scope = context.getStore(NAMESPACE).get("testScope", Scope.class);

        if (scope != null) {
            scope.close();
        }
        if (span != null) {
            span.end();
        }

        metricsCollector.decrementRunningTests();
        MDC.clear();
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        String testId = MDC.get("test_id");
        String testName = MDC.get("test_name");
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null ?
                System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class) : 0;
        long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);

        // Log test success
        logger.info("JUnit test passed",
                StructuredArguments.kv("eventType", "test_end"),
                StructuredArguments.kv("test_id", testId),
                StructuredArguments.kv("test_name", testName),
                StructuredArguments.kv("test_stage", "teardown"),
                StructuredArguments.kv("status", "passed"),
                StructuredArguments.kv("duration_ms", durationMs));

        // Record metrics
        metricsCollector.meterRegistry.counter("test_cases_passed_total", "test_name", testName, "test_type", "junit").increment();
        metricsCollector.meterRegistry.timer("test_case_duration_seconds", "test_name", testName, "status", "passed", "test_type", "junit")
                .record(durationNanos, TimeUnit.NANOSECONDS);

        // Update OpenTelemetry span
        Span span = context.getStore(NAMESPACE).get("testSpan", Span.class);
        if (span != null) {
            span.setAttribute("test.status", "passed");
            span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
        }
        logger.info("--- JUnit Test PASSED: '{}' ---", testName);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        String testId = MDC.get("test_id");
        String testName = MDC.get("test_name");
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null ?
                System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class) : 0;
        long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);

        // Log test failure
        logger.error("JUnit test failed",
                StructuredArguments.kv("eventType", "test_end"),
                StructuredArguments.kv("test_id", testId),
                StructuredArguments.kv("test_name", testName),
                StructuredArguments.kv("test_stage", "teardown"),
                StructuredArguments.kv("status", "failed"),
                StructuredArguments.kv("duration_ms", durationMs),
                StructuredArguments.kv("error_details", Map.of(
                        "error_message", cause.getMessage() != null ? cause.getMessage() : "unknown",
                        "error_type", cause.getClass().getSimpleName())),
                cause);

        // Record metrics
        metricsCollector.meterRegistry.counter("test_cases_failed_total", "test_name", testName, "test_type", "junit").increment();
        metricsCollector.meterRegistry.timer("test_case_duration_seconds", "test_name", testName, "status", "failed", "test_type", "junit")
                .record(durationNanos, TimeUnit.NANOSECONDS);

        // Update OpenTelemetry span
        Span span = context.getStore(NAMESPACE).get("testSpan", Span.class);
        if (span != null) {
            span.setAttribute("test.status", "failed");
            span.recordException(cause);
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, cause.getMessage() != null ? cause.getMessage() : "unknown");
        }
        logger.error("--- JUnit Test FAILED: '{}' ---", testName, cause);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        String testId = MDC.get("test_id");
        String testName = MDC.get("test_name");
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null ?
                System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class) : 0;
        long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);

        // Log test aborted
        logger.warn("JUnit test aborted",
                StructuredArguments.kv("eventType", "test_end"),
                StructuredArguments.kv("test_id", testId),
                StructuredArguments.kv("test_name", testName),
                StructuredArguments.kv("test_stage", "teardown"),
                StructuredArguments.kv("status", "aborted"),
                StructuredArguments.kv("duration_ms", durationMs),
                StructuredArguments.kv("reason", cause != null ? cause.getMessage() : "unknown"));

        // Record metrics
        metricsCollector.meterRegistry.counter("test_cases_skipped_total", "test_name", testName, "test_type", "junit").increment();
        metricsCollector.meterRegistry.timer("test_case_duration_seconds", "test_name", testName, "status", "aborted", "test_type", "junit")
                .record(durationNanos, TimeUnit.NANOSECONDS);

        // Update OpenTelemetry span
        Span span = context.getStore(NAMESPACE).get("testSpan", Span.class);
        if (span != null) {
            span.setAttribute("test.status", "aborted");
            if (cause != null) {
                span.recordException(cause);
            }
            span.setStatus(io.opentelemetry.api.trace.StatusCode.UNSET, "Test aborted");
        }
        logger.warn("--- JUnit Test ABORTED: '{}' ---", testName, cause);
    }

    public void testSkipped(ExtensionContext context, String reason) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        String testId = MDC.get("test_id");
        String testName = MDC.get("test_name");
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null ?
                System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class) : 0;
        long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);

        // Log test skipped
        logger.info("JUnit test skipped",
                StructuredArguments.kv("eventType", "test_end"),
                StructuredArguments.kv("test_id", testId),
                StructuredArguments.kv("test_name", testName),
                StructuredArguments.kv("test_stage", "teardown"),
                StructuredArguments.kv("status", "skipped"),
                StructuredArguments.kv("duration_ms", durationMs),
                StructuredArguments.kv("reason", reason != null ? reason : "unknown"));

        // Record metrics
        metricsCollector.meterRegistry.counter("test_cases_skipped_total", "test_name", testName, "test_type", "junit").increment();
        metricsCollector.meterRegistry.timer("test_case_duration_seconds", "test_name", testName, "status", "skipped", "test_type", "junit")
                .record(durationNanos, TimeUnit.NANOSECONDS);

        // Update OpenTelemetry span
        Span span = context.getStore(NAMESPACE).get("testSpan", Span.class);
        if (span != null) {
            span.setAttribute("test.status", "skipped");
            span.setStatus(io.opentelemetry.api.trace.StatusCode.UNSET, "Test skipped: " + reason);
        }
        logger.info("--- JUnit Test SKIPPED: '{}' --- Reason: {}", testName, reason != null ? reason : "unknown");
    }
}