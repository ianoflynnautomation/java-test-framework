package solutions.bjjeire.cucumber.steps.events;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
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
import java.util.Optional;

public class EventCreateSteps extends CucumberTestBase {
    private static final Logger logger = LoggerFactory.getLogger(EventCreateSteps.class);

    @Autowired private ScenarioContext scenarioContext;
    @Autowired private EventApiActions eventApi;

    // Holds state specific to this step definition class
    private BjjEvent createdEvent;

    @Given("I have valid details for a new BJJ event named {string}")
    public void iHaveValidDetailsForANewBjjEventNamed(String eventName) {
        BjjEvent eventToCreate = BjjEventFactory.createBjjEvent(builder -> builder.name(eventName));
        scenarioContext.setRequestPayload(new CreateBjjEventCommand(eventToCreate));
    }

    @Given("I have a BJJ event with invalid {string}")
    public void iHaveABjjEventWithInvalid(String invalidField) {
        CreateBjjEventCommand invalidCommand = BjjEventFactory.createInvalidEvent(invalidField);
        scenarioContext.setRequestPayload(invalidCommand);
    }

    @Given("a BJJ event exists with the name {string} in county {string}")
    public void aBjjEventExistsWithTheNameInCounty(String eventName, String county) {
        BjjEvent eventToCreate = BjjEventFactory.createBjjEvent(builder -> builder.name(eventName).county(County.valueOf(county)));
        CreateBjjEventResponse response = eventApi.createEvent(scenarioContext.getAuthToken(), eventToCreate);
        this.createdEvent = response.data();
        scenarioContext.getCreatedEntities().add(this.createdEvent);
    }

    @When("I create the BJJ event")
    public void iCreateTheBjjEvent() {
        CreateBjjEventCommand command = (CreateBjjEventCommand) scenarioContext.getRequestPayload();
        CreateBjjEventResponse response = eventApi.createEvent(scenarioContext.getAuthToken(), command.data());
        this.createdEvent = response.data();
        scenarioContext.getCreatedEntities().add(this.createdEvent);
    }

    @When("I attempt to create the BJJ event")
    public void iAttemptToCreateTheBjjEvent() {
        ValidatableResponse response = eventApi.attemptToCreateEvent(scenarioContext.getAuthToken(), scenarioContext.getRequestPayload());
        scenarioContext.setLastResponse(response);
    }

    @Then("the event is created successfully")
    public void theEventIsCreatedSuccessfully() {
        assertNotNull(this.createdEvent, "Event was not created successfully.");
        assertNotNull(this.createdEvent.id(), "Created event ID is null.");
    }

    @Then("the API returns a bad request error with message {string}")
    public void theApiReturnsABadRequestErrorWithMessage(String errorMessage) {
        ValidatableResponse response = scenarioContext.getLastResponse();
        assertNotNull(response, "Response was not found in context.");
        response.hasStatusCode(400).contentContains(errorMessage);
    }

    @And("the event details include:")
    public void theEventDetailsInclude(DataTable dataTable) {
        assertNotNull(this.createdEvent, "Cannot verify details, no created event found in context.");
        Map<String, String> expectedDetails = dataTable.asMap();

        expectedDetails.forEach((field, expectedValue) -> {
            switch (field) {
                case "Name":
                    assertEquals(expectedValue, this.createdEvent.name(), "Validation failed for Name.");
                    break;
                case "Location":
                    assertEquals(expectedValue, this.createdEvent.location().venue(), "Validation failed for Location venue.");
                    break;
                case "Organiser":
                    assertEquals(expectedValue, this.createdEvent.organiser().name(), "Validation failed for Organiser name.");
                    break;
                default:
                    fail("Unknown field to validate in DataTable: " + field);
            }
        });
    }

}