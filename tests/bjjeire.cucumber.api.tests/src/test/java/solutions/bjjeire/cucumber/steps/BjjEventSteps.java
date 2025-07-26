package solutions.bjjeire.cucumber.steps;

import com.github.javafaker.Faker;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.configuration.TestConfiguration;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.api.validation.ResponseAsserter;
import solutions.bjjeire.core.data.events.*;
import solutions.bjjeire.cucumber.context.ScenarioContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for the BJJ Event Management feature.
 * This class uses @SpringBootTest to load the full application context, ensuring
 * that @ConfigurationProperties and other features work correctly.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = TestConfiguration.class)
public class BjjEventSteps {

    private static final Logger logger = LoggerFactory.getLogger(BjjEventSteps.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ScenarioContext context;
    @Autowired
    private ApiSettings settings;
    @Autowired
    private Faker faker;

    private TestClient testClient() {
        return applicationContext.getBean(TestClient.class);
    }

    @Given("I am authenticated as an admin user")
    public void iAmAuthenticatedAsAnAdminUser() {
        logger.info("Authenticating as an admin user...");
        GenerateTokenResponse tokenResponse = testClient()
                .withQueryParams(Map.of("userId", "dev-user@example.com", "role", "Admin"))
                .get("/generate-token")
                .then()
                .hasStatusCode(200)
                .as(GenerateTokenResponse.class);
        context.setAuthToken(tokenResponse.token());
        assertNotNull(context.getAuthToken());
        assertFalse(context.getAuthToken().isBlank());
        logger.info("Successfully authenticated and stored auth token.");
    }

    @Given("the BJJ event API is available")
    public void theBjjEventApiIsAvailable() {
        logger.info("Precondition: API service is assumed to be available at {}", settings.getBaseUrl());
        assertNotNull(settings.getBaseUrl());
    }

    @Given("I have valid details for a new BJJ event named {string}")
    public void iHaveValidDetailsForANewBjjEventNamed(String eventName) {
        logger.debug("Creating a valid command payload for event: {}", eventName);
        // The factory now creates a unique name, but we can override it for specific scenarios.
        BjjEvent event = BjjEventFactory.createBjjEvent(builder -> builder.name(eventName));
        CreateBjjEventCommand command = new CreateBjjEventCommand(event);
        context.setRequestPayload(command);
        context.setEventName(eventName);
        context.setCreatedEvent(null); // Clear context from previous runs
        assertNotNull(context.getRequestPayload());
    }

    @Given("a BJJ event exists with the name {string} in county {string}")
    public void aBjjEventExistsWithTheNameInCounty(String eventName, String county) {
        logger.info("Precondition: Creating event with specific name '{}' in county '{}' to ensure it exists...", eventName, county);
        if (context.getAuthToken() == null) {
            iAmAuthenticatedAsAnAdminUser();
        }
        BjjEvent eventToCreate = BjjEventFactory.createBjjEvent(builder -> builder
                .name(eventName) // Use the specific name from the feature file
                .county(County.valueOf(county.replace(" ", "")))
        );
        CreateBjjEventCommand command = new CreateBjjEventCommand(eventToCreate);
        CreateBjjEventResponse creationResponse = testClient()
                .withAuthToken(context.getAuthToken())
                .body(command)
                .post("/api/bjjevent")
                .then()
                .hasStatusCode(201)
                .as(CreateBjjEventResponse.class);

        context.setCreatedEvent(creationResponse.data());
        context.setEventName(creationResponse.data().name()); // Store the exact name
        logger.info("Successfully created event '{}' with ID '{}' for test.", creationResponse.data().name(), creationResponse.data().id());
        assertNotNull(context.getCreatedEvent());
    }

    @Given("I have a BJJ event with invalid {string}")
    public void iHaveABjjEventWithInvalid(String invalidField) {
        logger.debug("Creating an invalid payload by making field '{}' invalid", invalidField);
        Map<String, Object> invalidEventPayload = BjjEventFactory.createInvalidEvent(invalidField);
        Map<String, Object> commandPayload = Map.of("data", invalidEventPayload);
        context.setRequestPayload(commandPayload);
        assertNotNull(context.getRequestPayload());
    }

    @When("I create the BJJ event")
    public void iCreateTheBjjEvent() {
        logger.info("Executing POST request to create a new BJJ event.");
        ResponseAsserter asserter = testClient()
                .withAuthToken(context.getAuthToken())
                .body(context.getRequestPayload())
                .post("/api/bjjevent");
        context.setResponseAsserter(asserter);
    }

    @When("I attempt to create the BJJ event")
    public void iAttemptToCreateTheBjjEvent() {
        logger.info("Executing POST request to attempt event creation (expecting failure).");
        iCreateTheBjjEvent();
    }

    @When("I retrieve all BJJ events for county {string}")
    public void iRetrieveAllBjjEventsForCounty(String county) {
        logger.info("Executing GET request for events in county: {}", county);
        context.setCreatedEvent(null); // Clear single-event context before making a list request

        String eventName = context.getEventName();
        assertNotNull(eventName, "Event name must be in context to perform a targeted search.");

        logger.info("Performing a targeted search for event name '{}' in county '{}'", eventName, county);
        ResponseAsserter asserter = testClient()
                .withAuthToken(context.getAuthToken())
                .withQueryParams(Map.of(
                        "County", county.replace(" ", ""),
                        "Name", eventName // Add name to query to avoid pagination issues
                ))
                .get("/api/bjjevent");
        context.setResponseAsserter(asserter);
    }

    @Then("the event is created successfully")
    public void theEventIsCreatedSuccessfully() {
        ResponseAsserter asserter = context.getResponseAsserter();
        asserter.hasStatusCode(201);
        logger.debug("Verified status code is 201 Created.");
        CreateBjjEventResponse response = asserter.as(CreateBjjEventResponse.class);
        context.setCreatedEvent(response.data());
        logger.debug("Successfully deserialized and stored the created event in context.");
    }

    @Then("the event details are returned successfully")
    public void theEventDetailsAreReturnedSuccessfully() {
        context.getResponseAsserter().hasStatusCode(200);
        logger.debug("Verified status code is 200 OK.");
    }

    @Then("the API returns a bad request error with message {string}")
    public void theApiReturnsABadRequestErrorMessage(String expectedErrorMessage) {
        ResponseAsserter asserter = context.getResponseAsserter();
        asserter.hasStatusCode(400);
        logger.debug("Verified status code is 400 Bad Request.");
        String responseBody = asserter.getResponse().responseBodyAsString();
        String actualErrorMessage;
        try {
            actualErrorMessage = com.jayway.jsonpath.JsonPath.read(responseBody, "$.errors[0].message");
        } catch (Exception e) {
            logger.warn("Could not find '$.errors[0].message'. Falling back to entire body.", e);
            actualErrorMessage = responseBody;
        }

        final String finalActualErrorMessage = actualErrorMessage;

        assertTrue(finalActualErrorMessage.toLowerCase().contains(expectedErrorMessage.toLowerCase()),
                () -> String.format("Error message mismatch.%nExpected to find: '%s'%nActual message: '%s'", expectedErrorMessage, finalActualErrorMessage));
    }

    @Then("the response contains the event {string}")
    public void theResponseContainsTheEvent(String eventName) {
        ResponseAsserter asserter = context.getResponseAsserter();
        asserter.hasStatusCode(200);
        GetBjjEventPaginatedResponse paginatedResponse = asserter.as(GetBjjEventPaginatedResponse.class);

        assertEquals(1, paginatedResponse.data().size(), "Expected exactly one event with the specified name, but found " + paginatedResponse.data().size());

        boolean eventFound = paginatedResponse.data().stream()
                .anyMatch(event -> event.name().equals(eventName));

        assertTrue(eventFound, "The event with name '" + eventName + "' was not found in the API response.");
        logger.info("Successfully found the unique event '{}' in the list.", eventName);
    }

    @And("the event details include:")
    public void theEventDetailsInclude(DataTable dataTable) {
        ResponseAsserter asserter = context.getResponseAsserter();
        assertNotNull(asserter, "Response asserter is not set in context.");
        String eventName = context.getEventName();
        assertNotNull(eventName, "Event name must be set in the context for validation.");

        BjjEvent eventToValidate;

        if (context.getCreatedEvent() != null) {
            logger.debug("Validating details against the single event stored in the context.");
            eventToValidate = context.getCreatedEvent();
        } else {
            logger.debug("Validating details by finding the event in a list from the response.");
            GetBjjEventPaginatedResponse paginatedResponse = asserter.as(GetBjjEventPaginatedResponse.class);
            eventToValidate = paginatedResponse.data().stream()
                    .filter(event -> event.name().equals(eventName))
                    .findFirst()
                    .orElse(null);
        }

        assertNotNull(eventToValidate, "Could not find event '" + eventName + "' in the response to validate details.");

        for (List<String> row : dataTable.asLists(String.class)) {
            String fieldName = row.get(0);
            String expectedValue = row.get(1);

            switch (fieldName) {
                case "Name":
                    assertEquals(expectedValue, eventToValidate.name(), "Validation failed for Name.");
                    break;
                case "Location":
                    assertEquals(expectedValue, eventToValidate.location().venue(), "Validation failed for Location.");
                    break;
                case "Organiser":
                    assertEquals(expectedValue, eventToValidate.organiser().name(), "Validation failed for Organiser.");
                    break;
                default:
                    fail("Unknown field to validate in DataTable: " + fieldName);
            }
        }
        logger.info("Successfully validated details for event '{}'.", eventName);
    }
}