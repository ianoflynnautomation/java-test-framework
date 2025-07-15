package solution.bjjeire.selenium.web.services;

import org.openqa.selenium.WebDriver;
import solution.bjjeire.selenium.web.infrastructure.DriverService;

public abstract class WebService {
    public WebDriver getWrappedDriver() {
        return DriverService.getWrappedDriver();
    }
}
