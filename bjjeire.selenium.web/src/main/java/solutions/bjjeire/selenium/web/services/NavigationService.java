package solutions.bjjeire.selenium.web.services;

import lombok.SneakyThrows;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;
import solutions.bjjeire.selenium.web.pages.WebPage;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;

@Service
public class NavigationService extends WebService {

    private final WebSettings webSettings;

    @Autowired
    public NavigationService(DriverService driverService, WebSettings webSettings) {
        super(driverService);
        this.webSettings = webSettings;
    }

    public void to(String url) {
        getWrappedDriver().navigate().to(url);
    }

    @SneakyThrows
    public void to(WebPage page) {
        Method method = page.getClass().getDeclaredMethod("getUrl");
        method.setAccessible(true);
        getWrappedDriver().navigate().to((String) method.invoke(page));
    }

    public void toLocalPage(String filePath) {
        URL testAppUrl = Thread.currentThread().getContextClassLoader().getResource(filePath);
        if (testAppUrl != null) {
            to(testAppUrl.toString());
        }
    }

    public void waitForPartialUrl(String partialUrl) {
        try {
            long waitForPartialTimeout = webSettings.getTimeoutSettings().getWaitForPartialUrl();
            long sleepInterval = webSettings.getTimeoutSettings().getSleepInterval();
            var webDriverWait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(waitForPartialTimeout), Duration.ofSeconds(sleepInterval));
            webDriverWait.until(ExpectedConditions.urlContains(partialUrl));
        } catch (TimeoutException ex) {
            // TODO: Add proper logging and event handling
            throw ex;
        }
    }
}