package solutions.bjjeire.selenium.web.services;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.pages.WebPage;

@Service
public class NavigationService extends WebService {

    private static final Logger log = LoggerFactory.getLogger(NavigationService.class);
    private final WebSettings webSettings;

    public NavigationService(DriverService driverService, WebSettings webSettings) {
        super(driverService);
        this.webSettings = webSettings;
    }

    public void to(String url) {
        log.info("Navigating to URL: {}", url);
        getWrappedDriver().navigate().to(url);
    }

    @SneakyThrows
    public void to(WebPage page) {
        log.info("Navigating to page: {}", page.getClass().getSimpleName());
        Method method = page.getClass().getDeclaredMethod("getUrl");
        method.setAccessible(true);
        String url = (String) method.invoke(page);
        to(url);
    }

    public void toLocalPage(String filePath) {
        log.info("Navigating to local page resource: {}", filePath);
        URL testAppUrl = Thread.currentThread().getContextClassLoader().getResource(filePath);
        if (testAppUrl != null) {
            to(testAppUrl.toString());
        } else {
            log.error("Local page resource not found at path: {}", filePath);
            throw new IllegalArgumentException("Local page resource not found: " + filePath);
        }
    }

    public void waitForPartialUrl(String partialUrl) {
        long waitForPartialTimeout = webSettings.getTimeoutSettings().getWaitForPartialUrl();
        long sleepInterval = webSettings.getTimeoutSettings().getSleepInterval();
        log.debug("Waiting up to {} seconds for URL to contain '{}'", waitForPartialTimeout, partialUrl);

        try {
            var webDriverWait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(waitForPartialTimeout),
                    Duration.ofSeconds(sleepInterval));
            webDriverWait.until(ExpectedConditions.urlContains(partialUrl));
        } catch (TimeoutException ex) {
            String currentUrl = getWrappedDriver().getCurrentUrl();
            log.error("TimeoutException: Waited for URL to contain '{}', but current URL is '{}'.", partialUrl,
                    currentUrl, ex);
            throw ex;
        }
    }
}