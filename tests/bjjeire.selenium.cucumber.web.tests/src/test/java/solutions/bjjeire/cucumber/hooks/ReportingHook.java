package solutions.bjjeire.cucumber.hooks;

import java.util.UUID;

import org.slf4j.MDC;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import solutions.bjjeire.selenium.web.services.ScreenshotService;

@Slf4j
@RequiredArgsConstructor
public class ReportingHook {

    private final ScreenshotService screenshotService;

    private static final String SCENARIO_ID_KEY = "scenarioId";
    private static final String SCENARIO_NAME_KEY = "scenarioName";

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        MDC.put(SCENARIO_ID_KEY, UUID.randomUUID().toString().substring(0, 8));
        MDC.put(SCENARIO_NAME_KEY, scenario.getName());

        log.info("--- SCENARIO START ---",
                StructuredArguments.keyValue("scenarioName", scenario.getName()),
                StructuredArguments.keyValue("tags", scenario.getSourceTagNames()));
    }

    @After(order = 10000)
    public void afterScenario(Scenario scenario) {

        try {
            if (scenario.isFailed()) {
                log.error("Scenario failed",
                        StructuredArguments.keyValue("status", scenario.getStatus()));

                screenshotService.takeScreenshotOnFailure(scenario);
            } else {
                log.info("Scenario finished successfully",
                        StructuredArguments.keyValue("status", scenario.getStatus()));
            }
        } finally {
            log.info("--- SCENARIO END ---");
            MDC.clear();
        }
    }
}