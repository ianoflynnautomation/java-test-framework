package solutions.bjjeire.cucumber.steps.common;

import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.actions.AuthApiActions;
import solutions.bjjeire.cucumber.context.TestState;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationSteps {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationSteps.class);

    @Autowired
    private TestState testState;
    @Autowired
    private AuthApiActions authApi;

    @Given("Admin is authenticated")
    public void adminAndyIsAuthenticated() {
        logger.info("Authenticating Admin Andy as an admin user...");
        String token = authApi.authenticateAsAdmin();
        testState.setAuthToken(token);

        assertNotNull(testState.getAuthToken(), "Authentication token should not be null.");
        assertFalse(testState.getAuthToken().isBlank(), "Authentication token should not be blank.");
        logger.info("Admin Andy successfully authenticated.");
    }
}