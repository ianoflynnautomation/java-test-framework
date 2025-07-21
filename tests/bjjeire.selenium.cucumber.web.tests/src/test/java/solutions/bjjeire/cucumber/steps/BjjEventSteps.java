package solutions.bjjeire.cucumber.steps;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import solutions.bjjeire.selenium.web.configuration.SeleniumConfig;
import solutions.bjjeire.selenium.web.pages.events.EventsPage;
import solutions.bjjeire.selenium.web.pages.events.data.BjjEventType;

@CucumberContextConfiguration
@ContextConfiguration(classes = SeleniumConfig.class)
public class BjjEventSteps {

    @Autowired
    private EventsPage eventsPage;

    @Given("I am on the BJJ app events page")
    public void i_am_on_the_bjj_app_events_page() {
        eventsPage.open();
    }

    @When("I select the county {string} from the dropdown")
    public void i_select_the_county_from_the_dropdown(String county) {
        eventsPage.selectCounty(county);
    }

//    @When("I select the event type {eventType}")
//    public void i_select_the_event_type(BjjEventType eventType) {
//        eventsPage.selectFilter(eventType);
//    }

    @When("no events exist for {string}")
    public void no_events_exist_for(String county) {
        eventsPage.assertNoDataInList();
    }

    @Then("I should see events only for the county {string}")
    public void i_should_see_events_only_for_the_county(String expectedCounty) {
        eventsPage.assertAllEventsMatchCountyFilter(expectedCounty);
    }

//    @Then("I should see events of type {eventType}")
//    public void i_should_see_events_of_type(BjjEventType eventType) {
//        eventsPage.assertAllEventsMatchTypeFilter(eventType);
//    }

    @When("I select the event type {string}")
    public void i_select_the_event_type(String eventType) {
        eventsPage.selectFilter(BjjEventType.fromString(eventType));
    }

    @Then("I should see events of type {string}")
    public void i_should_see_events_of_type(String eventType) {
        eventsPage.assertAllEventsMatchTypeFilter(BjjEventType.fromString(eventType));
    }

    @Then("I should see events for the county {string} and type {eventType}")
    public void i_should_see_events_for_the_county_and_type(String expectedCounty, BjjEventType eventType) {
        eventsPage.assertAllEventsMatchFilter(expectedCounty, eventType);
    }

    @Then("I should see events for the county {string} and type {string}")
    public void iShouldSeeEventsForTheCountyAndType(String expectedCounty, String eventType) {
        eventsPage.assertAllEventsMatchFilter(expectedCounty, BjjEventType.fromString(eventType));
    }

    @Then("I should see a message indicating no events are available")
    public void i_should_see_a_message_indicating_no_events_are_available() {
        eventsPage.assertNoDataInList();
    }

    @Then("the event list should be empty")
    public void the_event_list_should_be_empty() {
        eventsPage.assertNoDataInList();
    }

    @ParameterType(value = "All Types|Open Mat|Seminar|Tournament|Camp|Other", useForSnippets = true)
    public BjjEventType eventType(String eventTypeString) {
        return BjjEventType.fromString(eventTypeString);
    }

}
