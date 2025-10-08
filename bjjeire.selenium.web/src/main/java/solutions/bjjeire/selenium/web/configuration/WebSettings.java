package solutions.bjjeire.selenium.web.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import solutions.bjjeire.core.plugins.Browser;

@ConfigurationProperties(prefix = "web-settings")
@Data
@Validated
public class WebSettings {

  @NotBlank(message = "Base URL cannot be blank.")
  @URL(message = "Base URL must be a valid URL.")
  private String baseUrl;

  @NotBlank(message = "Execution type cannot be blank.")
  private String executionType;

  @NotBlank(message = "Default browser cannot be blank.")
  private String defaultBrowser;

  private String defaultLifeCycle;

  @PositiveOrZero(message = "Browser width must be a non-negative number.")
  private Integer defaultBrowserWidth = 0;

  @PositiveOrZero(message = "Browser height must be a non-negative number.")
  private Integer defaultBrowserHeight = 0;

  @Valid private List<GridSettings> gridSettings;

  @PositiveOrZero(message = "Artificial delay must be a non-negative number.")
  private int artificialDelayBeforeAction;

  @NotNull(message = "Timeout settings must be configured.")
  @Valid
  private TimeoutSettings timeoutSettings;

  private boolean automaticallyScrollToVisible;
  private boolean waitUntilReadyOnElementFound;

  private boolean screenshotsOnFailEnabled;
  private String screenshotsSaveLocation;

  private boolean videosOnFailEnabled;
  private String videosSaveLocation;

  public Browser getDefaultBrowserEnum() {
    return Browser.fromText(this.defaultBrowser);
  }
}
