package solutions.bjjeire.cucumber.context;

import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@Scope("cucumber-glue")
public class ScenarioContext {
    private WebDriver driver;
    private String authToken;
}
