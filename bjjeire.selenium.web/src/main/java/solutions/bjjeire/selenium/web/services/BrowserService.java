package solutions.bjjeire.selenium.web.services;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.testng.Assert;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Service
public class BrowserService extends WebService {

    private final WebSettings webSettings;

    @Autowired
    public BrowserService(DriverService driverService, WebSettings webSettings) {
        super(driverService);
        this.webSettings = webSettings;
    }

    public String getUrl() {
        return getWrappedDriver().getCurrentUrl();
    }

    public void refresh() {
        getWrappedDriver().navigate().refresh();
    }

    public void waitUntilPageLoadsCompletely() {
        try {
            WebDriverWait wait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(webSettings.getTimeoutSettings().getPageLoadTimeout()));
            wait.until(driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
        } catch (ScriptTimeoutException ex) {
            // TODO: Add proper logging
        }
    }

    public void waitForPartialUrl(String partialUrl) {
        long timeout = webSettings.getTimeoutSettings().getWaitForPartialUrl();
        WebDriverWait wait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(timeout));
        wait.until(ExpectedConditions.urlContains(partialUrl));
    }

    public void assertLandedOnPage(String partialUrl) {
        waitUntilPageLoadsCompletely();
        String currentBrowserUrl = getWrappedDriver().getCurrentUrl().toLowerCase();
        Assert.assertTrue(currentBrowserUrl.contains(partialUrl.toLowerCase()),
                "The expected partialUrl: '" + partialUrl + "' was not found in the PageUrl: '" + currentBrowserUrl + "'");
    }

    public void assertUrl(String fullUrl) {
        String currentBrowserUrl = getWrappedDriver().getCurrentUrl();
        try {
            URI actualUri = new URI(currentBrowserUrl);
            URI expectedUri = new URI(fullUrl);
            Assert.assertEquals(actualUri.toASCIIString(), expectedUri.toASCIIString(),
                    "Expected URL is different than the Actual one.");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}