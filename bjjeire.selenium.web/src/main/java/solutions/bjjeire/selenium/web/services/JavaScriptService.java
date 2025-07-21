package solutions.bjjeire.selenium.web.services;

import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;

@Service
public class JavaScriptService extends WebService {

    @Autowired
    public JavaScriptService(DriverService driverService) {
        super(driverService);
    }

    public Object execute(String script, Object... args) {
        // FIX: Get the driver for the current thread and cast it here.
        WebDriver driver = getWrappedDriver();
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }


    public Object executeAsync(String script, Object... args) {
        // FIX: Get the driver for the current thread and cast it here.
        WebDriver driver = getWrappedDriver();
        return ((JavascriptExecutor) driver).executeAsyncScript(script, args);
    }
}