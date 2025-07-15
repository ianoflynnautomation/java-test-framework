package solution.bjjeire.selenium.web.waitstrategies;

import org.openqa.selenium.*;
import solution.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.core.configuration.ConfigurationService;

public class ToExistWaitStrategy extends WaitStrategy {
    public ToExistWaitStrategy() {
        timeoutInterval = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getElementToExistTimeout();
        sleepInterval = ConfigurationService.get(WebSettings.class).getTimeoutSettings().getSleepInterval();
    }

    public ToExistWaitStrategy(long timeoutIntervalSeconds, long sleepIntervalSeconds) {
        super(timeoutIntervalSeconds, sleepIntervalSeconds);
    }

    public static ToExistWaitStrategy of() {
        return new ToExistWaitStrategy();
    }

    @Override
    public void waitUntil(SearchContext searchContext, By by) {
        waitUntil((x) -> elementExists(searchContext, by));
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
