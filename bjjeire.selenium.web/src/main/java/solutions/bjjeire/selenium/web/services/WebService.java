package solutions.bjjeire.selenium.web.services;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.logstash.logback.argument.StructuredArguments;

public abstract class WebService {
    private static final Logger log = LoggerFactory.getLogger(WebService.class);

    protected final DriverService driverService;

    public WebService(DriverService driverService) {
        this.driverService = driverService;
    }

    public WebDriver getWrappedDriver() {
        WebDriver driver = driverService.getWrappedDriver();
        if (driver == null) {

            log.error("WebDriver instance is null",
                    StructuredArguments.keyValue("reason", "Critical failure in browser startup lifecycle"),
                    StructuredArguments.keyValue("suggestion",
                            "Ensure the BrowserLifecyclePlugin has started the browser for the current thread."));

            throw new IllegalStateException(
                    "WebDriver instance is null. Ensure the BrowserLifecyclePlugin has started the browser for the current thread.");
        }
        return driver;
    }
}