package solution.bjjeire.selenium.web.waitstrategies;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import solution.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.core.configuration.ConfigurationService;

public class ToBeVisibleWaitStrategy extends WaitStrategy {
    public ToBeVisibleWaitStrategy() {
        timeoutInterval = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getElementToBeVisibleTimeout();
        sleepInterval = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getSleepInterval();
    }

    public ToBeVisibleWaitStrategy(long timeoutIntervalSeconds, long sleepIntervalSeconds) {
        super(timeoutIntervalSeconds, sleepIntervalSeconds);
    }

    public static ToBeVisibleWaitStrategy of() {
        return new ToBeVisibleWaitStrategy();
    }

    @Override
    public void waitUntil(SearchContext searchContext, By by) {
        waitUntil((x) -> elementIsVisible(searchContext, by));
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
