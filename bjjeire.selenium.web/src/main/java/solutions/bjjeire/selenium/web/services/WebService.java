

package solutions.bjjeire.selenium.web.services;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;

/**
 * Abstract base class for all web-related services.
 * It provides a consistent way to access the WebDriver instance for the current thread.
 */
public abstract class WebService {
    private static final Logger log = LoggerFactory.getLogger(WebService.class);
    protected final DriverService driverService;

    @Autowired
    public WebService(DriverService driverService) {
        this.driverService = driverService;
    }

    public WebDriver getWrappedDriver() {
        WebDriver driver = driverService.getWrappedDriver();
        if (driver == null) {
            log.error("WebDriver instance is null. This indicates a critical failure in the browser startup lifecycle.");
            throw new IllegalStateException("WebDriver instance is null. Ensure the BrowserLifecyclePlugin has started the browser for the current thread.");
        }
        return driver;
    }
}
