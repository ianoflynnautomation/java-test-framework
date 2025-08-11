package solutions.bjjeire.cucumber.hooks;

import java.util.Collection;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import solutions.bjjeire.core.plugins.Browser;
import solutions.bjjeire.core.plugins.BrowserConfiguration;
import solutions.bjjeire.core.plugins.Lifecycle;
import solutions.bjjeire.core.plugins.PluginExecutionEngine;
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.core.plugins.UsesPlugins;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.plugins.BrowserLifecyclePlugin;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.CookiesService;
import solutions.bjjeire.selenium.web.services.DriverService;

@Slf4j
@RequiredArgsConstructor
public class BrowserLifecycleHook extends UsesPlugins {

    private final DriverService driverService;
    private final WebSettings webSettings;
    private final CookiesService cookiesService;
    private final BrowserService browserService;
    private final ScenarioContext scenarioContext;
    private final ThreadLocal<solutions.bjjeire.core.plugins.ScenarioContext> pluginEngineContext = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_CUCUMBER_CONFIGURED = ThreadLocal.withInitial(() -> false);

    private void configureCucumberPlugins() {
        if (!IS_CUCUMBER_CONFIGURED.get()) {
            log.info("Configuring plugins for Cucumber execution...");
            // Add Plugins here
            addPlugin(BrowserLifecyclePlugin.class);
            IS_CUCUMBER_CONFIGURED.set(true);
        }
    }

    @Before(order = 100)
    public void beforeScenario(Scenario scenario) {
        configureCucumberPlugins();
        log.info("Preparing browser for scenario: '{}'", scenario.getName());

        BrowserConfiguration config = parseBrowserConfigurationFromTags(scenario);

        solutions.bjjeire.core.plugins.ScenarioContext coreContext = new solutions.bjjeire.core.plugins.ScenarioContext(
                scenario.getName(), null, config);
        this.pluginEngineContext.set(coreContext);

        PluginExecutionEngine.preBeforeScenario(coreContext);
        scenarioContext.setDriver(driverService.getWrappedDriver());

        log.info("Browser prepared with config: {}", config);
    }

    @After(order = 999)
    public void afterScenario(Scenario scenario) {
        solutions.bjjeire.core.plugins.ScenarioContext initialCoreContext = this.pluginEngineContext.get();
        if (initialCoreContext == null) {
            log.warn("Plugin Engine Context was not initialized for scenario: {}. Skipping post-scenario hooks.",
                    scenario.getName());
            return;
        }

        try {
            TestResult result = scenario.isFailed() ? TestResult.FAILURE : TestResult.SUCCESS;
            solutions.bjjeire.core.plugins.ScenarioContext finalCoreContext = new solutions.bjjeire.core.plugins.ScenarioContext(
                    initialCoreContext.getScenarioName(), result, initialCoreContext.getBrowserConfiguration());

            if (driverService.getWrappedDriver() != null) {
                log.debug("Clearing browser state (cookies, local storage, session storage).");
                cookiesService.deleteAllCookies();
                browserService.clearLocalStorage();
                browserService.clearSessionStorage();
            }

            PluginExecutionEngine.postAfterScenario(finalCoreContext);
            log.info("Browser teardown complete for scenario: '{}'", scenario.getName());

        } catch (Exception e) {
            log.error("Error during browser cleanup for scenario: {}", scenario.getName(), e);
        } finally {
            this.pluginEngineContext.remove();
            scenarioContext.setDriver(null);
        }
    }

    private BrowserConfiguration parseBrowserConfigurationFromTags(Scenario scenario) {
        Collection<String> tags = scenario.getSourceTagNames();

        Browser browser = webSettings.getDefaultBrowserEnum();
        Lifecycle lifecycle = Lifecycle.fromText(webSettings.getDefaultLifeCycle());
        int width = webSettings.getDefaultBrowserWidth();
        int height = webSettings.getDefaultBrowserHeight();
        String deviceName = null;

        for (String tag : tags) {
            if (tag.startsWith("@Browser:")) {
                browser = Browser.valueOf(tag.substring(9).toUpperCase());
            } else if (tag.startsWith("@Lifecycle:")) {
                lifecycle = Lifecycle.fromText(tag.substring(11));
            } else if (tag.startsWith("@BrowserSize:")) {
                String[] dimensions = tag.substring(13).split("x");
                if (dimensions.length == 2) {
                    try {
                        width = Integer.parseInt(dimensions[0].trim());
                        height = Integer.parseInt(dimensions[1].trim());
                    } catch (NumberFormatException e) {
                        log.warn("Could not parse @BrowserSize tag: '{}'. Using defaults.", tag);
                    }
                }
            } else if (tag.startsWith("@DeviceName:")) {
                deviceName = tag.substring(12).trim();
            }
        }

        BrowserConfiguration config;
        if (deviceName != null && browser == Browser.CHROME_MOBILE) {
            config = new BrowserConfiguration(Browser.CHROME_MOBILE, lifecycle, deviceName);
        } else {
            config = new BrowserConfiguration(browser, lifecycle, width, height);
        }

        config.setTestName(scenario.getName());
        return config;
    }
}