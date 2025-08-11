package solutions.bjjeire.selenium.web.services;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenshotService {

    private final DriverService driverService;

    public void takeScreenshotOnFailure(Scenario scenario) {
            log.debug("Scenario failed. Attempting to take a screenshot.",
                    StructuredArguments.keyValue("scenarioName", scenario.getName()),
                    StructuredArguments.keyValue("scenarioId", scenario.getId()));
            try {
                WebDriver driver = driverService.getWrappedDriver();
                if (driver instanceof TakesScreenshot takesScreenshot) {
                    final byte[] screenshot = takesScreenshot.getScreenshotAs(OutputType.BYTES);

                    scenario.attach(screenshot, "image/png", "Screenshot on Failure");
                    log.info("Screenshot captured and attached to the report.",
                            StructuredArguments.keyValue("scenarioName", scenario.getName()),
                            StructuredArguments.keyValue("scenarioId", scenario.getId()));
                } else {
                    log.warn("WebDriver instance does not support taking screenshots.",
                            StructuredArguments.keyValue("scenarioName", scenario.getName()),
                            StructuredArguments.keyValue("scenarioId", scenario.getId()),
                            StructuredArguments.keyValue("driverInstance", driver != null ? driver.getClass().getName() : "null"));
                }
            } catch (Exception e) {
                log.error("Failed to take screenshot.", e,
                        StructuredArguments.keyValue("scenarioName", scenario.getName()),
                        StructuredArguments.keyValue("scenarioId", scenario.getId()));
            }
    }
}