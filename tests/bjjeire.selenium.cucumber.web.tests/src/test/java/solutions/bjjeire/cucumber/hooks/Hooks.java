package solutions.bjjeire.cucumber.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.core.plugins.*;
import solutions.bjjeire.cucumber.context.BaseContext;
import solutions.bjjeire.cucumber.context.EventContext;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.data.TestDataManager;
import solutions.bjjeire.selenium.web.plugins.BrowserLifecyclePlugin;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.CookiesService;
import solutions.bjjeire.selenium.web.services.DriverService;

import java.util.Collection;
import java.util.UUID;

public class Hooks extends UsesPlugins {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    @Autowired private DriverService driverService;
    @Autowired private TestDataManager testDataManager;
    @Autowired private BaseContext baseContext;
    @Autowired private EventContext eventContext;
    @Autowired private WebSettings webSettings;
    @Autowired private CookiesService cookiesService;
    @Autowired private BrowserService browserService;

    private static final String SCENARIO_ID = "scenarioId";
    private static final String SCENARIO_NAME = "scenarioName";

    private static final ThreadLocal<Boolean> IS_CUCUMBER_CONFIGURED = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<BrowserConfiguration> SCENARIO_BROWSER_CONFIG = new ThreadLocal<>();

    private void configureCucumberPlugins() {
        if (!IS_CUCUMBER_CONFIGURED.get()) {
            log.info("Configuring plugins for Cucumber execution...");
            addPlugin(BrowserLifecyclePlugin.class);
            IS_CUCUMBER_CONFIGURED.set(true);
        }
    }

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        configureCucumberPlugins();

        MDC.put(SCENARIO_ID, UUID.randomUUID().toString().substring(0, 8));
        MDC.put(SCENARIO_NAME, scenario.getName());

        log.info("--- SCENARIO START ---");

        BrowserConfiguration config = getBrowserConfigurationFromTags(scenario);
        SCENARIO_BROWSER_CONFIG.set(config);

        ScenarioContext context = new ScenarioContext(scenario.getName(), null, config);
        PluginExecutionEngine.preBeforeScenario(context);

        baseContext.setDriver(driverService.getWrappedDriver());
        log.debug("Browser prepared. Config: {}", config);
    }

    @After(order = 999)
    public void afterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                log.error("Scenario failed. Status: {}", scenario.getStatus());
                captureScreenshot(scenario);
            } else {
                log.info("Scenario finished. Status: {}", scenario.getStatus());
            }

            if (baseContext.getAuthToken() != null && !eventContext.getCreatedEventIds().isEmpty()) {
                testDataManager.teardown(eventContext.getCreatedEventIds(), baseContext.getAuthToken());
                eventContext.getCreatedEventIds().clear();
            }

            TestResult result = scenario.isFailed() ? TestResult.FAILURE : TestResult.SUCCESS;
            BrowserConfiguration config = SCENARIO_BROWSER_CONFIG.get();
            ScenarioContext context = new ScenarioContext(scenario.getName(), result, config);
            PluginExecutionEngine.postAfterScenario(context);

        } finally {
            log.info("--- SCENARIO END ---");
            MDC.clear();
            SCENARIO_BROWSER_CONFIG.remove();

            browserService.clearLocalStorage();
            browserService.clearSessionStorage();
            cookiesService.deleteAllCookies();
        }
    }

    private void captureScreenshot(Scenario scenario) {
        if (baseContext.getDriver() == null) {
            log.warn("Driver was null, cannot take screenshot.");
            return;
        }
        try {
            final byte[] screenshot = ((TakesScreenshot) baseContext.getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "screenshot-" + System.currentTimeMillis());
            log.debug("Screenshot attached to report.");
        } catch (Exception e) {
            log.error("Failed to capture or attach screenshot.", e);
        }
    }

    private BrowserConfiguration getBrowserConfigurationFromTags(Scenario scenario) {
        Collection<String> tags = scenario.getSourceTagNames();
        Browser browser = webSettings.getDefaultBrowserEnum();
        Lifecycle lifecycle = Lifecycle.fromText(webSettings.getDefaultLifeCycle());
        int width = webSettings.getDefaultBrowserWidth();
        int height = webSettings.getDefaultBrowserHeight();

        for (String tag : tags) {
            if (tag.startsWith("@Browser:")) {
                browser = Browser.valueOf(tag.substring(9).toUpperCase());
            } else if (tag.startsWith("@Lifecycle:")) {
                lifecycle = Lifecycle.fromText(tag.substring(11));
            } else if (tag.startsWith("@BrowserSize:")) {
                String[] dimensions = tag.substring(13).split(",");
                if (dimensions.length == 2) {
                    width = Integer.parseInt(dimensions[0].trim());
                    height = Integer.parseInt(dimensions[1].trim());
                }
            }
        }
        BrowserConfiguration config = new BrowserConfiguration(browser, lifecycle, width, height);
        config.setTestName(scenario.getName());
        return config;
    }
}