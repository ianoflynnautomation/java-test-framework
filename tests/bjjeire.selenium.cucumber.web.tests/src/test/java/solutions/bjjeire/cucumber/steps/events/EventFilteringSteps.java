package solutions.bjjeire.cucumber.steps.events;

import java.util.List;
import java.util.stream.Collectors;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.BjjEventType;
import solutions.bjjeire.cucumber.context.ScenarioContext;
import solutions.bjjeire.cucumber.context.TestDataContext;
import solutions.bjjeire.selenium.web.data.TestDataManager;
import solutions.bjjeire.selenium.web.pages.events.EventsPage;

@Slf4j
@RequiredArgsConstructor
public class EventFilteringSteps {

    private final EventsPage eventsPage;
    private final TestDataManager testDataManager;
    private final ScenarioContext scenarioContext;
    private final TestDataContext testDataContext;

    @Data
    public static class EventDataRow {
        private String name;
        private County county;
        private BjjEventType type;
    }

    @Given("the following BJJ events exist:")
    public void the_following_bjj_events_exist(DataTable dataTable) {
        List<EventDataRow> dataRows = dataTable.asList(EventDataRow.class);

        List<BjjEvent> eventsToCreate = dataRows.stream()
                .map(row -> BjjEventFactory.createBjjEvent(builder -> builder
                        .name(row.getName())
                        .county(row.getCounty())
                        .type(row.getType())))
                .collect(Collectors.toList());

        String authToken = scenarioContext.getAuthToken();
        List<String> createdIds = testDataManager.seed(eventsToCreate, authToken);

        testDataContext.addEntityIds(BjjEvent.class, createdIds);
        log.info("Seeded {} BJJ event(s) for the test.", createdIds.size());
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
        eventsPage.assertEventCountInListIs(expectedCount);
        ;
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