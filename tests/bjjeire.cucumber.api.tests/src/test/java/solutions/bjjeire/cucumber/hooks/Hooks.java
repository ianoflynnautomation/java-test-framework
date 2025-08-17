package solutions.bjjeire.cucumber.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import solutions.bjjeire.api.endpoints.BjjEventEndpoints;
import solutions.bjjeire.api.endpoints.GymEndpoints;
import solutions.bjjeire.api.services.ApiService;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.cucumber.context.TestContext;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class Hooks {

    private final TestContext testContext;
    private final ApiService apiService;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();
    private static final ThreadLocal<String> currentScenarioId = new ThreadLocal<>();

    @Before
    public void setup(Scenario scenario) {
        String scenarioId = UUID.randomUUID().toString();
        String testSuite = scenario.getSourceTagNames().stream()
                .filter(tag -> tag.startsWith("@"))
                .findFirst()
                .map(tag -> tag.substring(1))
                .orElse("unknown_suite");

        currentScenarioId.set(scenarioId);
        startTime.set(System.nanoTime());

        MDC.put("test_id", scenarioId);
        MDC.put("test_name", scenario.getName());
        MDC.put("test_suite", testSuite);
        MDC.put("test_stage", "setup");

        logger.info("Scenario started", StructuredArguments.kv("eventType", "test_start"));
    }

    @After
    public void tearDown(Scenario scenario) {
        try {
            // Teardown logic
            MDC.put("test_stage", "teardown");
            performCleanup();
            logFinalStatus(scenario);
        } finally {
            MDC.clear();
            currentScenarioId.remove();
            startTime.remove();
        }
    }

    private void performCleanup() {
        List<Object> entitiesToClean = testContext.getCreatedEntities();
        if (entitiesToClean.isEmpty()) {
            return;
        }

        logger.debug("Executing cleanup actions",
                StructuredArguments.kv("eventType", "resource_cleanup_start"),
                StructuredArguments.kv("entity_count", entitiesToClean.size()));

        Collections.reverse(entitiesToClean);
        entitiesToClean.forEach(this::cleanupEntity);
    }

    private void logFinalStatus(Scenario scenario) {
        Long start = startTime.get();
        long durationMs = (start != null) ? TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) : -1;
        MDC.put("duration_ms", String.valueOf(durationMs));

        if (scenario.isFailed()) {
            MDC.put("test_status", "failed");
            logFailureDetails(scenario);
            logger.error("Scenario failed",
                    StructuredArguments.kv("eventType", "test_end"),
                    new RuntimeException("Scenario failed with status: " + scenario.getStatus()));
        } else {
            MDC.put("test_status", "passed");
            logger.info("Scenario passed", StructuredArguments.kv("eventType", "test_end"));
        }
    }

    private void cleanupEntity(Object entity) {
        try {
            if (entity instanceof BjjEvent event) {
                apiService.delete(testContext.getAuthToken(), BjjEventEndpoints.bjjEventById(event.id())).block();
                logger.debug("Cleaned up BjjEvent",
                        StructuredArguments.kv("eventType", "resource_cleanup_success"),
                        StructuredArguments.kv("entity_id", event.id()));
            } else if (entity instanceof Gym gym) {
                apiService.delete(testContext.getAuthToken(), GymEndpoints.gymById(gym.id())).block();
                logger.debug("Cleaned up Gym",
                        StructuredArguments.kv("eventType", "resource_cleanup_success"),
                        StructuredArguments.kv("entity_id", gym.id()));
            }
        } catch (Exception e) {
            logger.error("Error during cleanup for entity",
                    StructuredArguments.kv("eventType", "resource_cleanup_failure"),
                    StructuredArguments.kv("entity_type", entity.getClass().getSimpleName()),
                    e);
        }
    }

    private void logFailureDetails(Scenario scenario) {
        Map<String, Object> failureContext = new LinkedHashMap<>();

        Optional.ofNullable(testContext.getLastResponse()).ifPresent(response -> {
            failureContext.put("lastApiRequestUrl", response.getRequestPath());
            failureContext.put("lastApiResponseCode", response.getStatusCode());
            failureContext.put("lastApiResponseBody", response.getBodyAsString());
        });

        Optional.ofNullable(testContext.getRequestPayload()).ifPresent(payload -> {
            try {
                failureContext.put("lastApiRequestPayload", objectMapper.writeValueAsString(payload));
            } catch (Exception e) {
                failureContext.put("lastApiRequestPayload", "Could not serialize request payload.");
            }
        });

        logger.error("Scenario failure context",
                StructuredArguments.kv("eventType", "failure_details"),
                StructuredArguments.kv("context", failureContext));

        try {
            String failureJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(failureContext);
            scenario.attach(failureJson, "application/json", "Failure Context");
        } catch (Exception e) {
            logger.warn("Could not attach failure context to Cucumber report", e);
        }
    }
}