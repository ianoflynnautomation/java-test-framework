package testng;

import eventpage.EventPage;
import org.testng.annotations.Test;
import solution.bjjeire.selenium.web.infrastructure.Browser;
import solution.bjjeire.selenium.web.infrastructure.ExecutionBrowser;
import solution.bjjeire.selenium.web.infrastructure.Lifecycle;
import solution.bjjeire.selenium.web.infrastructure.testng.WebTest;

@ExecutionBrowser(browser = Browser.CHROME, lifecycle = Lifecycle.REUSE_IF_STARTED)
public class EventTests extends WebTest {

    @Override
    protected void afterEach() {
        app().cookies().deleteAllCookies();
    }

    @Test
    public void SearchEventsUserJourney()
    {

        app().navigate().to("https://localhost:60743/events");
        app().navigate().waitForPartialUrl("/events");


        var eventsPage = app().createPage(EventPage.class);
        eventsPage.SelectCounty("Cork");


    }
}
