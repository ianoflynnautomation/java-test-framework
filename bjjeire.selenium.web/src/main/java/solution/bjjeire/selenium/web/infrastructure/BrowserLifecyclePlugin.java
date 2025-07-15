package solution.bjjeire.selenium.web.infrastructure;

import solution.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.core.configuration.ConfigurationService;
import solutions.bjjeire.core.plugins.Plugin;
import solutions.bjjeire.core.plugins.TestResult;
import solutions.bjjeire.core.utilities.SecretsResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

public class BrowserLifecyclePlugin extends Plugin {
    private static final ThreadLocal<BrowserConfiguration> CURRENT_BROWSER_CONFIGURATION;
    private static final ThreadLocal<BrowserConfiguration> PREVIOUS_BROWSER_CONFIGURATION;
    private static final ThreadLocal<Boolean> IS_BROWSER_STARTED_CORRECTLY;

    static {
        CURRENT_BROWSER_CONFIGURATION = new ThreadLocal<>();
        PREVIOUS_BROWSER_CONFIGURATION = new ThreadLocal<>();
        IS_BROWSER_STARTED_CORRECTLY = ThreadLocal.withInitial(() -> false);
    }

    @Override
    public void preBeforeClass(Class type) {
        if (Objects.equals(ConfigurationService.get(WebSettings.class).getExecutionType(), "regular")) {
            CURRENT_BROWSER_CONFIGURATION.set(getBrowserConfiguration(type));
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
        super.preAfterClass(type);
    }

    @Override
    public void preBeforeTest(TestResult testResult, Method memberInfo) {
        CURRENT_BROWSER_CONFIGURATION.set(getBrowserConfiguration(memberInfo));
        if (shouldRestartBrowser()) {
            shutdownBrowser();
            startBrowser();
        }
    }

    @Override
    public void beforeTestFailed(Exception ex) throws Exception {
        throw ex;
    }

//    @Override
    public void postAfterTest(TestResult testResult, Method memberInfo, Throwable failedTestException) {

        if (CURRENT_BROWSER_CONFIGURATION.get().getLifecycle() == Lifecycle.REUSE_IF_STARTED) {
            return;
        }

        if (CURRENT_BROWSER_CONFIGURATION.get().getLifecycle() ==
                Lifecycle.RESTART_ON_FAIL && testResult != TestResult.FAILURE ) {
            return;
        }

        shutdownBrowser();
    }

    private void shutdownBrowser() {
        DriverService.close();
        PREVIOUS_BROWSER_CONFIGURATION.remove();
    }

    private void startBrowser() {
        try {
            DriverService.start(CURRENT_BROWSER_CONFIGURATION.get());
            IS_BROWSER_STARTED_CORRECTLY.set(true);
        } catch (Exception ex) {
//            Log.error("Error occurred while trying to start browser: %s".formatted(ex.getMessage()));
            IS_BROWSER_STARTED_CORRECTLY.set(false);
        }

        PREVIOUS_BROWSER_CONFIGURATION.set(CURRENT_BROWSER_CONFIGURATION.get());
    }

    private boolean shouldRestartBrowser() {
        // TODO: IsBrowserStartedCorrectly getter?
        var previousConfiguration = PREVIOUS_BROWSER_CONFIGURATION.get();
        var currentConfiguration = CURRENT_BROWSER_CONFIGURATION.get();
        if (previousConfiguration == null) {
            return true;
        } else if (!IS_BROWSER_STARTED_CORRECTLY.get()) {
            return true;
        } else if (!previousConfiguration.equals(currentConfiguration)) {
            return true;
        } else {
            return false;
        }
    }

    private BrowserConfiguration getBrowserConfiguration(Method memberInfo) {
        BrowserConfiguration result;
        var classBrowserType = getExecutionBrowserClassLevel(memberInfo.getDeclaringClass());
        var methodBrowserType = getExecutionBrowserMethodLevel(memberInfo);
        result = Objects.requireNonNullElse(methodBrowserType, classBrowserType);
        String testFullName = String.format("%s.%s", memberInfo.getDeclaringClass().getName(), memberInfo.getName());
        result.setTestName(testFullName);
        return result;
    }

    private BrowserConfiguration getBrowserConfiguration(Type classType) {
        return getExecutionBrowserClassLevel((Class<?>)classType);
    }

    private BrowserConfiguration getExecutionBrowserMethodLevel(Method memberInfo) {
        if (!memberInfo.isAnnotationPresent(ExecutionBrowser.class)) return null;

        ExecutionBrowser executionBrowserAnnotation = (ExecutionBrowser)memberInfo.getDeclaredAnnotation(ExecutionBrowser.class);
        return new BrowserConfiguration(executionBrowserAnnotation.browser(), executionBrowserAnnotation.deviceName(), executionBrowserAnnotation.lifecycle());
    }

    private BrowserConfiguration getExecutionBrowserClassLevel(Class<?> clazz) {
        Browser browser = Browser.fromText(SecretsResolver.getSecret(ConfigurationService.get(WebSettings.class).getDefaultBrowser()));
        var lifecycle = Lifecycle.fromText(ConfigurationService.get(WebSettings.class).getDefaultLifeCycle());
        var width = ConfigurationService.get(WebSettings.class).getDefaultBrowserWidth();
        var height = ConfigurationService.get(WebSettings.class).getDefaultBrowserHeight();

        if (clazz.isAnnotationPresent(ExecutionBrowser.class)) {
            ExecutionBrowser executionBrowserAnnotation = clazz.getDeclaredAnnotation(ExecutionBrowser.class);

            browser = executionBrowserAnnotation.browser() != Browser.NOT_SET && executionBrowserAnnotation.browser() != browser ? executionBrowserAnnotation.browser() : browser;
            lifecycle = executionBrowserAnnotation.lifecycle() != lifecycle ? executionBrowserAnnotation.lifecycle() : lifecycle;
            width = executionBrowserAnnotation.width() != 0 ? executionBrowserAnnotation.width() : width;
            height = executionBrowserAnnotation.height() != 0 ? executionBrowserAnnotation.height() : height;

            if (executionBrowserAnnotation.browser() == Browser.CHROME_MOBILE) {
                return new BrowserConfiguration(executionBrowserAnnotation.deviceName(), lifecycle, clazz.getName());
            }
        }

        return new BrowserConfiguration(browser, lifecycle, width, height);
    }
}