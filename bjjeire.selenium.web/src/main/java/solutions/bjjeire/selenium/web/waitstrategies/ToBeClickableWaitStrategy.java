package solutions.bjjeire.selenium.web.waitstrategies;

import org.openqa.selenium.*;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.DriverService;

public class ToBeClickableWaitStrategy extends WaitStrategy {

    public ToBeClickableWaitStrategy(WebSettings webSettings) {
        this.timeoutInterval = webSettings.getTimeoutSettings().getElementToBeClickableTimeout();
        this.sleepInterval = webSettings.getTimeoutSettings().getSleepInterval();
    }

    public ToBeClickableWaitStrategy(long timeoutIntervalSeconds, long sleepIntervalSeconds) {
        super(timeoutIntervalSeconds, sleepIntervalSeconds);
    }

    @Override
    public void waitUntil(DriverService driverService, SearchContext searchContext, By by) {
        waitUntil(driverService, (d) -> elementIsClickable(searchContext, by));
    }

    private boolean elementIsClickable(SearchContext searchContext, By by) {
        try {
            WebElement element = findElement(searchContext, by);
            return element != null && element.isDisplayed() && element.isEnabled();
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            return false;
        }
    }
}
