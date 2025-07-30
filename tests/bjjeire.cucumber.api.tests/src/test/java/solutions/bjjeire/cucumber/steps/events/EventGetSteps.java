package solutions.bjjeire.cucumber.steps.events;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import solutions.bjjeire.api.actions.EventApiActions;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.GetBjjEventPaginatedResponse;
import solutions.bjjeire.core.data.events.GetBjjEventPaginationQuery;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.cucumber.steps.CucumberTestBase;

public class EventGetSteps extends CucumberTestBase {
    private static final Logger logger = LoggerFactory.getLogger(EventGetSteps.class);

    @Autowired private ScenarioContext scenarioContext;
    @Autowired private EventApiActions eventApi;
    private GetBjjEventPaginatedResponse paginatedEventsResponse;

    @When("I retrieve all BJJ events for county {string}")
    public void iRetrieveAllBjjEventsForCounty(String county) {
        GetBjjEventPaginationQuery query = new GetBjjEventPaginationQuery();
        query.setCounty(County.valueOf(county));
        this.paginatedEventsResponse = eventApi.getEvents(scenarioContext.getAuthToken(), query);
    }

    @Then("the response contains the event {string}")
    public void theResponseContainsTheEvent(String eventName) {
        assertNotNull(this.paginatedEventsResponse, "Paginated response was not found.");
        boolean eventFound = this.paginatedEventsResponse.data().stream()
                .anyMatch(event -> event.name().equals(eventName));
        assertTrue(eventFound, "Event with name '" + eventName + "' was not found in the response.");
    }

}