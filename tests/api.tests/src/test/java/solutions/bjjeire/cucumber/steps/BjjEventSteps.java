package solutions.bjjeire.cucumber.steps;

import Data.*;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.api.validation.ResponseAsserter;
import solutions.bjjeire.cucumber.context.ScenarioContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BjjEventSteps {

    private final ScenarioContext context;
    private final TestClient testClient;
    private final ApiSettings settings;

    /**
     * Constructor for dependency injection by PicoContainer.
     * @param context The shared context for the scenario.
     * @param testClient The fluent client for making API calls.
     * @param settings The application configuration.
     */
    public BjjEventSteps(ScenarioContext context, TestClient testClient, ApiSettings settings) {
        this.context = context;
        this.testClient = testClient;
        this.settings = settings;
    }

    // --- GIVEN Steps ---

    @Given("I am an authenticated admin user")
    public void i_am_an_authenticated_admin_user() {
        // Dynamically fetch the token from the /generate-token endpoint
        ResponseAsserter<GenerateTokenResponse> tokenAsserter = testClient
                .withQueryParams(Map.of(
                        "userId", "dev-user@example.com",
                        "role", "Admin"
                ))
                .get("/generate-token", GenerateTokenResponse.class)
                .then()
                .hasStatusCode(200);

        // Extract the token from the response and store it in the scenario context
        String token = tokenAsserter.getData()
                .map(GenerateTokenResponse::token)
                .orElseThrow(() -> new IllegalStateException("Token could not be retrieved from auth response."));

        context.setAuthToken(token);
        assertNotNull(context.getAuthToken(), "Dynamically fetched auth token should not be null");
        assertFalse(context.getAuthToken().isBlank(), "Dynamically fetched auth token should not be blank");
    }

    @Given("the API service is available")
    public void the_api_service_is_available() {
        // This could be a ping to a /health endpoint in a real scenario.
        System.out.println("Precondition: API service is assumed to be available at " + settings.getBaseUrl());
    }

    @Given("I have the details for a new event named {string}")
    public void i_have_the_details_for_a_new_event_named(String eventName) {
        // Create the core event data DTO
        CreateBjjEventCommand command = BjjEventFactory.getValidBjjEventCommand();
        context.setRequestPayload(command);
        context.setEventName(eventName);
        assertNotNull(context.getRequestPayload());
    }

    @Given("a BJJ event already exists with the name {string}")
    public void a_bjj_event_already_exists_with_the_name(String eventName) {
        System.out.println("Precondition: Ensuring event '" + eventName + "' exists...");
        CreateBjjEventCommand command = BjjEventFactory.getValidBjjEventCommand();
        try {
            testClient
                    .withAuthToken(context.getAuthToken()) // Use the token from the context
                    .body(command) // Send the command object
                    .post("/api/bjjevents", Void.class); // We don't need to inspect the response here
        } catch (Exception e) {
            // Ignore failures (e.g., 409 Conflict), as the desired state is that the event exists.
            System.out.println("Could not pre-create event, assuming it exists. Error: " + e.getMessage());
        }
        context.setEventName(eventName);
    }

    @Given("I have a BJJ event payload that is invalid because of {string}")
    public void i_have_a_bjj_event_payload_that_is_invalid_because_of(String invalidReason) {
        // The factory now creates a Map that represents an invalid command structure
        Map<String, Object> invalidPayload = BjjEventFactory.createInvalidEvent(invalidReason);
        context.setRequestPayload(invalidPayload);
        assertNotNull(context.getRequestPayload());
    }

    // --- WHEN Steps ---

    @When("I create the new BJJ event")
    public void i_create_the_new_bjj_event() {
        // The API returns a CreateBjjEventResponse, so we set that as the expected type.
        ResponseAsserter<CreateBjjEventResponse> asserter = testClient
                .withAuthToken(context.getAuthToken())
                .body(context.getRequestPayload())
                .post("/api/bjjevents", CreateBjjEventResponse.class);
        context.setResponseAsserter(asserter);
    }

    @When("I attempt to create the event")
    public void i_attempt_to_create_the_event() {
        // This step is functionally identical to the one above
        i_create_the_new_bjj_event();
    }

    @When("I request the details for the {string} event")
    public void i_request_the_details_for_the_event(String eventName) {
        String urlSafeName = eventName.replace(" ", "-").toLowerCase();
        // Assuming the GET endpoint returns the response in the same wrapper for consistency
        ResponseAsserter<CreateBjjEventResponse> asserter = testClient
                .withAuthToken(context.getAuthToken())
                .get("/api/bjjevents/name/" + urlSafeName, CreateBjjEventResponse.class);
        context.setResponseAsserter(asserter);
    }

    // --- THEN Steps ---

    @Then("the system should confirm the event was created successfully")
    public void the_system_should_confirm_the_event_was_created_successfully() {
        context.getResponseAsserter().hasStatusCode(201);
    }

    @Then("the API should respond with the complete details for that event")
    public void the_api_should_respond_with_the_complete_details_for_that_event() {
        context.getResponseAsserter().hasStatusCode(200);
    }

    @Then("the API should respond with a bad request error")
    public void the_api_should_respond_with_a_bad_request_error() {
        context.getResponseAsserter().hasStatusCode(400);
    }

    @And("the event details in the response should contain:")
    public void the_event_details_in_the_response_should_contain(DataTable dataTable) {
        ResponseAsserter<?> asserter = context.getResponseAsserter();
        assertNotNull(asserter, "Response asserter is not set in context.");

        String responseBody = asserter.getResponseBodyAsString();

        for (Map<String, String> row : dataTable.asMaps(String.class, String.class)) {
            String path = row.get("path");
            String expectedValue = row.get("value");
            // The JSONPath is now evaluated against the response JSON, which has a root 'data' object.
            // The path from the feature file (e.g., "data.name") correctly maps to the JSON structure.
            Object actualValue = JsonPath.read(responseBody, "$." + path);
            assertEquals(expectedValue, String.valueOf(actualValue), "Validation failed for JSONPath: " + path);
        }
    }

    @And("the event's name in the response should be {string}")
    public void the_event_s_name_in_the_response_should_be(String expectedName) {
        context.getResponseAsserter().bodySatisfies(body -> {
            // The JSONPath must now look inside the 'data' wrapper of the response.
            String actualName = JsonPath.read(body, "$.data.name");
            assertEquals(expectedName, actualName);
        });
    }

    @And("the error message should be {string}")
    public void the_error_message_should_be(String expectedError) {
        context.getResponseAsserter().bodySatisfies(body -> {
            // Error responses likely do not follow the CQRS wrapper, so this path remains the same.
            String actualError = JsonPath.read(body, "$.error");
            assertEquals(expectedError, actualError);
        });
    }
}
