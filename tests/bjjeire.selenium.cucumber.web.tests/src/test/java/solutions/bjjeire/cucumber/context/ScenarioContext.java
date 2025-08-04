package solutions.bjjeire.cucumber.context;

import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A scenario-scoped container for holding state common to ALL scenarios.
 * Feature-specific contexts will extend this class.
 */
@Setter
@Getter
@Component
@Scope("cucumber-glue")
public class ScenarioContext {
    private WebDriver driver;
    private String authToken;

}
