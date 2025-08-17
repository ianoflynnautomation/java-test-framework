package solutions.bjjeire.cucumber.steps.common;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        String token = authService.getTokenFor("admin").block();
        testState.setAuthToken(token);
        assertNotNull(testState.getAuthToken(), "Authentication token should not be null.");
        assertFalse(testState.getAuthToken().isBlank(), "Authentication token should not be blank.");
    }
}