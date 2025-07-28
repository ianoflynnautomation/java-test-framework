package solutions.bjjeire.cucumber.steps.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import solutions.bjjeire.api.actions.EventApiActions;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.api.validation.ResponseAsserter;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.GetBjjEventPaginatedResponse;
import solutions.bjjeire.cucumber.context.ScenarioContext;

public class EventGetSteps {
    private static final Logger logger = LoggerFactory.getLogger(EventCreateSteps.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ScenarioContext context;


    private final EventApiActions eventApi = new EventApiActions();

    private TestClient testClient() {
        return applicationContext.getBean(TestClient.class);
    }


    @When("I retrieve all BJJ events for county {string}")
    public void iRetrieveAllBjjEventsForCounty(String county) {
        String eventName = context.getEventName();
        assertNotNull(eventName, "Event name must be in context to perform a targeted search.");
        ResponseAsserter asserter = eventApi.getEvents(testClient(), context.getAuthToken(), county, eventName);
        context.setResponseAsserter(asserter);
    }

    @Then("the event details are returned successfully")
    public void theEventDetailsAreReturnedSuccessfully() {
        context.getResponseAsserter().hasStatusCode(200);
        logger.debug("Verified status code is 200 OK.");
    }


    @Then("the response contains the event {string}")
    public void theResponseContainsTheEvent(String eventName) {
        ResponseAsserter asserter = context.getResponseAsserter();
        assertNotNull(asserter, "ResponseAsserter not found in context.");
        asserter.hasStatusCode(200);
        GetBjjEventPaginatedResponse paginatedResponse = asserter.as(GetBjjEventPaginatedResponse.class);
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
                    assertEquals(expectedValue, eventToValidate.location().venue(), "Validation failed for Location.");
                    break;
                case "Organiser":
                    assertEquals(expectedValue, eventToValidate.organiser().name(), "Validation failed for Organiser.");
                    break;
                default:
                    fail("Unknown field to validate in DataTable: " + fieldName);
            }
        });
    }

}