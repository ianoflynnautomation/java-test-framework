package solutions.bjjeire.cucumber.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.models.MeasuredResponse;
import solutions.bjjeire.cucumber.context.ScenarioContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages the lifecycle of a Cucumber scenario using Spring for dependency injection.
 */
public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Autowired
    private ScenarioContext scenarioContext;

    @Before
    public void setup(Scenario scenario) {
        logger.info("--- Starting Scenario: '{}' ---", scenario.getName());
        logger.debug("Tags: {}", scenario.getSourceTagNames());
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            logger.error("--- Scenario FAILED: '{}' ---", scenario.getName());
            logFailureDetails(scenario);
        } else {
            logger.info("--- Scenario PASSED: '{}' ---", scenario.getName());
        }
    }

    /**
     * Logs detailed context information when a scenario fails and attaches it to the report.
     * @param scenario The failing scenario.
     */
    private void logFailureDetails(Scenario scenario) {
        Map<String, Object> failureContext = new LinkedHashMap<>();
        failureContext.put("scenarioName", scenario.getName());
        failureContext.put("scenarioTags", scenario.getSourceTagNames());

        if (scenarioContext.getResponseAsserter() != null) {
            MeasuredResponse response = scenarioContext.getResponseAsserter().getResponse();
            failureContext.put("lastApiRequestUrl", response.requestUrl());
            failureContext.put("lastApiResponseCode", response.statusCode());
            failureContext.put("lastApiResponseBody", response.responseBodyAsString());
        } else {
            failureContext.put("lastApiResponse", "No API response was recorded in the context.");
        }

        if (scenarioContext.getRequestPayload() != null) {
            try {
                failureContext.put("lastApiRequestPayload", jsonMapper.writeValueAsString(scenarioContext.getRequestPayload()));
            } catch (Exception e) {
                logger.error("Could not serialize request payload to JSON.", e);
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