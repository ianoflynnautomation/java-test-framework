package solutions.bjjeire.cucumber.steps.events;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.BjjEventType;
import solutions.bjjeire.cucumber.context.BaseContext;
import solutions.bjjeire.cucumber.context.EventContext;
import solutions.bjjeire.selenium.web.data.TestDataManager;
import solutions.bjjeire.selenium.web.pages.events.EventsPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventFilteringSteps {

    private static final Logger log = LoggerFactory.getLogger(EventFilteringSteps.class);

    @Autowired private EventsPage eventsPage;
    @Autowired private TestDataManager testDataManager;
    @Autowired private BaseContext baseContext;
    @Autowired private EventContext eventContext;

    @Given("the following BJJ events exist:")
    public void the_following_bjj_events_exist(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        List<BjjEvent> eventsToCreate = new ArrayList<>();

        for (Map<String, String> columns : rows) {
            BjjEvent event = BjjEventFactory.createBjjEvent(builder -> {
                String name = columns.get("Name");
                String county = columns.get("County");
                String type = columns.get("Type");

                if (name != null) builder.name(name);
                if (county != null) builder.county(County.valueOf(county.replace(" ", "")));
                if (type != null) builder.type(BjjEventType.fromString(type));
            });
            eventsToCreate.add(event);
        }

        String authToken = baseContext.getAuthToken();
        List<String> createdIds = testDataManager.seedEvents(eventsToCreate, authToken);
        eventContext.addAllCreatedEventIds(createdIds);
        log.debug("Created {} BJJ event(s) for the test.", createdIds.size());
    }

    @Given("no BJJ events exist")
    public void no_bjj_events_exist() {
        log.debug("Ensuring no BJJ events exist for this test.");
    }

    @When("I filter events by county {string}")
    public void i_filter_events_by_county(String county) {
        eventsPage.selectCounty(county);
    }

    @When("I filter events by type {string}")
    public void i_filter_events_by_type(String eventType) {
        eventsPage.selectFilter(BjjEventType.fromString(eventType));
    }

    @Then("the displayed events include only those for county {string}")
    public void the_displayed_events_include_only_those_for_county(String expectedCounty) {
        eventsPage.assertAllEventsMatchCountyFilter(expectedCounty);
    }

    @Then("the displayed events include only those of type {string}")
    public void the_displayed_events_include_only_those_of_type(String eventType) {
        eventsPage.assertAllEventsMatchTypeFilter(BjjEventType.fromString(eventType));
    }

    @Then("the event list contains exactly {int} events")
    public void the_event_list_contains_exactly_events(int expectedCount) {
        eventsPage.assertEventCountInListIs(expectedCount);
    }

    @Then("the event list is empty")
    public void the_event_list_is_empty() {
        eventsPage.assertNoDataInList();
    }
}