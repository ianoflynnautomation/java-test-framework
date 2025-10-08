package solutions.bjjeire.selenium.web.waitstrategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import solutions.bjjeire.selenium.web.configuration.WebSettings;

@Component
public class WaitStrategyFactory {

  private final WebSettings webSettings;

  @Autowired
  public WaitStrategyFactory(WebSettings webSettings) {
    this.webSettings = webSettings;
  }

  public ToExistWaitStrategy exist() {
    return new ToExistWaitStrategy(
        webSettings.getTimeoutSettings().getElementToExistTimeout(),
        webSettings.getTimeoutSettings().getSleepInterval());
  }

  public ToExistWaitStrategy exist(long timeoutInterval, long sleepInterval) {
    return new ToExistWaitStrategy(timeoutInterval, sleepInterval);
  }

  public ToBeVisibleWaitStrategy beVisible() {
    return new ToBeVisibleWaitStrategy(
        webSettings.getTimeoutSettings().getElementToBeVisibleTimeout(),
        webSettings.getTimeoutSettings().getSleepInterval());
  }

  public ToBeVisibleWaitStrategy beVisible(long timeoutInterval, long sleepInterval) {
    return new ToBeVisibleWaitStrategy(timeoutInterval, sleepInterval);
  }

  public ToBeClickableWaitStrategy beClickable() {
    return new ToBeClickableWaitStrategy(
        webSettings.getTimeoutSettings().getElementToBeClickableTimeout(),
        webSettings.getTimeoutSettings().getSleepInterval());
  }

  public ToBeClickableWaitStrategy beClickable(long timeoutInterval, long sleepInterval) {
    return new ToBeClickableWaitStrategy(timeoutInterval, sleepInterval);
  }
}
