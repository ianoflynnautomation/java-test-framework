package solutions.bjjeire.cucumber.steps.common;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.selenium.web.data.TestDataManager;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationSteps {

    private final TestDataManager testDataManager;
    private final ScenarioContext scenarioContext;

    @Given("I am a user of the BJJ app")
    public void i_am_an_authenticated_user() {
        log.debug("Authenticating user and storing token in scenario context...");
        String authToken = testDataManager.authenticate();
        scenarioContext.setAuthToken(authToken);
        log.debug("Authentication successful. Token stored in context.");
    }
}