package solutions.bjjeire.cucumber.hooks;

import io.cucumber.java.After;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.gyms.Gym;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.cucumber.context.TestDataContext;
import solutions.bjjeire.selenium.web.data.TestDataManager;

@Slf4j
@RequiredArgsConstructor
public class TestDataLifecycleHook {

    private final TestDataManager testDataManager;
    private final ScenarioContext scenarioContext;
    private final TestDataContext testDataContext;

    @After(order = 1000)
    public void afterScenario() {
        log.info("Executing scenario teardown hook", StructuredArguments.keyValue("eventName", "teardownHookStart"));
        String authToken = scenarioContext.getAuthToken();
        if (authToken == null || authToken.isBlank()) {
            log.debug("Skipping data teardown due to missing auth token",
                    StructuredArguments.keyValue("eventName", "teardownSkipped"),
                    StructuredArguments.keyValue("reason", "No auth token in ScenarioContext"));
            return;
        }

        var eventIds = testDataContext.getEntityIds(BjjEvent.class);
        if (!eventIds.isEmpty()) {
            log.info("Tearing down BJJ event entities",
                    StructuredArguments.keyValue("eventName", "teardownEntityStart"),
                    StructuredArguments.keyValue("entityType", BjjEvent.class.getSimpleName()),
                    StructuredArguments.keyValue("count", eventIds.size()));
            testDataManager.teardown(BjjEvent.class, eventIds, authToken);
        }

        var gymIds = testDataContext.getEntityIds(Gym.class);
        if (!gymIds.isEmpty()) {
            log.info("Tearing down gym entities",
                    StructuredArguments.keyValue("eventName", "teardownEntityStart"),
                    StructuredArguments.keyValue("entityType", Gym.class.getSimpleName()),
                    StructuredArguments.keyValue("count", gymIds.size()));
            testDataManager.teardown(Gym.class, gymIds, authToken);
        }

        testDataContext.clearAll();
        log.info("Scenario teardown hook finished", StructuredArguments.keyValue("eventName", "teardownHookEnd"));
    }
}