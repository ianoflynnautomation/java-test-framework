package junit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.selenium.web.pages.events.EventsPage;
import solutions.bjjeire.selenium.web.infrastructure.Browser;
import solutions.bjjeire.selenium.web.infrastructure.ExecutionBrowser;
import solutions.bjjeire.selenium.web.infrastructure.Lifecycle;
import solutions.bjjeire.selenium.web.infrastructure.junit.JunitWebTest;
import solutions.bjjeire.selenium.web.pages.events.data.BjjEventType;
import solutions.bjjeire.selenium.web.pages.events.data.EventCardDetails;
import solutions.bjjeire.selenium.web.services.CookiesService;

import java.util.List;

@ExecutionBrowser(browser = Browser.FIREFOX, lifecycle = Lifecycle.RESTART_EVERY_TIME)
public class EventTests extends JunitWebTest {

    @Autowired
    private CookiesService cookiesService;

    @Autowired
    private EventsPage eventPage;

    @Override
    protected void afterEach() {
        cookiesService.deleteAllCookies();
    }

    @Test
    public void SearchEventsUserJourney() {

        EventCardDetails BERLIN_SEMINAR_EVENT = new EventCardDetails(
                "Berlin BJJ Seminar Series",
                "Cork",
                List.of(BjjEventType.SEMINAR),
                "Schillerstraße 10, 10625 Berlin, Germany",
                "Organised by: berlingrappling.com",
                "",
                "",
                "EUR 50.00"
                );

        eventPage.open();
        eventPage.selectCounty("Cork")
                .selectFilter(BjjEventType.SEMINAR)
                .assertEventIsInList(BERLIN_SEMINAR_EVENT)
                .assertTotalEventsFoundInList(1);

    }
}
