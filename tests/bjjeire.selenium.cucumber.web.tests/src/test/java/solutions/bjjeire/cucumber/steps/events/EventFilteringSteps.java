package solutions.bjjeire.cucumber.steps.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.BjjEventType;
import solutions.bjjeire.cucumber.context.EventContext;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.selenium.web.data.TestDataManager;
import solutions.bjjeire.selenium.web.pages.events.EventsPage;

public class EventFilteringSteps {

    private static final Logger log = LoggerFactory.getLogger(EventFilteringSteps.class);

    private final EventsPage eventsPage;
    private final TestDataManager testDataManager;
    private final ScenarioContext scenarioContext;
    private final EventContext eventContext;

    public EventFilteringSteps(EventsPage eventsPage, TestDataManager testDataManager, ScenarioContext scenarioContext,
            EventContext eventContext) {
        this.eventsPage = eventsPage;
        this.testDataManager = testDataManager;
        this.scenarioContext = scenarioContext;
        this.eventContext = eventContext;
    }

    @Given("the following BJJ events exist:")
    public void the_following_bjj_events_exist(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        List<BjjEvent> eventsToCreate = new ArrayList<>();

        for (Map<String, String> columns : rows) {
            BjjEvent event = BjjEventFactory.createBjjEvent(builder -> {
                String name = columns.get("Name");
                String county = columns.get("County");
                String type = columns.get("Type");

                if (name != null)
                    builder.name(name);
                if (county != null)
                    builder.county(County.valueOf(county.replace(" ", "")));
                if (type != null)
                    builder.type(BjjEventType.fromString(type));
            });
            eventsToCreate.add(event);
        }

        String authToken = scenarioContext.getAuthToken();
        List<String> createdIds = testDataManager.seedEvents(eventsToCreate, authToken);
        eventContext.addAllCreatedEventIds(createdIds);
        log.debug("Created {} BJJ event(s) for the test.", createdIds.size());

    }

    @Given("no BJJ events exist")
    public void no_bjj_events_exist() {
    }

    @When("I search for events in the county {string}")
    public void iSearchForEventsInTheCounty(String county) {
        eventsPage.open();
        eventsPage.selectCounty(county);
    }

    @When("I search events by county {string}")
    public void iSearchEventsByCounty(String county) {
        eventsPage.open();
        eventsPage.selectCounty(county);
    }

    @When("I search for events of type {string}")
    public void iSearchForEventsOfType(String eventType) {
        eventsPage.open();
        eventsPage.selectFilter(BjjEventType.fromString(eventType));
    }

    @Then("I should see exactly {int} events for {string}")
    public void iShouldSeeExactlyExpectedCountEventsFor(int expectedCount, String county) {
        eventsPage.assertAllEventsMatchCountyFilter(county + " County");
        eventsPage.assertEventCountInListIs(expectedCount);;
    }

    @Then("I should not see any events")
    public void iShouldNotSeeAnyEvents() {
        eventsPage.assertNoDataInList();
    }

    @Then("I should see exactly {int} events of type {string}")
    public void iShouldSeeExactlyEventsOfType(int expectedCount, String eventType) {
        eventsPage.assertAllEventsMatchTypeFilter(BjjEventType.fromString(eventType));
        eventsPage.assertEventCountInListIs(expectedCount);
        eventsPage.assertTotalEventsFoundInList(expectedCount);
    }
}