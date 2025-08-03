package solutions.bjjeire.cucumber.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
    private static final ThreadLocal<Scope> scenarioScope = new ThreadLocal<>();
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();
    private static final ThreadLocal<String> currentScenarioId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentScenarioName = new ThreadLocal<>();

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

        // Store scenario ID and name
        currentScenarioId.set(scenarioId);
        currentScenarioName.set(scenarioName);

        // Start OpenTelemetry span
        Span span = tracer.spanBuilder("test." + scenarioName)
                .setAttribute("test.id", scenarioId)
                .setAttribute("test.name", scenarioName)
                .setAttribute("test.type", "cucumber_scenario")
                .startSpan();

        // Store span and scope
        scenarioSpan.set(span);
        Scope scope = span.makeCurrent();
        scenarioScope.set(scope);

        // Set MDC properties
        MDC.put("traceId", span.getSpanContext().getTraceId());
        MDC.put("spanId", span.getSpanContext().getSpanId());
        MDC.put("test_id", scenarioId);
        MDC.put("test_name", scenarioName);
        MDC.put("test_stage", "setup");

        // Record start time
        startTime.set(System.nanoTime());

        // Log scenario start
        logger.debug("Scenario started",
                StructuredArguments.kv("eventType", "test_start"),
                StructuredArguments.kv("test_id", scenarioId),
                StructuredArguments.kv("test_name", scenarioName),
                StructuredArguments.kv("test_stage", "setup"));

        // Record metrics
        metricsCollector.meterRegistry.counter("test_runs_total", "test_type", "cucumber").increment();
        metricsCollector.meterRegistry.counter("test_cases_total", "test_name", scenarioName, "test_type", "cucumber").increment();
        metricsCollector.incrementRunningTests();
    }

    @After
    public void tearDown(Scenario scenario) {
        String scenarioName = Objects.requireNonNullElse(currentScenarioName.get(), "unknown-name");
        String scenarioId = Objects.requireNonNullElse(currentScenarioId.get(), "unknown-id");

        // Perform entity cleanup
        List<Object> entitiesToClean = testContext.getCreatedEntities();
        if (!entitiesToClean.isEmpty()) {
            MDC.put("test_stage", "teardown");
            logger.debug("Executing cleanup actions",
                    StructuredArguments.kv("eventType", "resource_cleanup"),
                    StructuredArguments.kv("entity_count", entitiesToClean.size()),
                    StructuredArguments.kv("test_id", scenarioId),
                    StructuredArguments.kv("test_name", scenarioName));
            Collections.reverse(entitiesToClean);
            entitiesToClean.forEach(this::cleanupEntity);
        }

        // Calculate duration
        Long start = startTime.get();
        long durationNanos = start != null ? System.nanoTime() - start : 0;
        long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);

        // Handle scenario outcome
        Span span = scenarioSpan.get();
        MDC.put("test_stage", "teardown");

        if (scenario.isFailed()) {
            logger.error("Scenario failed",
                    StructuredArguments.kv("eventType", "test_end"),
                    StructuredArguments.kv("test_id", scenarioId),
                    StructuredArguments.kv("test_name", scenarioName),
                    StructuredArguments.kv("test_stage", "teardown"),
                    StructuredArguments.kv("status", "failed"),
                    StructuredArguments.kv("duration_ms", durationMs),
                    new RuntimeException("Scenario failed: " + scenario.getStatus().name()));
            metricsCollector.meterRegistry.counter("test_cases_failed_total", "test_name", scenarioName, "test_type", "cucumber").increment();
            if (span != null) {
                span.setAttribute("test.status", "failed");
                span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, "Scenario failed");
            }
            logFailureDetails(scenario, span);
            metricsCollector.meterRegistry.timer("test_case_duration_seconds", "test_name", scenarioName, "status", "failed", "test_type", "cucumber")
                    .record(durationNanos, TimeUnit.NANOSECONDS);
        } else {
            logger.info("Scenario passed",
                    StructuredArguments.kv("eventType", "test_end"),
                    StructuredArguments.kv("test_id", scenarioId),
                    StructuredArguments.kv("test_name", scenarioName),
                    StructuredArguments.kv("test_stage", "teardown"),
                    StructuredArguments.kv("status", "passed"),
                    StructuredArguments.kv("duration_ms", durationMs));
            metricsCollector.meterRegistry.counter("test_cases_passed_total", "test_name", scenarioName, "test_type", "cucumber").increment();
            if (span != null) {
                span.setAttribute("test.status", "passed");
                span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
            }
            metricsCollector.meterRegistry.timer("test_case_duration_seconds", "test_name", scenarioName, "status", "passed", "test_type", "cucumber")
                    .record(durationNanos, TimeUnit.NANOSECONDS);
        }

        // Clean up OpenTelemetry and ThreadLocal
        Scope scope = scenarioScope.get();
        if (scope != null) {
            scope.close();
            scenarioScope.remove();
        }
        if (span != null) {
            span.end();
            scenarioSpan.remove();
        }
        metricsCollector.decrementRunningTests();
        startTime.remove();
        currentScenarioId.remove();
        currentScenarioName.remove();
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

        Map<String, Object> failureContext = new LinkedHashMap<>();
        failureContext.put("scenarioName", scenarioName);
        failureContext.put("scenarioId", scenarioId);

        if (testContext.getLastResponse() != null) {
            ApiResponse response = testContext.getLastResponse();
            failureContext.put("lastApiRequestUrl", Objects.requireNonNullElse(response.getRequestPath(), "unknown-path"));
            failureContext.put("lastApiResponseCode", response.getStatusCode());
            failureContext.put("lastApiResponseBody", Objects.requireNonNullElse(response.getBodyAsString(), ""));
        }

        if (testContext.getRequestPayload() != null) {
            try {
                failureContext.put("lastApiRequestPayload", objectMapper.writeValueAsString(testContext.getRequestPayload()));
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
                span.addEvent("scenario_failed", io.opentelemetry.api.common.Attributes.of(stringKey("failure_context"), failureJson));
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