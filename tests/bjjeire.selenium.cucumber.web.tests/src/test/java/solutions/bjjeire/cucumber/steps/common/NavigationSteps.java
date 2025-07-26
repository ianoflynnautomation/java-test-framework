package solutions.bjjeire.cucumber.steps;

import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.selenium.web.pages.events.EventsPage;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;

public class NavigationSteps {

    @Autowired private EventsPage eventsPage;
    @Autowired private GymsPage gymsPage;

    @Given("I am on the BJJ app events page")
    public void i_am_on_the_bjj_app_events_page() {
        eventsPage.open();
    }

    @Given("I am on the BJJ app gyms page")
    public void i_am_on_the_bjj_app_gyms_page() {
        gymsPage.open();
    }
}
