package solutions.bjjeire.cucumber.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.MDC;
import solutions.bjjeire.cucumber.context.ScenarioContext;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ReportingHook {

    private final ScenarioContext scenarioContext;

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
                captureScreenshot(scenario);
            } else {
                log.info("Scenario finished successfully",
                        StructuredArguments.keyValue("status", scenario.getStatus()));
            }
        } finally {
            log.info("--- SCENARIO END ---");
            MDC.clear();
        }
    }

    private void captureScreenshot(Scenario scenario) {
        WebDriver driver = scenarioContext.getDriver();
        if (driver == null) {
            log.warn("Driver was null, cannot take screenshot for scenario: {}", scenario.getName());
            return;
        }
        try {
            final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "failure-screenshot");
            log.info("Screenshot attached to report for failed scenario.");
        } catch (Exception e) {
            log.error("Failed to capture or attach screenshot.", e);
        }
    }
}