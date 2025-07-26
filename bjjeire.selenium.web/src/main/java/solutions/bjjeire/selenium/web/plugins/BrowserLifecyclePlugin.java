package solutions.bjjeire.selenium.web.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import solutions.bjjeire.core.plugins.*;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.infrastructure.*;
import solutions.bjjeire.selenium.web.services.DriverService;

import java.lang.reflect.Method;

@Component
public class BrowserLifecyclePlugin implements Plugin {

    private static final Logger log = LoggerFactory.getLogger(BrowserLifecyclePlugin.class);
    private final DriverService driverService;
    private final WebSettings webSettings;

    private final ThreadLocal<BrowserConfiguration> currentBrowserConfiguration = new ThreadLocal<>();
    private final ThreadLocal<BrowserConfiguration> previousBrowserConfiguration = new ThreadLocal<>();
    private final ThreadLocal<Boolean> isBrowserStartedCorrectly = ThreadLocal.withInitial(() -> false);

    public BrowserLifecyclePlugin(DriverService driverService, WebSettings webSettings) {
        this.driverService = driverService;
        this.webSettings = webSettings;
    }

    @Override
    public void preBeforeTest(TestResult testResult, Method memberInfo) {
        log.debug("Executing pre-before-test hook for method: {}", memberInfo.getName());
        BrowserConfiguration config = getBrowserConfiguration(memberInfo);
        startBrowser(config);
    }

    @Override
    public void postAfterTest(TestResult testResult, TimeRecord timeRecord, Method memberInfo, Throwable failedTestException) {
        log.debug("Executing post-after-test hook for method: {}", memberInfo.getName());
        handleBrowserShutdown(testResult, this.currentBrowserConfiguration.get());
    }

    @Override
    public void preBeforeScenario(ScenarioContext context) {
        log.debug("Executing pre-before-scenario hook for: {}", context.getScenarioName());
        startBrowser(context.getBrowserConfiguration());
    }

    @Override
    public void postAfterScenario(ScenarioContext context) {
        log.debug("Executing post-after-scenario hook for: {}", context.getScenarioName());
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
                log.error("Failed to start the browser. This is the root cause of the test failure.", ex);
                isBrowserStartedCorrectly.set(false);
                throw new RuntimeException("Failed to initialize WebDriver. Check logs for the original exception.", ex);
            }
            previousBrowserConfiguration.set(currentBrowserConfiguration.get());
        }
    }


    private void handleBrowserShutdown(TestResult testResult, BrowserConfiguration config) {
        if (config == null) {
            log.warn("Current browser configuration not found in post-test hook.");
            return;
        }

        if (config.getLifecycle() == Lifecycle.REUSE_IF_STARTED) {
            log.debug("Browser lifecycle is 'REUSE_IF_STARTED', keeping browser open.");
            return;
        }

        if (config.getLifecycle() == Lifecycle.RESTART_ON_FAIL && testResult != TestResult.FAILURE) {
            log.debug("Browser lifecycle is 'RESTART_ON_FAIL' and test passed, keeping browser open.");
            return;
        }

        shutdownBrowser();
    }

    private void shutdownBrowser() {
        driverService.close();
        previousBrowserConfiguration.remove();
    }

    private boolean shouldRestartBrowser() {
        var previousConfig = previousBrowserConfiguration.get();
        var currentConfig = currentBrowserConfiguration.get();
        if (previousConfig == null) return true;
        if (!isBrowserStartedCorrectly.get()) return true;
        return !previousConfig.equals(currentConfig);
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
        if (!memberInfo.isAnnotationPresent(ExecutionBrowser.class)) return null;
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
            if (annotation.browser() != Browser.NOT_SET) browser = annotation.browser();
            if (annotation.lifecycle() != lifecycle) lifecycle = annotation.lifecycle();
            if (annotation.width() != 0) width = annotation.width();
            if (annotation.height() != 0) height = annotation.height();
            if (annotation.browser() == Browser.CHROME_MOBILE) {
                return new BrowserConfiguration(annotation.deviceName(), lifecycle, clazz.getName());
            }
        }
        return new BrowserConfiguration(browser, lifecycle, width, height);
    }
}