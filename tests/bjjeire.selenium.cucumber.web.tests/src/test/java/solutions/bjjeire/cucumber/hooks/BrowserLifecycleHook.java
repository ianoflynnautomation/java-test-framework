package solutions.bjjeire.cucumber.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import solutions.bjjeire.core.plugins.Browser;
import solutions.bjjeire.core.plugins.BrowserConfiguration;
import solutions.bjjeire.core.plugins.Lifecycle;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.CookiesService;
import solutions.bjjeire.selenium.web.services.DriverService;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class BrowserLifecycleHook {

    private final DriverService driverService;
    private final ScenarioContext scenarioContext;
    private final WebSettings webSettings;
    private final CookiesService cookiesService;
    private final BrowserService browserService;

    private static final ThreadLocal<BrowserConfiguration> SCENARIO_BROWSER_CONFIG = new ThreadLocal<>();

    @Before(order = 100)
    public void beforeScenario(Scenario scenario) {
        BrowserConfiguration config = parseBrowserConfigurationFromTags(scenario);
        SCENARIO_BROWSER_CONFIG.set(config);

        driverService.start(config);

        scenarioContext.setDriver(driverService.getWrappedDriver());
        log.info("Browser prepared for scenario '{}' with config: {}", scenario.getName(), config);
    }


    @After(order = 999)
    public void afterScenario(Scenario scenario) {
        try {
            log.debug("Clearing browser state (cookies, local storage, session storage).");
            cookiesService.deleteAllCookies();
            browserService.clearLocalStorage();
            browserService.clearSessionStorage();

            // Delegate browser stopping logic to the DriverService
            //driverService.(SCENARIO_BROWSER_CONFIG.get());

        } catch (Exception e) {
            log.error("Error during browser cleanup for scenario: {}", scenario.getName(), e);
        } finally {
            SCENARIO_BROWSER_CONFIG.remove();
            scenarioContext.setDriver(null);
        }
    }

    private BrowserConfiguration parseBrowserConfigurationFromTags(Scenario scenario) {
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
                String[] dimensions = tag.substring(13).split("x");
                if (dimensions.length == 2) {
                    try {
                        width = Integer.parseInt(dimensions[0].trim());
                        height = Integer.parseInt(dimensions[1].trim());
                    } catch (NumberFormatException e) {
                        log.warn("Could not parse @BrowserSize tag: '{}'. Using defaults.", tag);
                    }
                }
            }
        }
        var config = new BrowserConfiguration(browser, lifecycle, width, height);
        config.setTestName(scenario.getName());
        return config;
    }
}