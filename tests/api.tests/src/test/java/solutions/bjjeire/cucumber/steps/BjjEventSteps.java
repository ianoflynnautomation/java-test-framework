package solutions.bjjeire.cucumber.steps;

import Data.*;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import solutions.bjjeire.api.configuration.CucumberTestConfiguration;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.api.validation.ResponseAsserter;
import solutions.bjjeire.cucumber.context.ScenarioContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for BJJ event-related features.
 * This class is a Spring-managed bean, and its dependencies are injected by the Spring container.
 * The @CucumberContextConfiguration annotation tells Cucumber to use Spring.
 * The @ContextConfiguration annotation specifies which Spring configuration class to use.
 */
@CucumberContextConfiguration
@ContextConfiguration(classes = CucumberTestConfiguration.class)
public class BjjEventSteps {

    private static final Logger logger = LoggerFactory.getLogger(BjjEventSteps.class);

    @Autowired
    private ScenarioContext context;
    @Autowired
    private TestClient testClient;

    // A public, no-argument constructor is required for Spring to instantiate this bean.
    public BjjEventSteps() {}

    @Given("I am an authenticated admin user")
    public void i_am_an_authenticated_admin_user() {
        logger.info("Authenticating as an admin user...");
        ResponseAsserter<GenerateTokenResponse> tokenAsserter = testClient
                .withQueryParams(Map.of("userId", "dev-user@example.com", "role", "Admin"))
                .get("/generate-token", GenerateTokenResponse.class)
                .then()
                .hasStatusCode(200);

        String token = tokenAsserter.getData()
                .map(GenerateTokenResponse::token)
                .orElseThrow(() -> new IllegalStateException("Token could not be retrieved from auth response."));

        context.setAuthToken(token);
        assertNotNull(context.getAuthToken(), "Dynamically fetched auth token should not be null");
        assertFalse(context.getAuthToken().isBlank(), "Dynamically fetched auth token should not be blank");
        logger.info("Successfully authenticated and stored auth token.");
    }

    @Given("the API service is available")
    public void the_api_service_is_available() {
        String baseUrl = testClient.getSettings().getBaseUrl();
        logger.info("Precondition: API service is assumed to be available at {}", baseUrl);
        assertNotNull(baseUrl, "Base URL should be configured and available through the TestClient.");
    }

    @Given("I have the details for a new event named {string}")
    public void i_have_the_details_for_a_new_event_named(String eventName) {
        logger.debug("Creating a valid command payload for event: {}", eventName);
        CreateBjjEventCommand command = BjjEventFactory.getValidBjjEventCommand();
        context.setRequestPayload(command);
        context.setEventName(eventName);
        assertNotNull(context.getRequestPayload(), "Request payload should be set in the context.");
    }

    @Given("a BJJ event already exists with the name {string}")
    public void a_bjj_event_already_exists_with_the_name(String eventName) {
        logger.info("Precondition: Ensuring event '{}' exists...", eventName);
        CreateBjjEventCommand command = BjjEventFactory.getValidBjjEventCommand();
        try {
            testClient
                    .withAuthToken(context.getAuthToken())
                    .body(command)
                    .post("/api/bjjevent", Void.class);
            logger.debug("Pre-creation request for event '{}' sent.", eventName);
        } catch (Exception e) {
            logger.warn("Could not pre-create event, assuming it exists. Error: {}", e.getMessage());
        }
        context.setEventName(eventName);
    }

    @Given("I have a BJJ event payload that is invalid because of {string}")
    public void i_have_a_bjj_event_payload_that_is_invalid_because_of(String invalidReason) {
        logger.debug("Creating an invalid payload for reason: {}", invalidReason);
        Map<String, Object> invalidPayload = BjjEventFactory.createInvalidEvent(invalidReason);
        context.setRequestPayload(invalidPayload);
        assertNotNull(context.getRequestPayload(), "Invalid request payload should be set in the context.");
    }

    @When("I create the new BJJ event")
    public void i_create_the_new_bjj_event() {
        logger.info("Executing POST request to create a new BJJ event.");
        ResponseAsserter<CreateBjjEventResponse> asserter = testClient
                .withAuthToken(context.getAuthToken())
                .body(context.getRequestPayload())
                .post("/api/bjjevent", CreateBjjEventResponse.class);
        context.setResponseAsserter(asserter);
    }

    @When("I attempt to create the event")
    public void i_attempt_to_create_the_event() {
        logger.info("Executing POST request to attempt event creation (expecting failure).");
        i_create_the_new_bjj_event();
    }

    @When("I request the details for the {string} event")
    public void i_request_the_details_for_the_event(String eventName) {
        String urlSafeName = eventName.replace(" ", "-").toLowerCase();
        logger.info("Executing GET request for event: {}", urlSafeName);
        ResponseAsserter<CreateBjjEventResponse> asserter = testClient
                .withAuthToken(context.getAuthToken())
                .get("/api/bjjevent/" + urlSafeName, CreateBjjEventResponse.class);
        context.setResponseAsserter(asserter);
    }

    @Then("the system should confirm the event was created successfully")
    public void the_system_should_confirm_the_event_was_created_successfully() {
        context.getResponseAsserter().hasStatusCode(201);
        logger.debug("Verified status code is 201 Created.");
    }

    @Then("the API should respond with the complete details for that event")
    public void the_api_should_respond_with_the_complete_details_for_that_event() {
        context.getResponseAsserter().hasStatusCode(200);
        logger.debug("Verified status code is 200 OK.");
    }

    @Then("the API should respond with a bad request error")
    public void the_api_should_respond_with_a_bad_request_error() {
        context.getResponseAsserter().hasStatusCode(400);
        logger.debug("Verified status code is 400 Bad Request.");
    }

    @And("the event details in the response should contain:")
    public void the_event_details_in_the_response_should_contain(DataTable dataTable) {
        ResponseAsserter<?> asserter = context.getResponseAsserter();
        assertNotNull(asserter, "Response asserter is not set in context.");
        String responseBody = asserter.getResponseBodyAsString();

        for (Map<String, String> row : dataTable.asMaps(String.class, String.class)) {
            String path = row.get("path");
            String expectedValue = row.get("value");
            logger.debug("Validating JSONPath '{}' has value '{}'", path, expectedValue);
            Object actualValue = JsonPath.read(responseBody, "$." + path);
            assertEquals(expectedValue, String.valueOf(actualValue), "Validation failed for JSONPath: " + path);
        }
    }

    @And("the event's name in the response should be {string}")
    public void the_event_s_name_in_the_response_should_be(String expectedName) {
        context.getResponseAsserter().bodySatisfies(body -> {
            String actualName = JsonPath.read(body, "$.data.name");
            assertEquals(expectedName, actualName, "Event name in response did not match expected value.");
        });
    }

    @And("the error message should be {string}")
    public void the_error_message_should_be(String expectedError) {
        context.getResponseAsserter().bodySatisfies(body -> {
            String actualError = JsonPath.read(body, "$.error");
            assertEquals(expectedError, actualError, "Error message in response did not match expected value.");
        });
    }
}
