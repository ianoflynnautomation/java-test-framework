package solution.bjjeire.selenium.web.services;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import solution.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.core.configuration.ConfigurationService;
import solutions.bjjeire.core.utilities.Wait;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

public class BrowserService extends WebService {

    public JavascriptExecutor getJavascriptExecutor() {
        return (JavascriptExecutor)getWrappedDriver();
    }

    public String getPageSource() {
        return getWrappedDriver().getPageSource();
    }

    public String getUrl() {
        return getWrappedDriver().getCurrentUrl();
    }

    public String getTitle() {
        return getWrappedDriver().getTitle();
    }

    public void back() {
        getWrappedDriver().navigate().back();
    }

    public void setSize(int width, int height) {
        setSize(new Dimension(width, height));
    }

    public void setSize(Dimension dimension) {
        getWrappedDriver().manage().window().setSize(dimension);
    }

    public void maximize() {
        getWrappedDriver().manage().window().maximize();
    }

    public void minimize() {
        getWrappedDriver().manage().window().minimize();
    }

    public void forward() {
        getWrappedDriver().navigate().forward();
    }

    public void refresh() {
        getWrappedDriver().navigate().refresh();
    }

    public void switchToDefault() {
        getWrappedDriver().switchTo().defaultContent();
    }

    public void switchToActive() {
        getWrappedDriver().switchTo().activeElement();
    }

    public void switchToFirstBrowserTab() {
        getWrappedDriver().switchTo().window(getWrappedDriver().getWindowHandles().stream().findFirst().orElse(""));
    }

    public void switchToLastTab() {
        var handles = getWrappedDriver().getWindowHandles();
        getWrappedDriver().switchTo().window(handles.stream().reduce((first, second) -> second).orElse(""));
    }

    public void switchToNewTab() {
        getWrappedDriver().switchTo().newWindow(WindowType.TAB);
    }

    public void switchToTab(Runnable condition) {
        Wait.retry(() -> {
                    var handles = getWrappedDriver().getWindowHandles();
                    boolean shouldThrowException = true;
                    for (var currentHandle : handles) {
                        getWrappedDriver().switchTo().window(currentHandle);
                        try {
                            condition.run();
                            shouldThrowException = false;
                            break;
                        } catch (Exception ex) {
                            // ignore
                        }
                    }

                    if (shouldThrowException) {
                        throw new TimeoutException();
                    }
                },
                5,
                0,
                TimeoutException.class, NotFoundException.class, StaleElementReferenceException.class);
    }

    public void switchToTab(String tabName) {
        getWrappedDriver().switchTo().window(tabName);
    }

    public void clearSessionStorage() {
        getJavascriptExecutor().executeScript("sessionStorage.clear()");
    }

    public void removeItemFromLocalStorage(String item) {
        getJavascriptExecutor().executeScript(String.format("window.localStorage.removeItem('%s');", item));
    }

    public void scrollToBottom() {
        getJavascriptExecutor().executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    public void scrollToTop() {
        getJavascriptExecutor().executeScript("window.scrollTo(0, 0)");
    }

    public boolean isItemPresentInLocalStorage(String item) {
        return !(getJavascriptExecutor().executeScript(String.format("return window.localStorage.getItem('%s');", item)) == null);
    }

    public String getItemFromLocalStorage(String key) {
        return (String)getJavascriptExecutor().executeScript(String.format("return window.localStorage.getItem('%s');", key));
    }


    public void setItemInLocalStorage(String item, String value) {
        getJavascriptExecutor().executeScript(String.format("window.localStorage.setItem('%s','%s');", item, value));
    }

    public void clearLocalStorage() {
        getJavascriptExecutor().executeScript("localStorage.clear()");
    }

    public List<LogEntry> getBrowserLogs() {
        return getLogsByType(LogType.BROWSER);
    }

    public List<LogEntry> getLogsByType(String type) {
        try {
            return getWrappedDriver().manage().logs().get(type).toJson();
        } catch (UnsupportedCommandException ex) {
            // Unsupported browser
            return new ArrayList<>();
        }
    }
//
//    public void assertNoConsoleErrorsLogged() {
//        Assertions.assertEquals(new ArrayList<LogEntry>(),
//                getSevereLogEntries(),
//                "Severe Errors found in console. If they are expected, add them to the whitelist.");
//    }

    public void assertConsoleErrorLogged(String errorMessage, Level severity) {
        var errorLogs = getLogsByType(LogType.BROWSER);
        var filteredLog = errorLogs.stream().filter((log) -> log.getMessage().contains(errorMessage)).findFirst();
        Assertions.assertTrue(filteredLog.isPresent(), "Expected message '%s' not found in console. Actual Log: %s".formatted(errorMessage, errorLogs));
        Assertions.assertEquals(severity,
                filteredLog.get().getLevel(),
                "Log severity is not as expected for message '%s'.".formatted(errorMessage));
    }

    public List<String> getRequestEntries(String partialUrl) {
        return (List<String>)getJavascriptExecutor().executeScript(String.format("return window.performance.getEntriesByType('resource').filter(x => x.name.indexOf('%s') >= 0).map(y => y.name);", partialUrl));
    }

    public void waitUntilPageLoadsCompletely() {
        long waitUntilReadyTimeout = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getWaitUntilReadyTimeout();
        long sleepInterval = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getSleepInterval();
        var webDriverWait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(waitUntilReadyTimeout), Duration.ofSeconds(sleepInterval));
        try {
            webDriverWait.until(webDriver -> ((JavascriptExecutor)webDriver).executeScript("return document.readyState").equals("complete"));
        } catch (ScriptTimeoutException ex) {
//            Log.error("Script timeout while loading for page load.");
        }
    }

    public void waitForReactPageLoadsCompletely() {
        long waitUntilReadyTimeout = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getWaitUntilReadyTimeout();
        long sleepInterval = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getSleepInterval();
        var webDriverWait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(waitUntilReadyTimeout), Duration.ofSeconds(sleepInterval));
        webDriverWait.until(d -> getJavascriptExecutor().executeScript("return document.querySelector('[data-reactroot]') !== null"));
        webDriverWait.until(d -> getJavascriptExecutor().executeScript("return window.performance.timing.loadEventEnd > 0"));
    }

    public void tryWaitUntil(Function function) {
        long waitUntilReadyTimeout = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getWaitUntilReadyTimeout();
        long sleepInterval = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getSleepInterval();
        var webDriverWait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(waitUntilReadyTimeout), Duration.ofSeconds(sleepInterval));
        try {
            String message = Thread.currentThread().getStackTrace()[2].getMethodName();
            webDriverWait.withMessage("Timed out while executing method: %s".formatted(message));
            webDriverWait.until(function);
        } catch (TimeoutException exception) {
//            Log.error(String.format("Timed out waiting for the condition! %s", function.toString()));
        }
    }

    public void waitForPartialUrl(String partialUrl) {
        long waitForPartialUrlTimeout = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getWaitForPartialUrl();
        long sleepInterval = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getSleepInterval();
        var webDriverWait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(waitForPartialUrlTimeout), Duration.ofSeconds(sleepInterval));
        webDriverWait.until(ExpectedConditions.urlContains(partialUrl));
    }

    public void assertLandedOnPage(String partialUrl) {
        assertLandedOnPage(partialUrl, false);
    }

    public void assertLandedOnPage(String partialUrl, boolean shouldUrlEncode) {
        if (shouldUrlEncode) {
            partialUrl = URLEncoder.encode(partialUrl, StandardCharsets.UTF_8);
        }

        waitUntilPageLoadsCompletely();

        String currentBrowserUrl = getWrappedDriver().getCurrentUrl().toLowerCase();

        Assert.assertTrue(currentBrowserUrl.contains(partialUrl.toLowerCase()),
                "The expected partialUrl: '" + partialUrl + "' was not found in the PageUrl: '" + currentBrowserUrl + "'");
    }
    public void assertUrl(String fullUrl) {
        String currentBrowserUrl = getWrappedDriver().getCurrentUrl().toString();
        URI actualUri = null;
        URI expectedUri = null;
        try {
            actualUri = new URI(currentBrowserUrl);
            expectedUri = new URI(fullUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(actualUri.toASCIIString(), expectedUri.toASCIIString(),
                "Expected URL is different than the Actual one.");
    }

    public void assertUrlPath(String urlPath) {
        String currentBrowserUrl = getWrappedDriver().getCurrentUrl().toString();
        URI actualUri = null;
        try {
            actualUri = new URI(currentBrowserUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(urlPath, actualUri.getPath(),
                "Expected URL path is different than the Actual one.");
    }

}


