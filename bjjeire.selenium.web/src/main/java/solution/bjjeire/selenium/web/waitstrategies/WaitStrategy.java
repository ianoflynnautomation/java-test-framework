package solution.bjjeire.selenium.web.waitstrategies;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import solution.bjjeire.selenium.web.infrastructure.DriverService;

import java.time.Duration;
import java.util.function.Function;

public abstract class WaitStrategy {
    @Getter protected long timeoutInterval;
    @Getter protected long sleepInterval;
    @Getter protected WebDriverWait webDriverWait;

    public WaitStrategy() {
    }

    public WaitStrategy(long timeoutInterval, long sleepInterval) {
        this.timeoutInterval = timeoutInterval;
        this.sleepInterval = sleepInterval;
    }

    public abstract void waitUntil(SearchContext searchContext, By by);

    protected void waitUntil(Function<SearchContext, Boolean> waitCondition) {
        webDriverWait = new WebDriverWait(DriverService.getWrappedDriver(), Duration.ofSeconds(timeoutInterval), Duration.ofSeconds(sleepInterval));
        webDriverWait.withMessage(Thread.currentThread().getStackTrace()[2].getMethodName());
        webDriverWait.until(waitCondition);
    }

    protected WebElement findElement(SearchContext searchContext, By by) {
        var element = searchContext.findElement(by);
        return element;
    }
}
