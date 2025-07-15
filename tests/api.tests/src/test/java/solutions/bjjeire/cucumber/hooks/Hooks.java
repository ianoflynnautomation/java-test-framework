package solutions.bjjeire.cucumber.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private final ScenarioContext scenarioContext;

    public Hooks(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

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

    private void logFailureDetails(Scenario scenario) {
        Map<String, Object> failureContext = new LinkedHashMap<>();
        if (scenarioContext.getResponseAsserter() != null) {
            failureContext.put("lastApiResponseCode", scenarioContext.getResponseAsserter().getStatusCode());
            failureContext.put("lastApiResponseBody", scenarioContext.getResponseAsserter().getResponseBodyAsString());
        } else {
            failureContext.put("lastApiResponse", "No API response was recorded in the context.");
        }
        if (scenarioContext.getRequestPayload() != null) {
            try {
                failureContext.put("lastApiRequestPayload", jsonMapper.writeValueAsString(scenarioContext.getRequestPayload()));
            } catch (Exception e) {
                failureContext.put("lastApiRequestPayload", "Could not serialize request payload.");
            }
        }

        try {
            String failureJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(failureContext);
            logger.error("Failure Context:\n{}", failureJson);
            scenario.attach(failureJson, "application/json", "Failure Context");
        } catch (Exception e) {
            logger.error("Could not serialize failure context to JSON.", e);
        }
    }
}
