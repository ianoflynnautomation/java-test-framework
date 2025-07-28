package solutions.bjjeire.cucumber.steps.events;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import solutions.bjjeire.api.actions.EventApiActions;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.api.validation.ResponseAsserter;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;
import solutions.bjjeire.cucumber.context.ScenarioContext;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class EventCreateSteps {
    private static final Logger logger = LoggerFactory.getLogger(EventCreateSteps.class);
    @Autowired private ApplicationContext applicationContext;
    @Autowired private ScenarioContext context;
    private final EventApiActions eventApi = new EventApiActions();
    private TestClient testClient() { return applicationContext.getBean(TestClient.class); }

    @Given("I have valid details for a new BJJ event named {string}")
    public void iHaveValidDetailsForANewBjjEventNamed(String eventName) {
        BjjEvent event = BjjEventFactory.createBjjEvent(builder -> builder.name(eventName));
        context.setRequestPayload(event);
        context.setEventName(eventName);
    }

    @Given("I have a BJJ event with invalid {string}")
    public void iHaveABjjEventWithInvalid(String invalidField) {
        Map<String, Object> invalidEventPayload = BjjEventFactory.createInvalidEvent(invalidField);
        context.setRequestPayload(Map.of("data", invalidEventPayload));
    }


    @Given("a BJJ event exists with the name {string} in county {string}")
    public void aBjjEventExistsWithTheNameInCounty(String eventName, String county) {
        BjjEvent eventToCreate = BjjEventFactory.createBjjEvent(b -> b.name(eventName).county(County.valueOf(county.replace(" ", ""))));
        var result = eventApi.createEvent(testClient(), context.getAuthToken(), eventToCreate);
        context.setCreatedEvent(result.resource());
        context.addCleanupAction(result.cleanupAction());
        context.setEventName(result.resource().name());
    }

    @When("I create the BJJ event")
    public void iCreateTheBjjEvent() {
        BjjEvent eventPayload = (BjjEvent) context.getRequestPayload();
        CreateBjjEventCommand command = new CreateBjjEventCommand(eventPayload);

        ResponseAsserter asserter = testClient()
                .withAuthToken(context.getAuthToken())
                .body(command)
                .post("/api/bjjevent");
        context.setResponseAsserter(asserter);
    }


    @When("I attempt to create the BJJ event")
    public void iAttemptToCreateTheBjjEvent() {
        // This step now correctly handles invalid map payloads
        ResponseAsserter asserter = testClient()
                .withAuthToken(context.getAuthToken())
                .body(context.getRequestPayload())
                .post("/api/bjjevent");
        context.setResponseAsserter(asserter);
    }

    @Then("the event is created successfully")
    public void theEventIsCreatedSuccessfully() {
        ResponseAsserter asserter = context.getResponseAsserter();
        assertNotNull(asserter, "ResponseAsserter was not found in the context.");

        asserter.hasStatusCode(201);
        BjjEvent createdEvent = asserter.as(CreateBjjEventResponse.class).data();
        context.setCreatedEvent(createdEvent);

        final String eventId = createdEvent.id();
        context.addCleanupAction(client -> {
            logger.info("CLEANUP: Deleting event with ID: {}", eventId);
            client.withAuthToken(context.getAuthToken())
                    .delete("/api/bjjevent/" + eventId)
                    .then().hasStatusCode(204);
        });
    }


    @Then("the API returns a bad request error with message {string}")
    public void theApiReturnsABadRequestErrorMessage(String expectedErrorMessage) {
        ResponseAsserter asserter = context.getResponseAsserter();
        assertNotNull(asserter, "ResponseAsserter not found in context.");

        asserter.hasStatusCode(400);
        String responseBody = asserter.getResponse().responseBodyAsString();
        assertTrue(responseBody.contains(expectedErrorMessage), "Expected error message not found.");
    }

}