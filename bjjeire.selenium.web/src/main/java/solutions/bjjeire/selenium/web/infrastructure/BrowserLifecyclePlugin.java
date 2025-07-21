package solutions.bjjeire.selenium.web.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import solutions.bjjeire.core.plugins.Plugin;
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.core.plugins.TimeRecord;
import solutions.bjjeire.selenium.web.configuration.WebSettings;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * A Spring-managed plugin to control the browser lifecycle during test execution.
 * It replaces the static, ThreadLocal-based approach with a proper bean that
 * receives its dependencies via constructor injection.
 */
@Component
public class BrowserLifecyclePlugin extends Plugin {

    private final DriverService driverService;
    private final WebSettings webSettings;

    private final ThreadLocal<BrowserConfiguration> currentBrowserConfiguration = new ThreadLocal<>();
    private final ThreadLocal<BrowserConfiguration> previousBrowserConfiguration = new ThreadLocal<>();
    private final ThreadLocal<Boolean> isBrowserStartedCorrectly = ThreadLocal.withInitial(() -> false);

    @Autowired
    public BrowserLifecyclePlugin(DriverService driverService, WebSettings webSettings) {
        this.driverService = driverService;
        this.webSettings = webSettings;
    }

    @Override
    public void preBeforeClass(Class type) {
        if ("regular".equalsIgnoreCase(webSettings.getExecutionType())) {
            currentBrowserConfiguration.set(getBrowserConfiguration(type));
            if (shouldRestartBrowser()) {
                shutdownBrowser();
                startBrowser();
            }
        }
        super.preBeforeClass(type);
    }

    @Override
    public void postAfterClass(Class type) {
        shutdownBrowser();
        super.postAfterClass(type);
    }

    @Override
    public void preBeforeTest(TestResult testResult, Method memberInfo) {
        currentBrowserConfiguration.set(getBrowserConfiguration(memberInfo));
        if (shouldRestartBrowser()) {
            shutdownBrowser();
            startBrowser();
        }
    }

    @Override
    public void beforeTestFailed(Exception ex) throws Exception {
        // Potentially add screenshot/logging logic here
        throw ex;
    }

    @Override
    public void postAfterTest(TestResult testResult, TimeRecord timeRecord, Method memberInfo, Throwable failedTestException) {
        BrowserConfiguration config = currentBrowserConfiguration.get();
        if (config == null) return;

        if (config.getLifecycle() == Lifecycle.REUSE_IF_STARTED) {
            return;
        }

        if (config.getLifecycle() == Lifecycle.RESTART_ON_FAIL && testResult != TestResult.FAILURE) {
            return;
        }

        shutdownBrowser();
    }

    private void shutdownBrowser() {
        driverService.close(); // Uses the injected service
        previousBrowserConfiguration.remove();
    }

    private void startBrowser() {
        try {
            driverService.start(currentBrowserConfiguration.get());
            isBrowserStartedCorrectly.set(true);
        } catch (Exception ex) {
            // TODO: Add proper logging
            isBrowserStartedCorrectly.set(false);
        }
        previousBrowserConfiguration.set(currentBrowserConfiguration.get());
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

    private BrowserConfiguration getBrowserConfiguration(Type classType) {
        return getExecutionBrowserClassLevel((Class<?>) classType);
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
