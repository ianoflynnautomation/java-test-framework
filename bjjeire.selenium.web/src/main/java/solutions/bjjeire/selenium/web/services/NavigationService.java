package solutions.bjjeire.selenium.web.services;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import lombok.SneakyThrows;
import net.logstash.logback.argument.StructuredArguments;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.pages.WebPage;

@Service
@Slf4j
public class NavigationService extends WebService {

    private final WebSettings webSettings;

    public NavigationService(DriverService driverService, WebSettings webSettings) {
        super(driverService);
        this.webSettings = webSettings;
    }

    public void to(String url) {

        log.info("Navigating to URL", StructuredArguments.keyValue("url", url));
        getWrappedDriver().navigate().to(url);
    }

    @SneakyThrows
    public void to(WebPage page) {

        log.info("Navigating to page", StructuredArguments.keyValue("pageClass", page.getClass().getSimpleName()));
        Method method = page.getClass().getDeclaredMethod("getUrl");
        method.setAccessible(true);
        String url = (String) method.invoke(page);
        to(url);
    }

    public void toLocalPage(String filePath) {

        log.info("Navigating to local page resource", StructuredArguments.keyValue("filePath", filePath));
        URL testAppUrl = Thread.currentThread().getContextClassLoader().getResource(filePath);
        if (testAppUrl != null) {
            to(testAppUrl.toString());
        } else {

            log.error("Local page resource not found", StructuredArguments.keyValue("filePath", filePath));
            throw new IllegalArgumentException("Local page resource not found: " + filePath);
        }
    }

    public void waitForPartialUrl(String partialUrl) {
        long waitForPartialTimeout = webSettings.getTimeoutSettings().getWaitForPartialUrl();
        long sleepInterval = webSettings.getTimeoutSettings().getSleepInterval();

        log.debug("Waiting for partial URL",
                StructuredArguments.keyValue("timeoutSeconds", waitForPartialTimeout),
                StructuredArguments.keyValue("partialUrl", partialUrl));

        try {
            var webDriverWait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(waitForPartialTimeout),
                    Duration.ofSeconds(sleepInterval));
            webDriverWait.until(ExpectedConditions.urlContains(partialUrl));
        } catch (TimeoutException ex) {
            String currentUrl = getWrappedDriver().getCurrentUrl();

            log.error("Timeout waiting for partial URL",
                    StructuredArguments.keyValue("partialUrl", partialUrl),
                    StructuredArguments.keyValue("currentUrl", currentUrl),
                    ex);
            throw ex;
        }
    }
}