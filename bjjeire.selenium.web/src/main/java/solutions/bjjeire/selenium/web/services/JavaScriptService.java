package solutions.bjjeire.selenium.web.services;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

@Service
public class JavaScriptService extends WebService {

    public JavaScriptService(DriverService driverService) {
        super(driverService);
    }

    public Object execute(String script, Object... args) {
        WebDriver driver = getWrappedDriver();
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    public Object executeAsync(String script, Object... args) {
        WebDriver driver = getWrappedDriver();
        return ((JavascriptExecutor) driver).executeAsyncScript(script, args);
    }
}