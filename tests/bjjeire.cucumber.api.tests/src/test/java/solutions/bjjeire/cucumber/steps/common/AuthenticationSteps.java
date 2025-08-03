package solutions.bjjeire.cucumber.steps.common;

import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.services.AuthService;
import solutions.bjjeire.cucumber.context.TestContext;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationSteps {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationSteps.class);

    @Autowired
    private TestContext testState;
    @Autowired
    private AuthService authService;

    @Given("Admin is authenticated")
    public void adminAndyIsAuthenticated() {
        String token = authService.authenticateAsAdmin();
        testState.setAuthToken(token);
        assertNotNull(testState.getAuthToken(), "Authentication token should not be null.");
        assertFalse(testState.getAuthToken().isBlank(), "Authentication token should not be blank.");
    }
}