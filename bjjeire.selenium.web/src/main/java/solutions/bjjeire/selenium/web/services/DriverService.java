package solutions.bjjeire.selenium.web.services;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import solutions.bjjeire.core.plugins.Browser;
import solutions.bjjeire.core.plugins.BrowserConfiguration;
import solutions.bjjeire.core.utilities.SecretsResolver;
import solutions.bjjeire.core.utilities.TimestampBuilder;
import solutions.bjjeire.selenium.web.configuration.GridSettings;
import solutions.bjjeire.selenium.web.configuration.WebSettings;

@Slf4j
@Service
public class DriverService {

    private final ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<>();
    private final WebSettings webSettings;
    private static final List<WebDriver> ALL_DRIVERS = Collections.synchronizedList(new ArrayList<>());

    public DriverService(WebSettings webSettings) {
        this.webSettings = webSettings;
    }

    static {
        log.info("Registering JVM shutdown hook for global browser cleanup.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM shutdown initiated. Closing all managed WebDriver instances...");
            new ArrayList<>(ALL_DRIVERS).forEach(driver -> {
                try {
                    if (driver != null) {
                        log.debug("Shutting down driver instance", StructuredArguments.keyValue("driverInstance", driver));
                        driver.quit();
                    }
                } catch (Exception e) {
                    log.error("Error while shutting down a WebDriver instance during JVM shutdown.", e);
                }
            });
            log.info("Global browser cleanup complete.");
        }));
    }


    public WebDriver start(BrowserConfiguration configuration) throws Exception {
        log.info("Starting new browser", StructuredArguments.keyValue("configuration", configuration));
        if (webDriverThreadLocal.get() != null) {
            log.warn("A WebDriver instance already exists for this thread. Closing it before starting a new one.");
            close();
        }

        WebDriver driver = createDriver(configuration);

        configureDriver(driver, configuration);

        webDriverThreadLocal.set(driver);
        ALL_DRIVERS.add(driver);

        log.info("Driver started successfully for thread: {}", Thread.currentThread().getName());
        return driver;
    }

    private WebDriver createDriver(BrowserConfiguration configuration) throws Exception {
        String executionType = Optional.ofNullable(webSettings.getExecutionType())
                .orElseThrow(() -> new Exception("The 'executionType' property is not set in the configuration."))
                .toLowerCase();

        return switch (executionType) {
            case "grid", "selenoid", "healenium" -> initializeGridDriver(configuration, executionType);
            case "regular" -> initializeLocalDriver(configuration);
            default -> initializeCloudDriver(configuration, executionType);
        };
    }

    private void configureDriver(WebDriver driver, BrowserConfiguration configuration) {
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(webSettings.getTimeoutSettings().getPageLoadTimeout()));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(webSettings.getTimeoutSettings().getScriptTimeout()));

        if (configuration.getHeight() != 0 && configuration.getWidth() != 0) {
            driver.manage().window().setSize(new Dimension(configuration.getWidth(), configuration.getHeight()));
        } else {
            driver.manage().window().maximize();
        }
        log.info("Window resized", StructuredArguments.keyValue("windowSize", driver.manage().window().getSize()));
    }

    public void close() {
        WebDriver driver = webDriverThreadLocal.get();
        if (driver != null) {

            log.info("Closing WebDriver instance",
                    StructuredArguments.keyValue("threadName", Thread.currentThread().getName()));
            try {
                driver.quit();
            } catch (Exception e) {
                log.error("An error occurred during WebDriver quit.", e);
            } finally {
                webDriverThreadLocal.remove();
                ALL_DRIVERS.remove(driver);
            }
        }
    }

    public WebDriver getWrappedDriver() {
        return webDriverThreadLocal.get();
    }

    private WebDriver initializeLocalDriver(BrowserConfiguration config) throws Exception {
        log.debug("Initializing driver in 'local' mode", StructuredArguments.keyValue("browser", config.getBrowser()));
        MutableCapabilities options = getBrowserOptions(config.getBrowser());
        applyCommonOptions(options, config);

        return switch (config.getBrowser()) {
            case CHROME, CHROME_HEADLESS -> new ChromeDriver((ChromeOptions) options);
            case FIREFOX, FIREFOX_HEADLESS -> new FirefoxDriver((FirefoxOptions) options);
            case EDGE, EDGE_HEADLESS -> new EdgeDriver((EdgeOptions) options);
            case SAFARI -> new SafariDriver((SafariOptions) options);
            case INTERNET_EXPLORER -> new InternetExplorerDriver((InternetExplorerOptions) options);
            default -> throw new Exception("Unsupported browser for local execution: " + config.getBrowser());
        };
    }

    private WebDriver initializeGridDriver(BrowserConfiguration config, String providerName) throws Exception {
        GridSettings gridSettings = findGridSettings(providerName);
        MutableCapabilities capabilities = getBrowserOptions(config.getBrowser());
        applyCommonOptions(capabilities, config);
        addGridOptions(capabilities, gridSettings);

        try {
            String gridUrl = getUrl(gridSettings.getUrl());
            log.info("Initializing driver in grid mode",
                    StructuredArguments.keyValue("gridProvider", providerName),
                    StructuredArguments.keyValue("gridUrl", gridUrl),
                    StructuredArguments.keyValue("browser", config.getBrowser()));
            return new RemoteWebDriver(new URI(gridUrl).toURL(), capabilities);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new Exception("Invalid Grid URL: " + gridSettings.getUrl(), e);
        }
    }

    private WebDriver initializeCloudDriver(BrowserConfiguration config, String providerName) throws Exception {
        GridSettings gridSettings = findGridSettings(providerName);
        MutableCapabilities capabilities = getBrowserOptions(config.getBrowser());

        HashMap<String, Object> cloudOptions = new HashMap<>();
        cloudOptions.put("sessionName", config.getTestName());
        addCloudOptions(cloudOptions, gridSettings);
        capabilities.setCapability(gridSettings.getOptionsName(), cloudOptions);

        try {
            String gridUrl = getUrl(gridSettings.getUrl());
            log.info("Initializing driver in cloud grid mode",
                    StructuredArguments.keyValue("cloudProvider", providerName),
                    StructuredArguments.keyValue("gridUrl", gridUrl),
                    StructuredArguments.keyValue("browser", config.getBrowser()));
            return new RemoteWebDriver(new URI(gridUrl).toURL(), capabilities);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new Exception("Invalid Cloud Grid URL: " + gridSettings.getUrl(), e);
        }
    }


    private MutableCapabilities getBrowserOptions(Browser browser) throws Exception {
        return switch (browser) {
            case CHROME -> new ChromeOptions();
            case CHROME_HEADLESS -> new ChromeOptions().addArguments("--headless=new");
            case FIREFOX -> new FirefoxOptions();
            case FIREFOX_HEADLESS -> new FirefoxOptions().addArguments("--headless");
            case EDGE -> new EdgeOptions();
            case EDGE_HEADLESS -> new EdgeOptions().addArguments("--headless");
            case SAFARI -> new SafariOptions();
            case INTERNET_EXPLORER -> new InternetExplorerOptions();
            default -> throw new Exception("Cannot create options for unsupported browser: " + browser);
        };
    }

    private void applyCommonOptions(MutableCapabilities options, BrowserConfiguration config) {
        config.getDriverOptions().forEach(options::setCapability);
    }
    private void addGridOptions(MutableCapabilities capabilities, GridSettings gridSettings) {
        gridSettings.getArguments().stream()
                .flatMap(map -> map.entrySet().stream())
                .forEach(entry -> capabilities.setCapability(entry.getKey(), resolveValue(entry.getValue())));
    }

    private void addCloudOptions(Map<String, Object> options, GridSettings gridSettings) {
        gridSettings.getArguments().stream()
                .flatMap(map -> map.entrySet().stream())
                .forEach(entry -> {
                    Object value = resolveValue(entry.getValue());
                    if ("build".equalsIgnoreCase(entry.getKey())) {
                        value = getBuildName(String.valueOf(value));
                    }
                    options.put(entry.getKey(), value);
                });
    }

    private Object resolveValue(Object value) {
        if (value instanceof String && ((String) value).startsWith("{env_")) {
            return SecretsResolver.getSecret((String) value);
        }
        return value;
    }

    private String getBuildName(String defaultValue) {
        String buildName = System.getProperty("buildName", defaultValue);
        if ("{randomNumber}".equals(buildName)) {
            buildName = TimestampBuilder.buildUniqueTextByPrefix("RUN_");
        }
        return buildName;
    }

    private String getUrl(@NotNull String url) {
        if (url.startsWith("{env_")) {
            return SecretsResolver.getSecret(url);
        }
        return url;
    }

    private GridSettings findGridSettings(String providerName) {
        return webSettings.getGridSettings().stream()
                .filter(g -> providerName.equalsIgnoreCase(g.getProviderName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Grid settings for provider '" + providerName + "' not found."));
    }
}