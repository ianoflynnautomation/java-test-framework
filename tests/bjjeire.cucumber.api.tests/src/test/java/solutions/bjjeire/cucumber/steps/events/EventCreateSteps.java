package solutions.bjjeire.cucumber.steps.events;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.services.EventService;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.api.validation.ResponseValidatorFactory;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;
import solutions.bjjeire.cucumber.context.TestContext;

import java.util.Map;

public class EventCreateSteps {

    @Autowired
    private TestContext testContext;
    @Autowired
    private EventService eventService;
    @Autowired // Inject the ResponseValidatorFactory
    private ResponseValidatorFactory responseValidator;


    @Given("a new event has been prepared")
    public void aNewEventHasBeenPrepared() {
        testContext.setRequestPayload(new CreateBjjEventCommand(BjjEventFactory.getValidBjjEvent()));
    }

    @Given("the Admin has an event with {string} set to {string}")
    public void adminHasABjjEventWithFieldSetToInvalidValue(String field, String invalidValue) {
        Map<String, String> invalidDetails = Map.of(field, invalidValue);
        CreateBjjEventCommand invalidPayload = BjjEventFactory.createPayloadWithInvalidDetails(invalidDetails);
        testContext.setRequestPayload(invalidPayload);
    }

    @When("the Admin adds the new event")
    public void adminAddsTheNewEvent() {
        CreateBjjEventCommand command = (CreateBjjEventCommand) testContext.getRequestPayload();
        ApiResponse response = eventService.createEvent(testContext.getAuthToken(), command).block();
        testContext.setLastResponse(response);

        if (response.getStatusCode() == 201) {
            BjjEvent createdEvent = response.as(CreateBjjEventResponse.class).data();
            testContext.addEntityForCleanup(createdEvent);
        }
    }

    @When("the Admin attempts to add the new event")
    public void adminAttemptsToCreateTheBjjEvent() {
        ApiResponse response = eventService.attemptToCreateEvent(
                testContext.getAuthToken(),
                testContext.getRequestPayload()).block();
        testContext.setLastResponse(response);
    }

    @Then("the event should be successfully added")
    public void theEventShouldBeSuccessfullyAdded() {
        // Use the factory to create a ResponseValidator instance
        responseValidator.validate(testContext.getLastResponse()).statusCode(201);
    }

    @Then("the Admin should be notified that adding the event failed for {string} with message {string}")
    public void adminIsNotifiedThatTheEventCreationFailedForWithMessage(String field, String errorMessage) {
        // Use the factory to create a ResponseValidator instance
        responseValidator.validate(testContext.getLastResponse())
                .statusCode(400)
                .validationError()
                .containsErrorForField(field, errorMessage);
    }
}