package solutions.bjjeire.cucumber.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC; 
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.services.EventService;
import solutions.bjjeire.api.services.GymService;
import solutions.bjjeire.api.telemetry.MetricsCollector;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.cucumber.context.TestContext;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

public class Hooks {
    
    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private static final ThreadLocal<Span> scenarioSpan = new ThreadLocal<>();
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();
    private static final ThreadLocal<String> currentScenarioId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentScenarioName = new ThreadLocal<>();
    private static final ThreadLocal<String> currentTestSuite = new ThreadLocal<>(); // Added ThreadLocal for testSuite

    @Autowired
    private TestContext testContext;
    @Autowired
    private EventService eventService;
    @Autowired
    private GymService gymService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MetricsCollector metricsCollector;
    @Autowired
    private Tracer tracer;

    @Before
    public void setup(Scenario scenario) {
        String scenarioName = scenario.getName();
        String scenarioId = UUID.randomUUID().toString();
        // Extract test suite from tags, defaulting if not found
        String testSuite = scenario.getSourceTagNames().stream()
                .filter(tag -> tag.startsWith("@"))
                .map(tag -> tag.replaceFirst("@", "")) // Remove '@' prefix
                .findFirst()
                .orElse("unknown_suite");

        currentScenarioId.set(scenarioId);
        currentScenarioName.set(scenarioName);
        currentTestSuite.set(testSuite); // Store testSuite in ThreadLocal

        // Put test context into MDC for structured logging.
        // These will be automatically picked up by Logback's OpenTelemetryAppender.
        MDC.put("test_id", scenarioId);
        MDC.put("test_name", scenarioName);
        MDC.put("test_suite", testSuite);
        MDC.put("test_type", "cucumber_scenario"); // Hardcoded for now, can be configured
        MDC.put("test_stage", "setup");

        // Propagate test context via Baggage.
        // We are no longer calling .makeCurrent() here to avoid scope management
        // issues.
        // Baggage is still created and can be propagated by auto-instrumentation if
        // configured.
        Baggage.current()
                .toBuilder()
                .put("test.id", scenarioId)
                .put("test.name", scenarioName)
                .put("test.suite", testSuite)
                .put("test.type", "cucumber_scenario")
                .build()
                .storeInContext(io.opentelemetry.context.Context.current()); // Store in context, but don't make
                                                                             // current.

        // Create a new span for the test case.
        // This span will automatically become the current span for the current thread
        // and its children.
        Span span = tracer.spanBuilder("test." + scenarioName)
                .setAttribute("test.id", scenarioId)
                .setAttribute("test.name", scenarioName)
                .setAttribute("test.suite", testSuite)
                .setAttribute("test.type", "cucumber_scenario")
                .startSpan();

        scenarioSpan.set(span); // Store span in ThreadLocal
        // Scope scope = span.makeCurrent(); // Removed explicit scope management
        // scenarioScope.set(scope); // Removed explicit scope storage
        startTime.set(System.nanoTime());

        logger.debug("Scenario started",
                StructuredArguments.kv("eventType", "test_start")); // MDC will add other fields

        metricsCollector.recordTestStart(scenarioName, testSuite, "cucumber");
        metricsCollector.incrementRunningTests();
    }

    @After
    public void tearDown(Scenario scenario) {
        String scenarioName = Objects.requireNonNullElse(currentScenarioName.get(), "unknown-name");
        String scenarioId = Objects.requireNonNullElse(currentScenarioId.get(), "unknown-id");
        String testSuite = Objects.requireNonNullElse(currentTestSuite.get(), "unknown_suite"); // Retrieve from
                                                                                                // ThreadLocal

        List<Object> entitiesToClean = testContext.getCreatedEntities();
        if (!entitiesToClean.isEmpty()) {
            logger.debug("Executing cleanup actions",
                    StructuredArguments.kv("eventType", "resource_cleanup"),
                    StructuredArguments.kv("entity_count", entitiesToClean.size()),
                    StructuredArguments.kv("test_id", scenarioId),
                    StructuredArguments.kv("test_name", scenarioName));
            Collections.reverse(entitiesToClean);
            entitiesToClean.forEach(this::cleanupEntity);
        }

        Long start = startTime.get();
        long durationNanos = start != null ? System.nanoTime() - start : 0;

        Span span = scenarioSpan.get();

        MDC.put("test_stage", "teardown");
        MDC.put("duration_ms", String.valueOf(TimeUnit.NANOSECONDS.toMillis(durationNanos)));
        MDC.put("test_id", scenarioId); // Ensure MDC is set for final log
        MDC.put("test_name", scenarioName); // Ensure MDC is set for final log
        MDC.put("test_suite", testSuite); // Ensure MDC is set for final log

        if (scenario.isFailed()) {
            MDC.put("status", "failed");
            logger.error("Scenario failed",
                    StructuredArguments.kv("eventType", "test_end"),
                    new RuntimeException("Scenario failed: " + scenario.getStatus().name())); // Pass cause directly

            metricsCollector.recordTestFailure(scenarioName, testSuite, "cucumber", durationNanos);
            if (span != null) {
                span.setAttribute("test.status", "failed");
                // Safely get the error, providing a fallback if getError() is not available or
                // returns null
                Throwable scenarioError = null;
                try {
                    // Check if getError() method exists and is accessible
                    java.lang.reflect.Method getErrorMethod = scenario.getClass().getMethod("getError");
                    scenarioError = (Throwable) getErrorMethod.invoke(scenario);
                } catch (NoSuchMethodException | IllegalAccessException
                        | java.lang.reflect.InvocationTargetException e) {
                    // Log a warning if getError() is not found or callable, but continue with
                    // fallback
                    logger.warn(
                            "Scenario.getError() method not found or callable, using generic RuntimeException for span.recordException.",
                            e);
                }

                // Record the exception on the span, using the actual error or a generic one
                span.recordException(scenarioError != null ? scenarioError
                        : new RuntimeException("Scenario failed: " + scenario.getStatus().name()));
                span.setStatus(StatusCode.ERROR, "Scenario failed");
            }
            logFailureDetails(scenario, span);
        }
        // } else if (scenario.isSkipped()) { // Handle skipped scenarios explicitly
        // MDC.put("status", "skipped");
        // logger.info("Scenario skipped",
        // StructuredArguments.kv("eventType", "test_end"),
        // StructuredArguments.kv("reason", "Cucumber scenario skipped"));
        // metricsCollector.recordTestSkipped(scenarioName, testSuite, "cucumber",
        // durationNanos);
        // if (span != null) {
        // span.setAttribute("test.status", "skipped");
        // span.setStatus(StatusCode.UNSET, "Scenario skipped");
        // }
        // }
        else {
            MDC.put("status", "passed");
            logger.info("Scenario passed",
                    StructuredArguments.kv("eventType", "test_end"));
            metricsCollector.recordTestSuccess(scenarioName, testSuite, "cucumber", durationNanos);
            if (span != null) {
                span.setAttribute("test.status", "passed");
                span.setStatus(StatusCode.OK);
            }
        }

        if (span != null) {
            span.end();
            scenarioSpan.remove();
        }
        metricsCollector.decrementRunningTests();
        startTime.remove();
        currentScenarioId.remove();
        currentScenarioName.remove();
        currentTestSuite.remove();
        MDC.clear();
    }

    private void cleanupEntity(Object entity) {
        String scenarioId = Objects.requireNonNullElse(currentScenarioId.get(), "unknown-id");
        String scenarioName = Objects.requireNonNullElse(currentScenarioName.get(), "unknown-name");

        try {
            if (entity instanceof BjjEvent) {
                BjjEvent event = (BjjEvent) entity;
                eventService.deleteEvent(testContext.getAuthToken(), event.id());
                logger.debug("Cleaned up BjjEvent",
                        StructuredArguments.kv("eventType", "resource_cleanup"),
                        StructuredArguments.kv("entity_type", "BjjEvent"),
                        StructuredArguments.kv("entity_id", event.id()),
                        StructuredArguments.kv("test_id", scenarioId),
                        StructuredArguments.kv("test_name", scenarioName));
            } else if (entity instanceof Gym) {
                Gym gym = (Gym) entity;
                gymService.deleteGym(testContext.getAuthToken(), gym.id());
                logger.debug("Cleaned up Gym",
                        StructuredArguments.kv("eventType", "resource_cleanup"),
                        StructuredArguments.kv("entity_type", "Gym"),
                        StructuredArguments.kv("entity_id", gym.id()),
                        StructuredArguments.kv("test_id", scenarioId),
                        StructuredArguments.kv("test_name", scenarioName));
            }
        } catch (Exception e) {
            logger.error("Error during cleanup for entity",
                    StructuredArguments.kv("eventType", "resource_cleanup_failure"),
                    StructuredArguments.kv("entity", entity.toString()),
                    StructuredArguments.kv("test_id", scenarioId),
                    StructuredArguments.kv("test_name", scenarioName),
                    e);
        }
    }

    private void logFailureDetails(Scenario scenario, Span span) {
        String scenarioName = Objects.requireNonNullElse(currentScenarioName.get(), "unknown-name");
        String scenarioId = Objects.requireNonNullElse(currentScenarioId.get(), "unknown-id");
        String testSuite = Objects.requireNonNullElse(currentTestSuite.get(), "unknown_suite"); // Retrieve from
                                                                                                // ThreadLocal

        Map<String, Object> failureContext = new LinkedHashMap<>();
        failureContext.put("scenarioName", scenarioName);
        failureContext.put("scenarioId", scenarioId);
        failureContext.put("testSuite", testSuite);

        if (testContext.getLastResponse() != null) {
            ApiResponse response = testContext.getLastResponse();
            failureContext.put("lastApiRequestUrl",
                    Objects.requireNonNullElse(response.getRequestPath(), "unknown-path"));
            failureContext.put("lastApiResponseCode", response.getStatusCode());
            failureContext.put("lastApiResponseBody", Objects.requireNonNullElse(response.getBodyAsString(), ""));
        }

        if (testContext.getRequestPayload() != null) {
            try {
                failureContext.put("lastApiRequestPayload",
                        objectMapper.writeValueAsString(testContext.getRequestPayload()));
            } catch (Exception e) {
                failureContext.put("lastApiRequestPayload", "Could not serialize request payload.");
            }
        }

        try {
            String failureJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(failureContext);
            logger.error("Scenario failed with context",
                    StructuredArguments.kv("eventType", "scenario_failure_details"),
                    StructuredArguments.kv("error_details", failureContext),
                    StructuredArguments.kv("test_id", scenarioId),
                    StructuredArguments.kv("test_name", scenarioName));
            scenario.attach(failureJson, "application/json", "Failure Context");
            if (span != null) {
                span.addEvent("scenario_failed",
                        io.opentelemetry.api.common.Attributes.of(stringKey("failure_context"), failureJson));
            }
        } catch (Exception e) {
            logger.error("Could not serialize failure context to JSON",
                    StructuredArguments.kv("eventType", "serialization_error"),
                    StructuredArguments.kv("test_id", scenarioId),
                    StructuredArguments.kv("test_name", scenarioName),
                    e);
        }
    }
}
