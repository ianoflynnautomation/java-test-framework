package solutions.bjjeire.selenium.web.pages.events;

import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import solutions.bjjeire.selenium.web.components.*;
import solutions.bjjeire.selenium.web.components.Button;
import solutions.bjjeire.selenium.web.components.Label;
import solutions.bjjeire.selenium.web.components.custom.event.EventArticle;
import solutions.bjjeire.selenium.web.configuration.UrlSettings;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;
import solutions.bjjeire.selenium.web.pages.ListPageBase;
import solutions.bjjeire.selenium.web.pages.WebPage;
import solutions.bjjeire.selenium.web.pages.events.data.BjjEventType;
import solutions.bjjeire.selenium.web.pages.events.data.EventCardDetails;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentCreateService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.services.NavigationService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@Component
@Scope("prototype")
public class EventsPage extends ListPageBase {

    private final UrlSettings urlSettings;

    @Autowired
    public EventsPage(DriverService driverService, JavaScriptService javaScriptService, BrowserService browserService, ComponentWaitService componentWaitService, WebSettings webSettings, ApplicationContext applicationContext, WaitStrategyFactory waitStrategyFactory, NavigationService navigationService, ComponentCreateService componentCreateService, UrlSettings urlSettings) {
        super(driverService, javaScriptService, browserService, componentWaitService, webSettings, applicationContext, waitStrategyFactory, navigationService, componentCreateService);
        this.urlSettings = urlSettings;
    }

    @Override
    protected String getUrl() {
        return urlSettings.getEventUrl();
    }

    public Heading titleText() {return create().byDataTestId(Heading.class, "events-page-header-title");}

    public Label foundEventsTotalText() {return create().byDataTestId(Label.class, "events-page-header-total");}

    private Select countyDropdown() {return create().byDataTestId(Select.class, "select-filter-select");}

    private Button eventTypeButton(String buttonText) {return create().byInnerTextContaining(Button.class, buttonText);}

    private List<EventArticle> eventCards() {return create().allByDataTestId(EventArticle.class, "events-list-item");}


    public EventsPage selectCounty(String county) {
        countyDropdown().toBeClickable();
        countyDropdown().selectByText(county);
        return this;
    }

    public EventsPage selectFilter(BjjEventType eventType) {
        eventTypeButton(eventType.toString()).toBeClickable();
        eventTypeButton(eventType.toString()).click();
        return this;
    }

    public EventsPage assertTotalEventsFoundInList(Integer expectedEventsTotal) {
        switch (expectedEventsTotal) {
            case 0:
                break;
            case 1:
                foundEventsTotalText().validateTextIs(String.format("Found %d event.", expectedEventsTotal));
                break;
            default:
                foundEventsTotalText().validateTextIs(String.format("Found %d events.", expectedEventsTotal));
        }
        return this;
    }


    public EventsPage assertEventIsInList(EventCardDetails eventCard)
    {
        EventArticle cardToAssert = eventCards().stream()
                .filter(card -> card.headingText().getText().trim().equals(eventCard.name()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Could not find an event card with the name: " + eventCard.name()));

        cardToAssert.headingText().validateTextIs(eventCard.name());

        return this;
    }

    public EventsPage assertAllEventsMatchCountyFilter(String expectedCounty) {
        List<EventArticle> cards = eventCards();
        assertFalse(cards.isEmpty(), "Expected to find event cards after filtering, but none were found.");

        for (EventArticle card : cards) {
            // FIX: Corrected method call from county() to countyText()
            String actualCounty = card.county().getText();
            assertTrue(actualCounty.toLowerCase().contains(expectedCounty.toLowerCase()),
                    String.format("Event card '%s' should have county '%s' but was '%s'", card.headingText().getText(), expectedCounty, actualCounty));
        }
        return this;
    }

    public EventsPage assertAllEventsMatchTypeFilter(BjjEventType expectedEventType) {
        List<EventArticle> cards = eventCards();
        assertFalse(cards.isEmpty(), "Expected to find event cards after filtering, but none were found.");

        for (EventArticle card : cards) {
            // Assert Event Type
            boolean hasMatchingType = card.TypeLabels().stream()
                    .anyMatch(label -> label.getText().equalsIgnoreCase(expectedEventType.toString()));
            assertTrue(hasMatchingType,
                    String.format("Event card '%s' should have event type '%s'", card.headingText().getText(), expectedEventType));
        }

        return this;
    }


    public EventsPage assertAllEventsMatchFilter(String expectedCounty, BjjEventType expectedEventType) {
        assertAllEventsMatchCountyFilter(expectedCounty);
        assertAllEventsMatchTypeFilter(expectedEventType);
        return this;
    }
}