package solutions.bjjeire.selenium.web.services;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import solutions.bjjeire.selenium.web.configuration.GridSettings;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.core.utilities.SecretsResolver;
import solutions.bjjeire.core.utilities.TimestampBuilder;
import solutions.bjjeire.core.plugins.BrowserConfiguration;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;

/**
 * A Spring-managed service responsible for creating and managing WebDriver
 * instances.
 */
@Service
public class DriverService {
    private static final Logger log = LoggerFactory.getLogger(DriverService.class);

    private final ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<>();
    private final WebSettings webSettings;

    private static final List<WebDriver> ALL_DRIVERS = Collections.synchronizedList(new ArrayList<>());

    static {
        log.info("Registering JVM shutdown hook for global browser cleanup.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM shutdown initiated. Closing all managed WebDriver instances...");
            new ArrayList<>(ALL_DRIVERS).forEach(driver -> {
                try {
                    if (driver != null) {
                        log.debug("Shutting down driver instance: {}", driver);
                        driver.quit();
                    }
                } catch (Exception e) {
                    log.error("Error while shutting down a WebDriver instance during JVM shutdown.", e);
                }
            });
            log.info("Global browser cleanup complete. All WebDriver instances have been shut down.");
        }));
    }

    public DriverService(WebSettings webSettings) {
        this.webSettings = webSettings;
    }

    public WebDriver start(BrowserConfiguration configuration) {
        log.info("Starting new browser with configuration: {}", configuration);
        if (webDriverThreadLocal.get() != null) {
            log.warn("A WebDriver instance already exists for this thread. Closing it before starting a new one.");
            close();
        }

        WebDriver driver;
        String executionType = webSettings.getExecutionType();

        if (executionType == null) {
            throw new IllegalStateException(
                    "The 'executionType' property is not set. Please check your configuration.");
        }

        switch (executionType.toLowerCase()) {
            case "grid", "selenoid", "healenium" -> driver = initializeDriverGridMode(configuration, executionType);
            case "regular" -> driver = initializeDriverRegularMode(configuration);
            default -> driver = initializeDriverCloudGridMode(configuration, executionType);
        }

        driver.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(webSettings.getTimeoutSettings().getPageLoadTimeout()));
        driver.manage().timeouts()
                .scriptTimeout(Duration.ofSeconds(webSettings.getTimeoutSettings().getScriptTimeout()));

        if (configuration.getHeight() != 0 && configuration.getWidth() != 0) {
            driver.manage().window().setSize(new Dimension(configuration.getWidth(), configuration.getHeight()));
        } else {
            driver.manage().window().maximize();
        }

        log.info("Window resized to dimensions: {}", driver.manage().window().getSize());

        webDriverThreadLocal.set(driver);
        ALL_DRIVERS.add(driver);

        return driver;
    }

    public void close() {
        WebDriver driver = webDriverThreadLocal.get();
        if (driver != null) {
            log.info("Closing WebDriver instance for thread: {}", Thread.currentThread().getName());
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

    private WebDriver initializeDriverRegularMode(BrowserConfiguration config) {
        log.debug("Initializing driver in 'regular' mode for browser: {}", config.getBrowser());
        return switch (config.getBrowser()) {
            case CHROME -> new ChromeDriver(applyCommonOptions(new ChromeOptions(), config));
            case CHROME_HEADLESS ->
                new ChromeDriver(applyCommonOptions(new ChromeOptions().addArguments("--headless=new"), config));
            case FIREFOX -> new FirefoxDriver(applyCommonOptions(new FirefoxOptions(), config));
            case FIREFOX_HEADLESS ->
                new FirefoxDriver(applyCommonOptions(new FirefoxOptions().addArguments("--headless"), config));
            case EDGE -> new EdgeDriver(applyCommonOptions(new EdgeOptions(), config));
            case EDGE_HEADLESS ->
                new EdgeDriver(applyCommonOptions(new EdgeOptions().addArguments("--headless"), config));
            case SAFARI -> new SafariDriver(applyCommonOptions(new SafariOptions(), config));
            case INTERNET_EXPLORER ->
                new InternetExplorerDriver(applyCommonOptions(new InternetExplorerOptions(), config));
            default ->
                throw new IllegalArgumentException("Unsupported browser for regular execution: " + config.getBrowser());
        };
    }

    private WebDriver initializeDriverGridMode(BrowserConfiguration config, String providerName) {
        GridSettings gridSettings = findGridSettings(providerName);
        MutableCapabilities capabilities = switch (config.getBrowser()) {
            case CHROME, CHROME_HEADLESS -> applyCommonOptions(new ChromeOptions(), config);
            case FIREFOX, FIREFOX_HEADLESS -> applyCommonOptions(new FirefoxOptions(), config);
            case EDGE, EDGE_HEADLESS -> applyCommonOptions(new EdgeOptions(), config);
            case SAFARI -> applyCommonOptions(new SafariOptions(), config);
            case INTERNET_EXPLORER -> applyCommonOptions(new InternetExplorerOptions(), config);
            default ->
                throw new IllegalArgumentException("Unsupported browser for grid execution: " + config.getBrowser());
        };
        addGridOptions(capabilities, gridSettings);
        try {
            String gridUrl = getUrl(gridSettings.getUrl());
            return new RemoteWebDriver(new URI(gridUrl).toURL(), capabilities);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException("Invalid Grid URL: " + gridSettings.getUrl(), e);
        }
    }

    private WebDriver initializeDriverCloudGridMode(BrowserConfiguration config, String providerName) {
        GridSettings gridSettings = findGridSettings(providerName);
        MutableCapabilities capabilities = switch (config.getBrowser()) {
            case CHROME, CHROME_HEADLESS -> new ChromeOptions();
            case FIREFOX, FIREFOX_HEADLESS -> new FirefoxOptions();
            case EDGE, EDGE_HEADLESS -> new EdgeOptions();
            case SAFARI -> new SafariOptions();
            default ->
                throw new IllegalArgumentException("Unsupported browser for cloud execution: " + config.getBrowser());
        };
        HashMap<String, Object> cloudOptions = new HashMap<>();
        cloudOptions.put("sessionName", config.getTestName());
        addCloudOptions(cloudOptions, gridSettings);
        capabilities.setCapability(gridSettings.getOptionsName(), cloudOptions);
        try {
            String gridUrl = getUrl(gridSettings.getUrl());
            return new RemoteWebDriver(new URI(gridUrl).toURL(), capabilities);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException("Invalid Cloud Grid URL: " + gridSettings.getUrl(), e);
        }
    }

    private <T extends MutableCapabilities> T applyCommonOptions(T options, BrowserConfiguration config) {
        options.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
        config.getDriverOptions().forEach(options::setCapability);
        return options;
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

    private String getUrl(String url) {
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