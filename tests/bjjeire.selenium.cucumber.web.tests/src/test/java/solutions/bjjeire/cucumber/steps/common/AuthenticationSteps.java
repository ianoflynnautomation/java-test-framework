package solutions.bjjeire.cucumber.steps;

import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.cucumber.context.BaseContext;
import solutions.bjjeire.selenium.web.data.TestDataManager;

public class AuthenticationSteps {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationSteps.class);

    @Autowired private TestDataManager testDataManager;
    @Autowired private BaseContext baseContext;

    @Given("I am an authenticated user")
    public void i_am_an_authenticated_user() {
        log.info("Authenticating user and storing token in scenario context...");
        String authToken = testDataManager.authenticate();
        baseContext.setAuthToken(authToken);
        log.debug("Authentication successful. Token stored.");
    }
}