package solutions.bjjeire.cucumber.steps.events;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.actions.EventApiActions;
import solutions.bjjeire.api.validation.ValidatableResponse;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.cucumber.steps.CucumberTestBase;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class EventCreateSteps extends CucumberTestBase {
    private static final Logger logger = LoggerFactory.getLogger(EventCreateSteps.class);

    @Autowired private ScenarioContext context;
    @Autowired private EventApiActions eventApi;

    @Given("I have valid details for a new BJJ event named {string}")
    public void iHaveValidDetailsForANewBjjEventNamed(String eventName) {
        BjjEvent event = BjjEventFactory.createBjjEvent(builder -> builder.name(eventName));
        context.setRequestPayload(new CreateBjjEventCommand(event));
        context.setEventName(eventName);
    }

    @Given("I have a BJJ event with invalid {string}")
    public void iHaveABjjEventWithInvalid(String invalidField) {
        Map<String, Object> invalidEventPayload = BjjEventFactory.createInvalidEvent(invalidField);
        context.setRequestPayload(invalidEventPayload);
    }


    @Given("a BJJ event exists with the name {string} in county {string}")
    public void aBjjEventExistsWithTheNameInCounty(String eventName, String county) {
        BjjEvent eventToCreate = BjjEventFactory.createBjjEvent(b -> b.name(eventName).county(County.valueOf(county.replace(" ", ""))));
        var result = eventApi.createEvent(context.getAuthToken(), eventToCreate);
        context.setCreatedEvent(result.resource());
        context.addCleanupAction(result.cleanupAction());
        context.setEventName(result.resource().name());
    }

    @When("I create the BJJ event")
    public void iCreateTheBjjEvent() {
        ValidatableResponse response = given()
                .withAuthToken(context.getAuthToken())
                .withBody(context.getRequestPayload())
                .post("/api/bjjevent");
        context.setValidatableResponse(response);
    }

    @When("I attempt to create the BJJ event")
    public void iAttemptToCreateTheBjjEvent() {
        ValidatableResponse response = given()
                .withAuthToken(context.getAuthToken())
                .withBody(context.getRequestPayload())
                .post("/api/bjjevent");
        context.setValidatableResponse(response);
    }

    @Then("the event is created successfully")
    public void theEventIsCreatedSuccessfully() {
        ValidatableResponse response = context.getValidatableResponse();
        assertNotNull(response, "ValidatableResponse was not found in the context.");

        response.hasStatusCode(201);
        BjjEvent createdEvent = response.as(CreateBjjEventResponse.class).data();
        context.setCreatedEvent(createdEvent);

        final String eventId = createdEvent.id();
        context.addCleanupAction(() -> {
            logger.info("CLEANUP: Deleting event with ID: {}", eventId);
            given().withAuthToken(context.getAuthToken())
                    .delete("/api/bjjevent/" + eventId)
                    .then().hasStatusCode(204);
        });
    }

    @Then("the API returns a bad request error with message {string}")
    public void theApiReturnsABadRequestErrorMessage(String expectedErrorMessage) {
        ValidatableResponse response = context.getValidatableResponse();
        assertNotNull(response, "ValidatableResponse not found in context.");

        response.hasStatusCode(400).contentContains(expectedErrorMessage);
    }
}