package solutions.bjjeire.cucumber.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.actions.EventApiActions;
import solutions.bjjeire.api.actions.GymApiActions;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.cucumber.context.TestState;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);

    @Autowired private TestState testState;
    @Autowired private EventApiActions eventApi;
    @Autowired private GymApiActions gymApi;
    @Autowired private ObjectMapper objectMapper;

    @Before
    public void setup(Scenario scenario) {
        logger.info("--- Starting Scenario: '{}' ---", scenario.getName());
    }

    @After
    public void tearDown(Scenario scenario) {
        List<Object> entitiesToClean = testState.getCreatedEntities();
        if (!entitiesToClean.isEmpty()) {
            logger.info("--- Executing {} cleanup action(s) for scenario: '{}' ---", entitiesToClean.size(), scenario.getName());
            Collections.reverse(entitiesToClean);
            entitiesToClean.forEach(this::cleanupEntity);
        }

        if (scenario.isFailed()) {
            logger.error("--- Scenario FAILED: '{}' ---", scenario.getName());
            logFailureDetails(scenario);
        } else {
            logger.info("--- Scenario PASSED: '{}' ---", scenario.getName());
        }
    }

    private void cleanupEntity(Object entity) {
        try {
            if (entity instanceof BjjEvent) {
                BjjEvent event = (BjjEvent) entity;
                eventApi.deleteEvent(testState.getAuthToken(), event.id());
            } else if (entity instanceof Gym) {
                Gym gym = (Gym) entity;
                gymApi.deleteGym(testState.getAuthToken(), gym.id());
            }
        } catch (Exception e) {
            logger.error("Error during cleanup for entity [{}]: {}", entity, e.getMessage(), e);
        }
    }

    private void logFailureDetails(Scenario scenario) {
        Map<String, Object> failureContext = new LinkedHashMap<>();
        failureContext.put("scenarioName", scenario.getName());

        if (testState.getLastResponse() != null) {
            ApiResponse response = testState.getLastResponse();
            failureContext.put("lastApiRequestUrl", response.getRequestPath());
            failureContext.put("lastApiResponseCode", response.getStatusCode());
            failureContext.put("lastApiResponseBody", response.getBodyAsString());
        }

        if (testState.getRequestPayload() != null) {
            try {
                failureContext.put("lastApiRequestPayload", objectMapper.writeValueAsString(testState.getRequestPayload()));
            } catch (Exception e) {
                failureContext.put("lastApiRequestPayload", "Could not serialize request payload.");
            }
        }

        try {
            String failureJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(failureContext);
            logger.error("Failure Context:\n{}", failureJson);
            scenario.attach(failureJson, "application/json", "Failure Context");
        } catch (Exception e) {
            logger.error("Could not serialize failure context to JSON.", e);
        }
    }
}