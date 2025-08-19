package junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.BjjEventType;
import solutions.bjjeire.core.plugins.Browser;
import solutions.bjjeire.core.plugins.Lifecycle;
import solutions.bjjeire.selenium.web.data.TestDataManager;
import solutions.bjjeire.selenium.web.infrastructure.ExecutionBrowser;
import solutions.bjjeire.selenium.web.infrastructure.junit.JunitWebTest;
import solutions.bjjeire.selenium.web.pages.events.EventsPage;
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
        @Autowired
        private TestDataManager testDataManager;

        private String authToken;
        private final List<String> createdEventIds = new ArrayList<>();

        @BeforeEach
        public void setup() {
                this.authToken = testDataManager.authenticate();
                this.createdEventIds.clear();
        }

        @Override
        protected void afterEach() {
                testDataManager.teardown(BjjEvent.class, this.createdEventIds, this.authToken);

                browserService.clearLocalStorage();
                browserService.clearSessionStorage();
                cookiesService.deleteAllCookies();
        }

        @DisplayName("Filter events by county")
        @ParameterizedTest(name = "Should show 2 events for county: {0}")
        @ValueSource(strings = { "Cork", "Kildare" })
        public void filterByCounty_shouldShowOnlyEventsForSelectedCounty(String countyStr) {
                County county = County.valueOf(countyStr);

                createdEventIds.addAll(testDataManager.seed(List.of(
                                BjjEventFactory.createBjjEvent(b -> b.county(county).name(county + " Seminar 1")),
                                BjjEventFactory.createBjjEvent(b -> b.county(county).name(county + " Seminar 2")),
                                BjjEventFactory.createBjjEvent(b -> b.county(County.Dublin).name("Dublin Open Mat"))),
                                authToken));

                eventsPage.open();
                eventsPage.selectCounty(countyStr)
                                .assertAllEventsMatchCountyFilter(countyStr)
                                .assertEventCountInListIs(2);
        }

        @DisplayName("Filter events by event type")
        @Test
        public void filterByEventType_shouldShowOnlyEventsOfSelectedType() {
                BjjEventType eventTypeToFilter = BjjEventType.SEMINAR;

                createdEventIds.addAll(testDataManager.seed(List.of(
                                BjjEventFactory.createBjjEvent(
                                                b -> b.type(BjjEventType.SEMINAR).name("Cork Seminar 1")),
                                BjjEventFactory.createBjjEvent(
                                                b -> b.type(BjjEventType.SEMINAR).name("Cork Seminar 2")),
                                BjjEventFactory.createBjjEvent(
                                                b -> b.type(BjjEventType.TOURNAMENT).name("Cork Open Mat"))),
                                authToken));

                eventsPage.open();
                eventsPage.selectFilter(eventTypeToFilter)
                                .assertAllEventsMatchTypeFilter(eventTypeToFilter)
                                .assertEventCountInListIs(2);
        }

        @DisplayName("Filter events by both county and event type")
        @ParameterizedTest(name = "Should show 1 event for {0} and {1}")
        @CsvSource({
                        "Cork, Seminar",
                        "Cork, Tournament"
        })
        public void filterByCountyAndType_shouldShowMatchingEvents(String countyStr, String eventTypeStr) {

                County county = County.valueOf(countyStr);
                BjjEventType eventType = BjjEventType.fromString(eventTypeStr);

                createdEventIds.addAll(testDataManager.seed(List.of(
                                BjjEventFactory.createBjjEvent(b -> b.county(county).type(BjjEventType.SEMINAR)
                                                .name("Cork Seminar")),
                                BjjEventFactory.createBjjEvent(b -> b.county(county).type(BjjEventType.TOURNAMENT)
                                                .name("Cork Open Mat")),
                                BjjEventFactory.createBjjEvent(b -> b.county(County.Dublin).type(BjjEventType.SEMINAR)
                                                .name("Dublin Seminar"))),
                                authToken));

                eventsPage.open();
                eventsPage.selectCounty(countyStr)
                                .selectFilter(eventType)
                                .assertAllEventsMatchFilter(countyStr, eventType)
                                .assertEventCountInListIs(1);
        }

        @Test
        @DisplayName("Should show empty list when filtering for a county with no matching events")
        public void filterForCountyWithNoMatchingEvents_shouldShowEmptyList() {

                createdEventIds.addAll(testDataManager.seed(List.of(
                                BjjEventFactory.createBjjEvent(b -> b.county(County.Dublin))), authToken));

                eventsPage.open();
                eventsPage.selectCounty("Clare")
                                .assertNoDataInList();
        }

        @Test
        @DisplayName("Should show empty list when no events exist in the system")
        public void filterWhenNoEventsExist_shouldShowEmptyList() {

                eventsPage.open();
                eventsPage.selectCounty("Wexford")
                                .assertNoDataInList();
        }
}