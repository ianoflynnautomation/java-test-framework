package solutions.bjjeire.api.utils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import net.logstash.logback.argument.StructuredArguments;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.telemetry.MetricsCollector;

@Component
public class TestLifecycleLogger implements BeforeEachCallback, AfterEachCallback, TestWatcher {

    private static final Logger logger = LoggerFactory.getLogger(TestLifecycleLogger.class);
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create("solutions",
            "bjjeire", "observability");

    public static class SpringContext {
        private static ApplicationContext applicationContext;

        public static void setApplicationContext(ApplicationContext context) {
            applicationContext = context;
        }

        public static <T> T getBean(Class<T> beanClass) {
            if (applicationContext == null) {
                throw new IllegalStateException(
                        "Spring ApplicationContext not set in TestLifecycleLogger.SpringContext.");
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

        context.getStore(NAMESPACE).put("testId", testId);
        context.getStore(NAMESPACE).put("testName", testName);
        context.getStore(NAMESPACE).put("testSuite", testSuite);
        context.getStore(NAMESPACE).put("testType", testType);

        MDC.put("test_id", testId);
        MDC.put("test_name", testName);
        MDC.put("test_suite", testSuite);
        MDC.put("test_type", testType);
        MDC.put("test_stage", "setup");

        Baggage.current()
                .toBuilder()
                .put("test.id", testId)
                .put("test.name", testName)
                .put("test.suite", testSuite)
                .put("test.type", testType)
                .build()
                .storeInContext(io.opentelemetry.context.Context.current());

        Span span = tracer.spanBuilder("test." + testName)
                .setAttribute("test.id", testId)
                .setAttribute("test.name", testName)
                .setAttribute("test.class", testClass)
                .setAttribute("test.method", testMethod)
                .setAttribute("test.type", testType)
                .startSpan();

        context.getStore(NAMESPACE).put("testSpan", span);

        logger.debug("JUnit test started",
                StructuredArguments.kv("eventType", "test_start"));

        metricsCollector.recordTestStart(testName, testSuite, testType);
        metricsCollector.incrementRunningTests();
        logger.info("--- Starting JUnit Test: '{}' ---", testName);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        metricsCollector.decrementRunningTests();
        MDC.clear();
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        ApiSettings apiSettings = SpringContext.getBean(ApiSettings.class);

        String testName = context.getStore(NAMESPACE).get("testName", String.class);
        String testSuite = context.getStore(NAMESPACE).get("testSuite", String.class);
        String testType = context.getStore(NAMESPACE).get("testType", String.class);
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null
                ? System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class)
                : 0;

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
            span.end();
        }
        logger.info("--- JUnit Test PASSED: '{}' ---", testName);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        ApiSettings apiSettings = SpringContext.getBean(ApiSettings.class);

        String testName = context.getStore(NAMESPACE).get("testName", String.class);
        String testSuite = context.getStore(NAMESPACE).get("testSuite", String.class);
        String testType = context.getStore(NAMESPACE).get("testType", String.class);
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null
                ? System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class)
                : 0;

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
            span.end();
        }
        logger.error("--- JUnit Test FAILED: '{}' ---", testName, cause);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        ApiSettings apiSettings = SpringContext.getBean(ApiSettings.class);

        String testName = context.getStore(NAMESPACE).get("testName", String.class);
        String testSuite = context.getStore(NAMESPACE).get("testSuite", String.class);
        String testType = context.getStore(NAMESPACE).get("testType", String.class);
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null
                ? System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class)
                : 0;

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
            span.end();
        }
        logger.warn("--- JUnit Test ABORTED: '{}' ---", testName, cause);
    }

    // @Override
    public void testSkipped(ExtensionContext context) {
        MetricsCollector metricsCollector = SpringContext.getBean(MetricsCollector.class);
        ApiSettings apiSettings = SpringContext.getBean(ApiSettings.class);

        String testName = context.getStore(NAMESPACE).get("testName", String.class);
        String testSuite = context.getStore(NAMESPACE).get("testSuite", String.class);
        String testType = apiSettings.getTestType();
        Long durationNanos = context.getStore(NAMESPACE).get("startTime", Long.class) != null
                ? System.nanoTime() - context.getStore(NAMESPACE).get("startTime", Long.class)
                : 0;

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
            span.end();
        }
        logger.info("--- JUnit Test SKIPPED: '{}' --- Reason: {}", testName, reason);
    }
}
