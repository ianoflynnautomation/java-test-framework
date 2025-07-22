package junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.selenium.web.pages.events.EventsPage;
import solutions.bjjeire.selenium.web.infrastructure.Browser;
import solutions.bjjeire.selenium.web.infrastructure.ExecutionBrowser;
import solutions.bjjeire.selenium.web.infrastructure.Lifecycle;
import solutions.bjjeire.selenium.web.infrastructure.junit.JunitWebTest;
import solutions.bjjeire.selenium.web.pages.events.data.BjjEventType;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.CookiesService;


@ExecutionBrowser(browser = Browser.FIREFOX, lifecycle = Lifecycle.REUSE_IF_STARTED)
public class EventTests extends JunitWebTest {

    @Autowired
    private CookiesService cookiesService;

    @Autowired
    private BrowserService browserService;

    @Autowired
    private EventsPage eventsPage;

    @Override
    protected void afterEach() {
        browserService.clearLocalStorage();
        browserService.clearSessionStorage();
        cookiesService.deleteAllCookies();
    }

    @DisplayName("Filter events by county")
    @ParameterizedTest(name = "Should show events only for the county: {0}")
    @ValueSource(strings = {"Cork", "Kildare"})
    public void filterByCounty_shouldShowOnlyEventsForSelectedCounty(String county) {

        eventsPage.open();
        eventsPage.selectCounty(county)
                .assertAllEventsMatchCountyFilter(county);
    }

    @DisplayName("Filter events by event type")
    @ParameterizedTest(name = "Should show events only of type: {0}")
    @ValueSource(strings = {"Seminar"})
    public void filterByEventType_shouldShowOnlyEventsOfSelectedType(String eventTypeStr) {
        BjjEventType eventType = BjjEventType.fromString(eventTypeStr);

        eventsPage.open();
        eventsPage.selectFilter(eventType)
                .assertAllEventsMatchTypeFilter(eventType);
    }

    @Test
    @DisplayName("Filter events by both county and event type")
    public void filterByCountyAndType_shouldShowMatchingEvents() {
        String county = "Cork";
        BjjEventType eventType = BjjEventType.fromString("Seminar");

        eventsPage.open();
        eventsPage.selectCounty(county)
                .selectFilter(eventType)
                .assertAllEventsMatchFilter(county, eventType);
    }

    @DisplayName("No events found for a given county")
    @ParameterizedTest(name = "Should show 'no events' message for county: {0}")
    @ValueSource(strings = {"Clare", "Wexford"})
    public void filterByCountyWithNoEvents_shouldShowNoEventsMessage(String countyWithNoEvents) {

        eventsPage.open();
        eventsPage.selectCounty(countyWithNoEvents)
                .assertNoDataInList();
    }

}
