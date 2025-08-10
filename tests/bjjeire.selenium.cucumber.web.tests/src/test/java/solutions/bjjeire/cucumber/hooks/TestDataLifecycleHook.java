package solutions.bjjeire.cucumber.hooks;

import io.cucumber.java.After;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        String authToken = scenarioContext.getAuthToken();
        if (authToken == null || authToken.isBlank()) {
            log.debug("No auth token found in ScenarioContext, skipping data teardown.");
            return;
        }

        var eventIds = testDataContext.getEntityIds(BjjEvent.class);
        if (!eventIds.isEmpty()) {
            log.info("Tearing down {} created event(s).", eventIds.size());
            testDataManager.teardown(BjjEvent.class, eventIds, authToken);
        }

        var gymIds = testDataContext.getEntityIds(Gym.class);
        if (!gymIds.isEmpty()) {
            log.info("Tearing down {} created gym(s).", gymIds.size());
            testDataManager.teardown(Gym.class, gymIds, authToken);
        }

        testDataContext.clearAll();
    }
}