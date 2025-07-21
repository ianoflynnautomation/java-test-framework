

package solutions.bjjeire.selenium.web.services;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;

/**
 * Abstract base class for all service classes.
 * It provides access to the WebDriver instance via constructor injection.
 */
public abstract class WebService {

    protected final DriverService driverService;

    @Autowired
    public WebService(DriverService driverService) {
        this.driverService = driverService;
    }


    public WebDriver getWrappedDriver() {
        WebDriver driver = driverService.getWrappedDriver();
        if (driver == null) {
            throw new IllegalStateException("WebDriver instance is null. Ensure the BrowserLifecyclePlugin has started the browser for the current thread.");
        }
        return driver;
    }
}