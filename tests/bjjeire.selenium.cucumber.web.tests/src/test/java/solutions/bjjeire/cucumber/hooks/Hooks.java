package solutions.bjjeire.cucumber.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.infrastructure.BrowserConfiguration;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;
import solutions.bjjeire.selenium.web.infrastructure.Lifecycle;

public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);

    @Autowired
    private DriverService driverService;

    @Autowired
    private WebSettings webSettings;

    @Before
    public void beforeScenario(Scenario scenario) {
        logger.info("Starting Scenario: '{}'", scenario.getName());

        BrowserConfiguration defaultConfig = new BrowserConfiguration(
                webSettings.getDefaultBrowserEnum(),
                Lifecycle.fromText(webSettings.getDefaultLifeCycle()),
                webSettings.getDefaultBrowserWidth(),
                webSettings.getDefaultBrowserHeight()
        );
        defaultConfig.setTestName(scenario.getName());

        logger.info("Starting browser with default configuration for scenario.");
        driverService.start(defaultConfig);
    }

    @After
    public void afterScenario(Scenario scenario) {
        logger.info("Finished Scenario: '{}' with status: {}", scenario.getName(), scenario.getStatus());

        try {
            WebDriver driver = driverService.getWrappedDriver();
            if (driver != null && scenario.isFailed()) {
                logger.error("Scenario failed! Capturing screenshot...");

                if (driver instanceof TakesScreenshot) {
                    final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "Screenshot on failure");
                } else {
                    logger.warn("WebDriver does not support taking screenshots.");
                }
            }
        } catch (Exception e) {
            logger.error("Failed to take screenshot or get driver on scenario failure.", e);
        } finally {
            logger.info("Closing browser after scenario.");
            driverService.close();
        }
    }
}

