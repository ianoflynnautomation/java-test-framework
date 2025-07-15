package solution.bjjeire.selenium.web.services;

import lombok.SneakyThrows;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import solution.bjjeire.selenium.web.configuration.WebSettings;
import solution.bjjeire.selenium.web.pages.WebPage;
import solutions.bjjeire.core.configuration.ConfigurationService;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;

public class NavigationService extends WebService {

    public void to(String url) {
        getWrappedDriver().navigate().to(url);
    }

    @SneakyThrows
    public void to(WebPage page) {
        Method method = page.getClass().getDeclaredMethod("getUrl");
        method.setAccessible(true);

        getWrappedDriver().navigate().to((String)method.invoke(page));
    }

    public void toLocalPage(String filePath) {
        URL testAppUrl = Thread.currentThread().getContextClassLoader().getResource(filePath);
        if (testAppUrl != null) {
            to(testAppUrl.toString());
        } else {
            testAppUrl = getClass().getClassLoader().getResource(filePath);
            if (testAppUrl != null) {
                to(testAppUrl.toString());
            }
        }
    }

    public void waitForPartialUrl(String partialUrl) {
        try {
            long waitForPartialTimeout = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getWaitForPartialUrl();
            long sleepInterval = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getSleepInterval();
            var webDriverWait = new WebDriverWait(getWrappedDriver(), Duration.ofSeconds(waitForPartialTimeout), Duration.ofSeconds(sleepInterval));
            webDriverWait.until(d -> getWrappedDriver().getCurrentUrl().contains(partialUrl));
        } catch (TimeoutException ex) {
            // TODO: UrlNotNavigatedEvent?.Invoke(this, new UrlNotNavigatedEventArgs(ex));
            throw ex;
        }
    }

}
