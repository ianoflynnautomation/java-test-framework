package solutions.bjjeire.selenium.web.plugins;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.logstash.logback.argument.StructuredArguments;
import solutions.bjjeire.core.plugins.Browser;
import solutions.bjjeire.core.plugins.BrowserConfiguration;
import solutions.bjjeire.core.plugins.Lifecycle;
import solutions.bjjeire.core.plugins.Plugin;
import solutions.bjjeire.core.plugins.ScenarioContext;
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.core.plugins.TimeRecord;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.infrastructure.ExecutionBrowser;
import solutions.bjjeire.selenium.web.services.DriverService;

@Component
public class BrowserLifecyclePlugin implements Plugin {

    private final ThreadLocal<BrowserConfiguration> currentBrowserConfiguration = new ThreadLocal<>();
    private final ThreadLocal<BrowserConfiguration> previousBrowserConfiguration = new ThreadLocal<>();
    private final ThreadLocal<Boolean> isBrowserStartedCorrectly = ThreadLocal.withInitial(() -> false);
    private static final Logger log = LoggerFactory.getLogger(BrowserLifecyclePlugin.class);
    private final DriverService driverService;
    private final WebSettings webSettings;

    public BrowserLifecyclePlugin(DriverService driverService, WebSettings webSettings) {
        this.driverService = driverService;
        this.webSettings = webSettings;
    }

    @Override
    public void preBeforeTest(TestResult testResult, Method memberInfo) {
        log.debug("Executing pre-before-test hook",
                StructuredArguments.keyValue("methodName", memberInfo.getName()),
                StructuredArguments.keyValue("testResult", testResult.name()));
        BrowserConfiguration config = getBrowserConfiguration(memberInfo);
        startBrowser(config);
    }

    @Override
    public void postAfterTest(TestResult testResult, TimeRecord timeRecord, Method memberInfo,
            Throwable failedTestException) {

        log.debug("Executing post-after-test hook",
                StructuredArguments.keyValue("methodName", memberInfo.getName()),
                StructuredArguments.keyValue("testResult", testResult.name()));
        // StructuredArguments.keyValue("durationMillis",
        // timeRecord.getDurationMillis()));

        handleBrowserShutdown(testResult, this.currentBrowserConfiguration.get());
    }

    @Override
    public void preBeforeScenario(ScenarioContext context) {

        log.debug("Executing pre-before-scenario hook",
                StructuredArguments.keyValue("scenarioName", context.getScenarioName()));
        startBrowser(context.getBrowserConfiguration());
    }

    @Override
    public void postAfterScenario(ScenarioContext context) {
        log.debug("Executing post-after-scenario hook",
                StructuredArguments.keyValue("scenarioName", context.getScenarioName()),
                StructuredArguments.keyValue("testResult", context.getTestResult().name()));

        handleBrowserShutdown(context.getTestResult(), context.getBrowserConfiguration());
    }

    private void startBrowser(BrowserConfiguration config) {
        currentBrowserConfiguration.set(config);
        if (shouldRestartBrowser()) {
            shutdownBrowser();
            try {
                driverService.start(currentBrowserConfiguration.get());
                isBrowserStartedCorrectly.set(true);
            } catch (Exception ex) {

                log.error("Failed to start the browser",
                        StructuredArguments.keyValue("browserConfig", currentBrowserConfiguration.get()),
                        ex);
                isBrowserStartedCorrectly.set(false);
                throw new RuntimeException("Failed to initialize WebDriver. Check logs for the original exception.",
                        ex);
            }
            previousBrowserConfiguration.set(currentBrowserConfiguration.get());
        }
    }

    private void handleBrowserShutdown(TestResult testResult, BrowserConfiguration config) {
        if (config == null) {
            log.warn("Current browser configuration not found in post-test hook.");
            return;
        }

        switch (config.getLifecycle()) {
            case REUSE_IF_STARTED:

                log.debug("Browser lifecycle is REUSE_IF_STARTED",
                        StructuredArguments.keyValue("lifecycle", "REUSE_IF_STARTED"),
                        StructuredArguments.keyValue("action", "keeping browser open"));
                break;
            case RESTART_ON_FAIL:
                if (testResult != TestResult.FAILURE) {

                    log.debug("Browser lifecycle is RESTART_ON_FAIL and test passed",
                            StructuredArguments.keyValue("lifecycle", "RESTART_ON_FAIL"),
                            StructuredArguments.keyValue("testResult", testResult.name()),
                            StructuredArguments.keyValue("action", "keeping browser open"));
                    break;
                }
            default:
                shutdownBrowser();
                break;
        }
    }

    private void shutdownBrowser() {
        driverService.close();
        previousBrowserConfiguration.remove();

        log.debug("Browser shutdown completed.");
    }

    private boolean shouldRestartBrowser() {
        var previousConfig = previousBrowserConfiguration.get();
        var currentConfig = currentBrowserConfiguration.get();
        if (previousConfig == null) {
            log.debug("Previous browser configuration is null, starting new browser.");
            return true;
        }
        if (!isBrowserStartedCorrectly.get()) {
            log.debug("Browser did not start correctly, forcing restart.");
            return true;
        }
        if (!previousConfig.equals(currentConfig)) {
            log.debug("Browser configuration changed, forcing restart.");
            return true;
        }
        return false;
    }

    private BrowserConfiguration getBrowserConfiguration(Method memberInfo) {
        BrowserConfiguration classConfig = getExecutionBrowserClassLevel(memberInfo.getDeclaringClass());
        BrowserConfiguration methodConfig = getExecutionBrowserMethodLevel(memberInfo);
        BrowserConfiguration result = (methodConfig != null) ? methodConfig : classConfig;
        String testFullName = String.format("%s.%s", memberInfo.getDeclaringClass().getName(), memberInfo.getName());
        result.setTestName(testFullName);
        return result;
    }

    private BrowserConfiguration getExecutionBrowserMethodLevel(Method memberInfo) {
        if (!memberInfo.isAnnotationPresent(ExecutionBrowser.class))
            return null;
        ExecutionBrowser annotation = memberInfo.getAnnotation(ExecutionBrowser.class);
        return new BrowserConfiguration(annotation.browser(), annotation.deviceName(), annotation.lifecycle());
    }

    private BrowserConfiguration getExecutionBrowserClassLevel(Class<?> clazz) {
        Browser browser = webSettings.getDefaultBrowserEnum();
        Lifecycle lifecycle = Lifecycle.fromText(webSettings.getDefaultLifeCycle());
        int width = webSettings.getDefaultBrowserWidth();
        int height = webSettings.getDefaultBrowserHeight();

        if (clazz.isAnnotationPresent(ExecutionBrowser.class)) {
            ExecutionBrowser annotation = clazz.getAnnotation(ExecutionBrowser.class);
            if (annotation.browser() != Browser.NOT_SET)
                browser = annotation.browser();
            if (annotation.lifecycle() != lifecycle)
                lifecycle = annotation.lifecycle();
            if (annotation.width() != 0)
                width = annotation.width();
            if (annotation.height() != 0)
                height = annotation.height();
            if (annotation.browser() == Browser.CHROME_MOBILE) {
                return new BrowserConfiguration(annotation.deviceName(), lifecycle, clazz.getName());
            }
        }
        return new BrowserConfiguration(browser, lifecycle, width, height);
    }
}