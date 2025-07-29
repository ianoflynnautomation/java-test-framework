package solutions.bjjeire.cucumber.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.validation.ValidatableResponse;
import solutions.bjjeire.cucumber.context.ScenarioContext;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the lifecycle of a Cucumber scenario using Spring for dependency injection.
 */
public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private static final ObjectMapper jsonMapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .addModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .build();

    @Autowired
    private ScenarioContext scenarioContext;

    @Before
    public void setup(Scenario scenario) {
        logger.info("--- Starting Scenario: '{}' ---", scenario.getName());
        scenarioContext.getCleanupActions().clear();
        scenarioContext.setValidatableResponse(null);
        scenarioContext.setRequestPayload(null);
    }

    @After
    public void tearDown(Scenario scenario) {
        // Execute cleanup actions FIRST.
        List<Runnable> actions = scenarioContext.getCleanupActions();
        if (!actions.isEmpty()) {
            logger.info("--- Executing {} cleanup action(s) for scenario: '{}' ---", actions.size(), scenario.getName());
            Collections.reverse(actions);
            actions.forEach(action -> {
                try {
                    action.run();
                } catch (Exception e) {
                    logger.error("Error during cleanup action: {}", e.getMessage(), e);
                }
            });
        }

        if (scenario.isFailed()) {
            logger.error("--- Scenario FAILED: '{}' ---", scenario.getName());
            logFailureDetails(scenario);
        } else {
            logger.info("--- Scenario PASSED: '{}' ---", scenario.getName());
        }
    }

    private void logFailureDetails(Scenario scenario) {
        Map<String, Object> failureContext = new LinkedHashMap<>();
        failureContext.put("scenarioName", scenario.getName());

        if (scenarioContext.getValidatableResponse() != null) {
            ValidatableResponse response = scenarioContext.getValidatableResponse();
            failureContext.put("lastApiRequestUrl", response.getRequestUrl());
            failureContext.put("lastApiResponseCode", response.getStatusCode());
            failureContext.put("lastApiResponseBody", response.getBody());
        }

        if (scenarioContext.getRequestPayload() != null) {
            try {
                failureContext.put("lastApiRequestPayload", jsonMapper.writeValueAsString(scenarioContext.getRequestPayload()));
            } catch (Exception e) {
                logger.error("Could not serialize request payload to JSON for logging.", e);
                failureContext.put("lastApiRequestPayload", "Could not serialize request payload.");
            }
        }

        try {
            String failureJson = jsonMapper.writeValueAsString(failureContext);
            logger.error("Failure Context:\n{}", failureJson);
            scenario.attach(failureJson, "application/json", "Failure Context");
        } catch (Exception e) {
            logger.error("Could not serialize failure context to JSON.", e);
        }
    }
}