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
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.infrastructure.BrowserConfiguration;
import solutions.bjjeire.selenium.web.infrastructure.BrowserLifecyclePlugin;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.infrastructure.Lifecycle;


public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    @Autowired
    private DriverService driverService;

    @Autowired
    private WebSettings webSettings;

    @Autowired
    private BrowserLifecyclePlugin browserLifecyclePlugin;

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("Starting Scenario: '{}'", scenario.getName());

        // The @Before hook is now responsible for starting the browser for each scenario.
        // The BrowserLifecyclePlugin will decide whether to shut it down in the @After hook.
        BrowserConfiguration defaultConfig = new BrowserConfiguration(
                webSettings.getDefaultBrowserEnum(),
                Lifecycle.fromText(webSettings.getDefaultLifeCycle()),
                webSettings.getDefaultBrowserWidth(),
                webSettings.getDefaultBrowserHeight()
        );
        defaultConfig.setTestName(scenario.getName());

        log.info("Starting browser with default configuration for scenario.");
        driverService.start(defaultConfig);
    }

    @After
    public void afterScenario(Scenario scenario) {
        log.info("Finished Scenario: '{}' with status: {}", scenario.getName(), scenario.getStatus());

        if (scenario.isFailed()) {
            log.error("Scenario failed! Capturing and attaching screenshot...");
            try {
                WebDriver driver = driverService.getWrappedDriver();
                if (driver instanceof TakesScreenshot) {
                    final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "Screenshot on failure");
                    log.info("Screenshot attached to Cucumber report.");
                } else {
                    log.warn("WebDriver does not support taking screenshots.");
                }
            } catch (Exception e) {
                log.error("Failed to capture screenshot on scenario failure.", e);
            }finally {
                log.info("Closing browser after scenario.");
                driverService.close();
            }
        }

        // Convert Cucumber's scenario status to our internal TestResult enum.
        TestResult testResult = scenario.isFailed() ? TestResult.FAILURE : TestResult.SUCCESS;

        // Delegate the decision to shut down the browser to the centralized plugin logic.
        browserLifecyclePlugin.handleBrowserShutdown(testResult);
    }
}