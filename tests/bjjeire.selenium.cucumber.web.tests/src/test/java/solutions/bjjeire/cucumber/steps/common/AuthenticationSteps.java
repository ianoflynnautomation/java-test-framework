package solutions.bjjeire.cucumber.steps.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.java.en.Given;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.selenium.web.data.TestDataManager;

public class AuthenticationSteps {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationSteps.class);

    private final TestDataManager testDataManager;
    private final ScenarioContext scenarioContext;

    public AuthenticationSteps(TestDataManager testDataManager, ScenarioContext scenarioContext) {
        this.testDataManager = testDataManager;
        this.scenarioContext = scenarioContext;
    }

    @Given("I am a user of the BJJ app")
    public void i_am_an_authenticated_user() {
        log.info("Authenticating user and storing token in scenario context...");
        String authToken = testDataManager.authenticate();
        scenarioContext.setAuthToken(authToken);
        log.debug("Authentication successful. Token stored.");
    }
}