package solutions.bjjeire.selenium.web.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ScriptTimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.testng.Assert;

import solutions.bjjeire.selenium.web.configuration.WebSettings;

@Service
public class BrowserService extends WebService {

    private static final Logger log = LoggerFactory.getLogger(BrowserService.class);
    private final WebSettings webSettings;
    protected final JavaScriptService javaScriptService;

    public BrowserService(DriverService driverService, JavaScriptService javaScriptService, WebSettings webSettings) {
        super(driverService);
        this.javaScriptService = javaScriptService;
        this.webSettings = webSettings;
    }

    public String getUrl() {
        String currentUrl = getWrappedDriver().getCurrentUrl();
        log.debug("Current browser URL is: {}", currentUrl);
        return currentUrl;
    }

    public void refresh() {
        log.info("Refreshing the current page.");
        getWrappedDriver().navigate().refresh();
    }

    public void waitUntilPageLoadsCompletely() {
        long pageLoadTimeout = webSettings.getTimeoutSettings().getPageLoadTimeout();
        log.debug("Waiting up to {} seconds for document.readyState to be 'complete'.", pageLoadTimeout);
        try {
            WebDriverWait wait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(pageLoadTimeout));
            wait.until(driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState")
                    .equals("complete"));
        } catch (ScriptTimeoutException ex) {
            log.warn("Timed out waiting for page to load completely.", ex);
        }
    }

    public void clearLocalStorage() {
        javaScriptService.execute("localStorage.clear()");
    }

    public void clearSessionStorage() {

        javaScriptService.execute("sessionStorage.clear()");
    }

    public void waitForPartialUrl(String partialUrl) {
        long timeout = webSettings.getTimeoutSettings().getWaitForPartialUrl();
        log.debug("Waiting up to {} seconds for URL to contain '{}'", timeout, partialUrl);
        WebDriverWait wait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(timeout));
        wait.until(ExpectedConditions.urlContains(partialUrl));
    }

    public void assertLandedOnPage(String partialUrl) {
        waitUntilPageLoadsCompletely();
        String currentBrowserUrl = getUrl().toLowerCase();
        log.info("Asserting that current URL '{}' contains '{}'", currentBrowserUrl, partialUrl.toLowerCase());
        Assert.assertTrue(currentBrowserUrl.contains(partialUrl.toLowerCase()),
                String.format("The expected partialUrl: '%s' was not found in the PageUrl: '%s'", partialUrl,
                        currentBrowserUrl));
    }

    public void assertUrl(String fullUrl) {
        String currentBrowserUrl = getUrl();
        log.info("Asserting that current URL '{}' matches expected URL '{}'", currentBrowserUrl, fullUrl);
        try {
            URI actualUri = new URI(currentBrowserUrl);
            URI expectedUri = new URI(fullUrl);
            Assert.assertEquals(actualUri.toASCIIString(), expectedUri.toASCIIString(),
                    "Expected URL is different than the Actual one.");
        } catch (URISyntaxException e) {
            log.error("Invalid URL syntax for comparison. Actual: '{}', Expected: '{}'", currentBrowserUrl, fullUrl, e);
            throw new IllegalArgumentException("Invalid URL syntax provided for assertion.", e);
        }
    }
}