package solutions.bjjeire.selenium.web.waitstrategies;

import org.openqa.selenium.*;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.DriverService;

public class ToExistWaitStrategy extends WaitStrategy {

    public ToExistWaitStrategy(WebSettings webSettings) {
        this.timeoutInterval = webSettings.getTimeoutSettings().getElementToBeClickableTimeout();
        this.sleepInterval = webSettings.getTimeoutSettings().getSleepInterval();
    }

    public ToExistWaitStrategy(long timeoutIntervalSeconds, long sleepIntervalSeconds) {
        super(timeoutIntervalSeconds, sleepIntervalSeconds);
    }

    @Override
    public void waitUntil(DriverService driverService, SearchContext searchContext, By by) {
        waitUntil(driverService, (d) -> elementExists(searchContext, by));
    }

    private boolean elementExists(SearchContext searchContext, By by) {
        try {
            var element = findElement(searchContext, by);
            return element != null;
        } catch (NoSuchElementException | StaleElementReferenceException | ElementNotInteractableException e) {
            return false;
        }
    }
}
