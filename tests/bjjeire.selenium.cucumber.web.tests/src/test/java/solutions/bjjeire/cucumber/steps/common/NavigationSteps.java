package solutions.bjjeire.cucumber.steps.common;

import io.cucumber.java.en.Given;
import solutions.bjjeire.selenium.web.pages.events.EventsPage;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;

public class NavigationSteps {

    private final EventsPage eventsPage;
    private final GymsPage gymsPage;

    public NavigationSteps(EventsPage eventsPage, GymsPage gymsPage) {
        this.eventsPage = eventsPage;
        this.gymsPage = gymsPage;
    }

    @Given("I am on the BJJ app events page")
    public void i_am_on_the_bjj_app_events_page() {
        eventsPage.open();
    }

    @Given("I am on the BJJ app gyms page")
    public void i_am_on_the_bjj_app_gyms_page() {
        gymsPage.open();
    }
}
