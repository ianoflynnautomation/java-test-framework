package solutions.bjjeire.cucumber.context;

import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A scenario-scoped container for holding state common to ALL scenarios.
 * Feature-specific contexts will extend this class.
 */
@Component
@Scope("cucumber-glue")
public class BaseContext {
    private WebDriver driver;
    private String authToken;

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
