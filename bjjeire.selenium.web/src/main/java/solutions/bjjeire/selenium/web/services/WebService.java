package solutions.bjjeire.selenium.web.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.openqa.selenium.WebDriver;

@Slf4j
@RequiredArgsConstructor
public abstract class WebService {

  protected final DriverService driverService;

  public WebDriver getWrappedDriver() {
    WebDriver driver = driverService.getWrappedDriver();
    if (driver == null) {

      log.error(
          "WebDriver instance is null",
          StructuredArguments.keyValue("reason", "Critical failure in browser startup lifecycle"),
          StructuredArguments.keyValue(
              "suggestion",
              "Ensure the BrowserLifecyclePlugin has started the browser for the current thread."));

      throw new IllegalStateException(
          "WebDriver instance is null. Ensure the BrowserLifecyclePlugin has started the browser for the current thread.");
    }
    return driver;
  }
}
