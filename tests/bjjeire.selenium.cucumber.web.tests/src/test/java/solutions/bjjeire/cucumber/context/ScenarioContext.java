package solutions.bjjeire.cucumber.context;

import io.cucumber.spring.CucumberTestContext;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import solutions.bjjeire.cucumber.configuration.CucumberSpringConfiguration;

@Setter
@Getter
@Component
@Scope(CucumberTestContext.SCOPE_CUCUMBER_GLUE)
public class ScenarioContext {
    private WebDriver driver;
    private String authToken;
}
