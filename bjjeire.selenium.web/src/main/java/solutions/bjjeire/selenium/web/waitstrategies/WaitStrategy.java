package solutions.bjjeire.selenium.web.waitstrategies;

import java.time.Duration;
import java.util.function.Function;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import solutions.bjjeire.selenium.web.services.DriverService;

public abstract class WaitStrategy {
  @Getter protected long timeoutInterval;
  @Getter protected long sleepInterval;

  public WaitStrategy() {}

  public WaitStrategy(long timeoutInterval, long sleepInterval) {
    this.timeoutInterval = timeoutInterval;
    this.sleepInterval = sleepInterval;
  }

  public abstract void waitUntil(DriverService driverService, SearchContext searchContext, By by);

  protected void waitUntil(
      DriverService driverService, Function<WebDriver, Boolean> waitCondition) {
    WebDriverWait webDriverWait =
        new WebDriverWait(
            driverService.getWrappedDriver(),
            Duration.ofSeconds(timeoutInterval),
            Duration.ofSeconds(sleepInterval));
    webDriverWait.withMessage(Thread.currentThread().getStackTrace()[2].getMethodName());
    webDriverWait.until(waitCondition);
  }

  protected WebElement findElement(SearchContext searchContext, By by) {
    return searchContext.findElement(by);
  }
}
