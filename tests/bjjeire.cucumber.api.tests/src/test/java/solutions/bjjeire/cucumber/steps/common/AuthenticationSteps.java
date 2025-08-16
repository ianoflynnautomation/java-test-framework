package solutions.bjjeire.cucumber.steps.common;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.services.AuthService;
import solutions.bjjeire.cucumber.context.TestContext;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationSteps {

    private final TestContext testState;
    private final AuthService authService;

    @Given("Admin is authenticated")
    public void adminAndyIsAuthenticated() {
        String token = authService.authenticateAsAdmin();
        testState.setAuthToken(token);
        assertNotNull(testState.getAuthToken(), "Authentication token should not be null.");
        assertFalse(testState.getAuthToken().isBlank(), "Authentication token should not be blank.");
    }
}