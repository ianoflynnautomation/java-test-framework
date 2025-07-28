package solutions.bjjeire.cucumber.steps.common;


import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import solutions.bjjeire.api.actions.AuthApiActions;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.cucumber.context.ScenarioContext;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationSteps {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationSteps.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ScenarioContext context;

    private final AuthApiActions authApi = new AuthApiActions();

    private TestClient testClient() {
        return applicationContext.getBean(TestClient.class);
    }

    @Given("I am authenticated as an admin user")
    public void iAmAuthenticatedAsAnAdminUser() {
        logger.info("Authenticating as an admin user...");
        context.setAuthToken(authApi.authenticateAsAdmin(testClient()));
        assertNotNull(context.getAuthToken());
        assertFalse(context.getAuthToken().isBlank());
        logger.info("Successfully authenticated.");
    }
}