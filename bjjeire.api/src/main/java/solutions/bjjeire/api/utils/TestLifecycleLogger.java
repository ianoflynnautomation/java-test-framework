package solutions.bjjeire.api.utils;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
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
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.telemetry.MetricsCollector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TestLifecycleLogger implements BeforeEachCallback, AfterEachCallback, TestWatcher {
    private static final Logger logger = LoggerFactory.getLogger(TestLifecycleLogger.class);
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create("solutions", "bjjeire", "observability");

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
    public void beforeEach(ExtensionContext context) {
        String testId = UUID.randomUUID().toString();
        String testName = context.getDisplayName();
        String testClass = context.getRequiredTestClass().getSimpleName();
        String testMethod = context.getRequiredTestMethod().getName();
        String testSuite = testClass;

        Tracer tracer = SpringContext.getBean(Tracer.class);
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        ApiSettings apiSettings = SpringContext.getBean(ApiSettings.class);

        String testType = apiSettings.getTestType();

        // Store test details in JUnit context for reliable retrieval in TestWatcher methods
        context.getStore(NAMESPACE).put("testId", testId);
        context.getStore(NAMESPACE).put("testName", testName);
        context.getStore(NAMESPACE).put("testSuite", testSuite);
        context.getStore(NAMESPACE).put("testType", testType);


        // Put test context into MDC for structured logging.
        // These will be automatically picked up by Logback's OpenTelemetryAppender.
        MDC.put("test_id", testId);
        MDC.put("test_name", testName);
        MDC.put("test_suite", testSuite);
        MDC.put("test_type", testType);
        MDC.put("test_stage", "setup");

        // Propagate test context via Baggage.
        // We are no longer calling .makeCurrent() here to avoid scope management issues.
        // Baggage is still created and can be propagated by auto-instrumentation if configured.
        Baggage.current()
                .toBuilder()
                .put("test.id", testId)
                .put("test.name", testName)
                .put("test.suite", testSuite)
                .put("test.type", testType)
                .build()
                .storeInContext(io.opentelemetry.context.Context.current()); // Store in context, but don't make current.

        // Create a new span for the test case.
        // This span will automatically become the current span for the current thread and its children.
        Span span = tracer.spanBuilder("test." + testName)
                .setAttribute("test.id", testId)
                .setAttribute("test.name", testName)
                .setAttribute("test.class", testClass)
                .setAttribute("test.method", testMethod)
                .setAttribute("test.type", testType)
                .startSpan();

        // Store span in JUnit context for later access.
        context.getStore(NAMESPACE).put("testSpan", span);

        logger.debug("JUnit test started",
                StructuredArguments.kv("eventType", "test_start"));

        metricsCollector.recordTestStart(testName, testSuite, testType);
        metricsCollector.incrementRunningTests();
        logger.info("--- Starting JUnit Test: '{}' ---", testName);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // This method is called AFTER testSuccessful/testFailed/testAborted/testSkipped.
        // The span should have already been ended by those methods.
        // We only need to decrement the running tests counter and clear MDC here.
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        metricsCollector.decrementRunningTests();
        MDC.clear(); // Clear MDC after each test to prevent pollution
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        ApiSettings apiSettings = SpringContext.getBean(ApiSettings.class);
        // Retrieve test details from JUnit context store
        String testName = context.getStore(NAMESPACE).get("testName", String.class);
        String testSuite = context.getStore(NAMESPACE).get("testSuite", String.class);
        String testType = context.getStore(NAMESPACE).get("testType", String.class);
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null ?
                System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class) : 0;

        MDC.put("test_stage", "teardown");
        MDC.put("status", "passed");
        MDC.put("duration_ms", String.valueOf(TimeUnit.NANOSECONDS.toMillis(durationNanos)));

        logger.info("JUnit test passed",
                StructuredArguments.kv("eventType", "test_end"));

        metricsCollector.recordTestSuccess(testName, testSuite, testType, durationNanos);
        Span span = context.getStore(NAMESPACE).get("testSpan", Span.class);

        if (span != null) {
            span.setAttribute("test.status", "passed");
            span.setStatus(StatusCode.OK);
            span.end(); // End the span after setting attributes and status
        }
        logger.info("--- JUnit Test PASSED: '{}' ---", testName);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        ApiSettings apiSettings = SpringContext.getBean(ApiSettings.class);
        // Retrieve test details from JUnit context store
        String testName = context.getStore(NAMESPACE).get("testName", String.class);
        String testSuite = context.getStore(NAMESPACE).get("testSuite", String.class);
        String testType = context.getStore(NAMESPACE).get("testType", String.class);
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null ?
                System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class) : 0;

        MDC.put("test_stage", "teardown");
        MDC.put("status", "failed");
        MDC.put("duration_ms", String.valueOf(TimeUnit.NANOSECONDS.toMillis(durationNanos)));
        MDC.put("error_message", cause.getMessage() != null ? cause.getMessage() : "unknown");
        MDC.put("error_type", cause.getClass().getSimpleName());


        logger.error("JUnit test failed",
                StructuredArguments.kv("eventType", "test_end"),
                cause);

        metricsCollector.recordTestFailure(testName, testSuite, testType, durationNanos);
        Span span = context.getStore(NAMESPACE).get("testSpan", Span.class);

        if (span != null) {
            span.setAttribute("test.status", "failed");
            span.recordException(cause);
            span.setStatus(StatusCode.ERROR, cause.getMessage() != null ? cause.getMessage() : "unknown");
            span.end(); // End the span after setting attributes and status
        }
        logger.error("--- JUnit Test FAILED: '{}' ---", testName, cause);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        ApiSettings apiSettings = SpringContext.getBean(ApiSettings.class);
        // Retrieve test details from JUnit context store
        String testName = context.getStore(NAMESPACE).get("testName", String.class);
        String testSuite = context.getStore(NAMESPACE).get("testSuite", String.class);
        String testType = context.getStore(NAMESPACE).get("testType", String.class);
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null ?
                System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class) : 0;

        MDC.put("test_stage", "teardown");
        MDC.put("status", "aborted");
        MDC.put("duration_ms", String.valueOf(TimeUnit.NANOSECONDS.toMillis(durationNanos)));
        MDC.put("reason", cause != null ? cause.getMessage() : "unknown");

        logger.warn("JUnit test aborted",
                StructuredArguments.kv("eventType", "test_end"));

        metricsCollector.recordTestAborted(testName, testSuite, testType, durationNanos);
        Span span = context.getStore(NAMESPACE).get("testSpan", Span.class);

        if (span != null) {
            span.setAttribute("test.status", "aborted");
            if (cause != null) {
                span.recordException(cause);
            }
            span.setStatus(StatusCode.UNSET, "Test aborted");
            span.end(); // End the span
        }
        logger.warn("--- JUnit Test ABORTED: '{}' ---", testName, cause);
    }

    //@Override
    public void testSkipped(ExtensionContext context) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        ApiSettings apiSettings = SpringContext.getBean(ApiSettings.class);
        // Retrieve test details from JUnit context store
        String testName = context.getStore(NAMESPACE).get("testName", String.class);
        String testSuite = context.getStore(NAMESPACE).get("testSuite", String.class);
        String testType = apiSettings.getTestType();
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null ?
                System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class) : 0;

        String reason = "skipped via JUnit";

        MDC.put("test_stage", "teardown");
        MDC.put("status", "skipped");
        MDC.put("duration_ms", String.valueOf(TimeUnit.NANOSECONDS.toMillis(durationNanos)));
        MDC.put("reason", reason);


        logger.info("JUnit test skipped",
                StructuredArguments.kv("eventType", "test_end"));

        metricsCollector.recordTestSkipped(testName, testSuite, testType, durationNanos);
        Span span = context.getStore(NAMESPACE).get("testSpan", Span.class);

        if (span != null) {
            span.setAttribute("test.status", "skipped");
            span.setStatus(StatusCode.UNSET, "Test skipped: " + reason);
            span.end(); // End the span
        }
        logger.info("--- JUnit Test SKIPPED: '{}' --- Reason: {}", testName, reason);
    }
}
