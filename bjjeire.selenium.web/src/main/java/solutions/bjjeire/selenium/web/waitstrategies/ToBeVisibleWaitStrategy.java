package solutions.bjjeire.selenium.web.waitstrategies;

import org.openqa.selenium.*;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.DriverService;

public class ToBeVisibleWaitStrategy extends WaitStrategy {

    public ToBeVisibleWaitStrategy(WebSettings webSettings) {
        this.timeoutInterval = webSettings.getTimeoutSettings().getElementToBeClickableTimeout();
        this.sleepInterval = webSettings.getTimeoutSettings().getSleepInterval();
    }

    public ToBeVisibleWaitStrategy(long timeoutIntervalSeconds, long sleepIntervalSeconds) {
        super(timeoutIntervalSeconds, sleepIntervalSeconds);
    }


    @Override
    public void waitUntil(DriverService driverService, SearchContext searchContext, By by) {
        waitUntil(driverService, (d) -> elementIsVisible(searchContext, by));
    }

    private boolean elementIsVisible(SearchContext searchContext, By by) {
        var element = findElement(searchContext, by);
        try {
            return element != null && element.isDisplayed();
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            return false;
        }
    }
}
