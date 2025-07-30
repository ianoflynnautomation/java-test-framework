package solutions.bjjeire.cucumber.steps.events;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.actions.EventApiActions;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;
import solutions.bjjeire.cucumber.context.TestState;

import java.util.Map;


public class EventCreateSteps {

    @Autowired
    private TestState testState;
    @Autowired
    private EventApiActions eventApi;


    @Given("a new event has been prepared")
    public void aNewEventHasBeenPrepared() {
        testState.setRequestPayload(new CreateBjjEventCommand(BjjEventFactory.getValidBjjEvent()));
    }
    
    @Given("Admin has a BJJ event with {string} set to {string}")
    public void adminAndyHasABjjEventWithFieldSetToInvalidValue(String field, String invalidValue) {
        Map<String, String> invalidDetails = Map.of(field, invalidValue);
        CreateBjjEventCommand invalidPayload = BjjEventFactory.createPayloadWithInvalidDetails(invalidDetails);
        testState.setRequestPayload(invalidPayload);
    }


    @When("Admin adds the new event")
    public void adminAndyAddsTheNewEvent() {
        CreateBjjEventCommand command = (CreateBjjEventCommand) testState.getRequestPayload();
        
        var response = eventApi.createEvent(testState.getAuthToken(), command);
        testState.setLastResponse(response);
        
        if (response.getStatusCode() == 201) {
            BjjEvent createdEvent = response.as(CreateBjjEventResponse.class).data();
            testState.addEntity(createdEvent);
        }
    }
    
    @When("Admin attempts to create the BJJ event")
    public void adminAndyAttemptsToCreateTheBjjEvent() {
        var response = eventApi.attemptToCreateEventWithInvalidData(
                testState.getAuthToken(),
                testState.getRequestPayload()
        );
        testState.setLastResponse(response);
    }

 
    @Then("the event should be successfully added")
    public void theEventShouldBeSuccessfullyAdded() {
        testState.getLastResponse().then().statusCode(201);
    }


    @Then("Admin is notified that the event creation failed for {string} with message {string}")
    public void adminIsNotifiedThatTheEventCreationFailedForWithMessage(String field, String errorMessage) {
        testState.getLastResponse().then()
                .statusCode(400)
                .validationError()
                .containsErrorForField(field, errorMessage);
    }
}