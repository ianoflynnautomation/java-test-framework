package solutions.bjjeire.cucumber.steps.events;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import solutions.bjjeire.core.data.events.BjjEventType;
import solutions.bjjeire.cucumber.actions.EventActions;
import solutions.bjjeire.selenium.web.pages.events.EventsPage;

@Slf4j
@RequiredArgsConstructor
public class EventFilteringSteps {

    private final EventsPage eventsPage;
    private final EventActions eventActions;

    @Given("the following BJJ events exist:")
    public void the_following_bjj_events_exist(DataTable dataTable) {
        eventActions.createEvents(dataTable);
    }

    @Given("no BJJ events exist")
    public void no_bjj_events_exist() {
        eventsPage.assertNoDataInList();
    }

    @Given("I can access events")
    public void iCanAccessEvents() {
        eventsPage.open();
    }

    @When("I search events by county {string}")
    public void iSearchEventsByCounty(String county) {
        eventsPage.selectCounty(county);
    }

    @When("I search for events of type {string}")
    public void iSearchForEventsOfType(String eventType) {
        eventsPage.selectFilter(BjjEventType.fromString(eventType));
    }

    @Then("I should see exactly {int} events for {string}")
    public void iShouldSeeExactlyExpectedCountEventsFor(int expectedCount, String county) {
        eventsPage.assertAllEventsMatchCountyFilter(county + " County");
        eventsPage.assertEventCountInListIs(expectedCount);
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