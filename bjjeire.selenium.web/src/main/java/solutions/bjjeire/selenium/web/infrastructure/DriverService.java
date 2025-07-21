package solutions.bjjeire.selenium.web.infrastructure;

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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;

/**
 * A Spring-managed service responsible for creating and managing WebDriver instances.
 */
@Service
public class DriverService {
    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    private final ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<>();
    private final WebSettings webSettings;

    public DriverService(WebSettings webSettings) {
        this.webSettings = webSettings;
    }

    /**
     * Starts a new browser instance based on the provided configuration.
     * This method is thread-safe.
     *
     * @param configuration The desired browser configuration.
     * @return The initialized WebDriver instance.
     */
    public WebDriver start(BrowserConfiguration configuration) {
        logger.info("Starting new browser with configuration: {}", configuration);
        WebDriver driver;
        String executionType = webSettings.getExecutionType();

        switch (executionType.toLowerCase()) {
            case "grid":
            case "selenoid":
            case "healenium":
                driver = initializeDriverGridMode(configuration, executionType);
                break;
            case "regular":
                driver = initializeDriverRegularMode(configuration);
                break;
            default: // Cloud providers like BrowserStack, LambdaTest, etc.
                driver = initializeDriverCloudGridMode(configuration, executionType);
                break;
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(webSettings.getTimeoutSettings().getPageLoadTimeout()));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(webSettings.getTimeoutSettings().getScriptTimeout()));

        if (configuration.getHeight() != 0 && configuration.getWidth() != 0) {
            driver.manage().window().setSize(new Dimension(configuration.getWidth(), configuration.getHeight()));
        } else {
            driver.manage().window().maximize();
        }

        logger.info("Window resized to dimensions: {}", driver.manage().window().getSize());
        webDriverThreadLocal.set(driver);
        return driver;
    }

    /**
     * Closes the WebDriver instance for the current thread.
     */
    public void close() {
        WebDriver driver = webDriverThreadLocal.get();
        if (driver != null) {
            logger.info("Closing WebDriver instance for thread: {}", Thread.currentThread().getName());
            driver.quit();
            webDriverThreadLocal.remove();
        }
    }

    public WebDriver getWrappedDriver() {
        return webDriverThreadLocal.get();
    }

    private WebDriver initializeDriverRegularMode(BrowserConfiguration config) {
        switch (config.getBrowser()) {
            case CHROME:
                return new ChromeDriver(applyCommonOptions(new ChromeOptions(), config));
            case CHROME_HEADLESS:
                return new ChromeDriver(applyCommonOptions(new ChromeOptions().addArguments("--headless=new"), config));
            case FIREFOX:
                return new FirefoxDriver(applyCommonOptions(new FirefoxOptions(), config));
            case FIREFOX_HEADLESS:
                return new FirefoxDriver(applyCommonOptions(new FirefoxOptions().addArguments("--headless"), config));
            case EDGE:
                return new EdgeDriver(applyCommonOptions(new EdgeOptions(), config));
            case EDGE_HEADLESS:
                return new EdgeDriver(applyCommonOptions(new EdgeOptions().addArguments("--headless"), config));
            case SAFARI:
                return new SafariDriver(applyCommonOptions(new SafariOptions(), config));
            case INTERNET_EXPLORER:
                return new InternetExplorerDriver(applyCommonOptions(new InternetExplorerOptions(), config));
            default:
                throw new IllegalArgumentException("Unsupported browser for regular execution: " + config.getBrowser());
        }
    }

    private WebDriver initializeDriverGridMode(BrowserConfiguration config, String providerName) {
        GridSettings gridSettings = findGridSettings(providerName);
        MutableCapabilities capabilities;

        switch (config.getBrowser()) {
            case CHROME, CHROME_HEADLESS -> capabilities = applyCommonOptions(new ChromeOptions(), config);
            case FIREFOX, FIREFOX_HEADLESS -> capabilities = applyCommonOptions(new FirefoxOptions(), config);
            case EDGE, EDGE_HEADLESS -> capabilities = applyCommonOptions(new EdgeOptions(), config);
            case SAFARI -> capabilities = applyCommonOptions(new SafariOptions(), config);
            case INTERNET_EXPLORER -> capabilities = applyCommonOptions(new InternetExplorerOptions(), config);
            default -> throw new IllegalArgumentException("Unsupported browser for grid execution: " + config.getBrowser());
        }

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
        MutableCapabilities capabilities;

        switch (config.getBrowser()) {
            case CHROME, CHROME_HEADLESS -> capabilities = new ChromeOptions();
            case FIREFOX, FIREFOX_HEADLESS -> capabilities = new FirefoxOptions();
            case EDGE, EDGE_HEADLESS -> capabilities = new EdgeOptions();
            case SAFARI -> capabilities = new SafariOptions();
            default -> throw new IllegalArgumentException("Unsupported browser for cloud execution: " + config.getBrowser());
        }

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
        //options.setAcceptInsecureCerts(true);
        options.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
        config.getDriverOptions().forEach(options::setCapability);
        return options;
    }

    private void addGridOptions(MutableCapabilities capabilities, GridSettings gridSettings) {
        gridSettings.getArguments().stream()
                .flatMap(map -> map.entrySet().stream())
                .forEach(entry -> {
                    Object value = resolveValue(entry.getValue());
                    capabilities.setCapability(entry.getKey(), value);
                });
    }

    private void addCloudOptions(HashMap<String, Object> options, GridSettings gridSettings) {
        gridSettings.getArguments().stream()
                .flatMap(map -> map.entrySet().stream())
                .forEach(entry -> {
                    Object value = resolveValue(entry.getValue());
                    if (entry.getKey().equalsIgnoreCase("build")) {
                        value = getBuildName(value.toString());
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
        String buildName = System.getProperty("buildName");
        if (buildName == null || buildName.isEmpty()) {
            buildName = defaultValue;
        }
        if ("{randomNumber}".equals(buildName)) {
            buildName = TimestampBuilder.buildUniqueTextByPrefix("RUN_");
        }
        return buildName;
    }

    private String getUrl(String url) {
        if (url.startsWith("{env_")) {
            return SecretsResolver.getSecret(url);
        }
        // Simplified for clarity, can be expanded to handle multiple placeholders
        return url;
    }

    private GridSettings findGridSettings(String providerName) {
        return webSettings.getGridSettings().stream()
                .filter(g -> providerName.equalsIgnoreCase(g.getProviderName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Grid settings for provider '" + providerName + "' not found in configuration."));
    }
}
