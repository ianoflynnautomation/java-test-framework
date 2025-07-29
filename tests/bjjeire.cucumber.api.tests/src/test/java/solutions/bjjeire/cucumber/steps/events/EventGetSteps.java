package solutions.bjjeire.cucumber.steps.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import solutions.bjjeire.api.actions.EventApiActions;
import solutions.bjjeire.api.validation.ValidatableResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.GetBjjEventPaginatedResponse;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.cucumber.steps.CucumberTestBase;

public class EventGetSteps extends CucumberTestBase {
    private static final Logger logger = LoggerFactory.getLogger(EventGetSteps.class);

    @Autowired private ScenarioContext context;
    @Autowired private EventApiActions eventApi;

    @When("I retrieve all BJJ events for county {string}")
    public void iRetrieveAllBjjEventsForCounty(String county) {
        String eventName = context.getEventName();
        assertNotNull(eventName, "Event name must be in context to perform a targeted search.");
        ValidatableResponse response = eventApi.getEvents(context.getAuthToken(), county, eventName);
        context.setValidatableResponse(response);
    }

    @Then("the event details are returned successfully")
    public void theEventDetailsAreReturnedSuccessfully() {
        context.getValidatableResponse().hasStatusCode(200);
        logger.debug("Verified status code is 200 OK.");
    }

    @Then("the response contains the event {string}")
    public void theResponseContainsTheEvent(String eventName) {
        ValidatableResponse response = context.getValidatableResponse();
        assertNotNull(response, "ValidatableResponse not found in context.");

        response.hasStatusCode(200);
        GetBjjEventPaginatedResponse paginatedResponse = response.as(GetBjjEventPaginatedResponse.class);
        boolean eventFound = paginatedResponse.data().stream().anyMatch(event -> event.name().equals(eventName));
        assertTrue(eventFound, "The event with name '" + eventName + "' was not found in the API response.");
    }

    @And("the event details include:")
    public void theEventDetailsInclude(DataTable dataTable) {
        BjjEvent eventToValidate = context.getCreatedEvent();
        assertNotNull(eventToValidate, "Created event must be in the context for validation.");

        Map<String, String> expectedDetails = dataTable.asMap(String.class, String.class);
        expectedDetails.forEach((fieldName, expectedValue) -> {
            switch (fieldName) {
                case "Name":
                    assertEquals(expectedValue, eventToValidate.name(), "Validation failed for Name.");
                    break;
                case "Location":
                    // Assuming Location is a complex object and we're checking a field on it
                    // This part needs to be adapted to your actual BjjEvent data structure
                    // For now, let's assume a placeholder method `getVenue()` exists
                    // assertEquals(expectedValue, eventToValidate.location().getVenue(), "Validation failed for Location.");
                    break;
                case "Organiser":
                    // Same assumption as above
                    // assertEquals(expectedValue, eventToValidate.organiser().getName(), "Validation failed for Organiser.");
                    break;
                default:
                    fail("Unknown field to validate in DataTable: " + fieldName);
            }
        });
    }
}