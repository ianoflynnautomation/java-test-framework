package solutions.bjjeire.selenium.web.configuration;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class TimeoutSettings {

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long pageLoadTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long scriptTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long elementWaitTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long waitForAjaxTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long waitUntilReadyTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long waitForJavaScriptAnimationsTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long waitForAngularTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long waitForPartialUrl;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long sleepInterval;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long validationsTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long elementToBeVisibleTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long elementToExistTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long elementToNotExistTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long elementToBeClickableTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long elementNotToBeVisibleTimeout;

  @PositiveOrZero(message = "Timeout must be a non-negative value.")
  private long elementToHaveContentTimeout;
}
