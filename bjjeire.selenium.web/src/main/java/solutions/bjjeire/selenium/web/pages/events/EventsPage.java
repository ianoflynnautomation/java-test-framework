package solutions.bjjeire.selenium.web.pages.events;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import solutions.bjjeire.core.data.events.BjjEventType;
import solutions.bjjeire.selenium.web.components.Button;
import solutions.bjjeire.selenium.web.components.Heading;
import solutions.bjjeire.selenium.web.components.Label;
import solutions.bjjeire.selenium.web.components.Select;
import solutions.bjjeire.selenium.web.components.Span;
import solutions.bjjeire.selenium.web.components.custom.event.EventArticle;
import solutions.bjjeire.selenium.web.configuration.UrlSettings;
import solutions.bjjeire.selenium.web.pages.ListPageBase;
import solutions.bjjeire.selenium.web.pages.events.data.EventCardDetails;
import solutions.bjjeire.selenium.web.services.ComponentCreateService;
import solutions.bjjeire.selenium.web.services.NavigationService;
import static solutions.bjjeire.selenium.web.utils.EventsPageDataTestIds.EVENTS_LIST_ITEM;
import static solutions.bjjeire.selenium.web.utils.EventsPageDataTestIds.PAGE_HEADER_TITLE;
import static solutions.bjjeire.selenium.web.utils.EventsPageDataTestIds.PAGE_HEADER_TOTAL;
import static solutions.bjjeire.selenium.web.utils.EventsPageDataTestIds.SELECT_FILTER;

@Component
@Scope("prototype")
public class EventsPage extends ListPageBase {

    private final UrlSettings urlSettings;

    public EventsPage(NavigationService navigationService,
            ComponentCreateService componentCreateService,
            UrlSettings urlSettings) {
        super(navigationService, componentCreateService);
        this.urlSettings = urlSettings;
    }

    @Override
    protected String getUrl() {
        return urlSettings.getEventUrl();
    }

    public Heading titleText() {
        return create().byDataTestId(Heading.class, PAGE_HEADER_TITLE);
    }

    public Label foundEventsTotalText() {
        return create().byDataTestId(Label.class, PAGE_HEADER_TOTAL);
    }

    private Select countyDropdown() {
        return create().byDataTestId(Select.class, SELECT_FILTER);
    }

    private Button eventTypeButton(String buttonText) {
        return create().byInnerTextContaining(Button.class, buttonText);
    }

    public List<EventArticle> eventCards() {
        return create().allByDataTestId(EventArticle.class, EVENTS_LIST_ITEM);
    }

    public EventsPage selectCounty(String county) {
        countyDropdown().toBeClickable().waitToBe();
        countyDropdown().selectByText(county);
        return this;
    }

    public EventsPage selectFilter(BjjEventType eventType) {
        eventTypeButton(eventType.toString()).toBeClickable().waitToBe();
        eventTypeButton(eventType.toString()).click();
        return this;
    }

    public EventsPage assertTotalEventsFoundInList(Integer expectedEventsTotal) {
        if (null == expectedEventsTotal) {
            foundEventsTotalText().validateTextIs(String.format("Found %d events.", expectedEventsTotal));
        } else
            switch (expectedEventsTotal) {
                case 0 -> foundEventsTotalText().validateTextIs("");
                case 1 -> foundEventsTotalText().validateTextIs(String.format("Found %d event.", expectedEventsTotal));
                default ->
                    foundEventsTotalText().validateTextIs(String.format("Found %d events.", expectedEventsTotal));
            }
        return this;
    }

    public EventsPage assertEventIsInList(EventCardDetails eventCard) {

        EventArticle cardToAssert = eventCards().stream()
                .filter(card -> card.headingText().getText().trim().equals(eventCard.name()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Could not find an event card with the name: " + eventCard.name()));

        cardToAssert.headingText().validateTextIs(eventCard.name());

        return this;
    }

    public EventsPage assertEventCountInListIs(int expectedCount) {

        List<EventArticle> cards = eventCards();
        assertEquals(expectedCount, cards.size(),
                String.format("Expected to find %d event cards, but found %d.", expectedCount, cards.size()));
        return this;
    }

    public EventsPage assertAllEventsMatchCountyFilter(String expectedCounty) {
        List<EventArticle> cards = eventCards();
        assertFalse(cards.isEmpty(), "Expected to find event cards after filtering, but none were found.");

        List<String> mismatchedCounties = cards.stream()
                .map(EventArticle::county)
                .map(Span::getText)
                .map(Object::toString)
                .filter(actualCounty -> !actualCounty.equalsIgnoreCase(expectedCounty))
                .toList();

        assertTrue(mismatchedCounties.isEmpty(),
                String.format("The following counties did not match the filter '%s': %s", expectedCounty,
                        mismatchedCounties));

        return this;
    }

    public EventsPage assertNoEventsFromCounty(String forbiddenCounty) {
        List<EventArticle> cards = eventCards();

        List<EventArticle> cardsFromForbiddenCounty = cards.stream()
                .filter(card -> card.county().getText().equalsIgnoreCase(forbiddenCounty))
                .toList();

        assertTrue(cardsFromForbiddenCounty.isEmpty(),

                String.format("Assertion failed: Found events from a county that should have been excluded.%n" +
                        "Expected NO events from '%s', but the following events were found: %s",
                        forbiddenCounty,
                        cardsFromForbiddenCounty.stream()
                                .map(card -> card.headingText().getText())
                                .collect(Collectors.joining(", "))));

        return this;
    }

    public EventsPage assertAllEventsMatchTypeFilter(BjjEventType expectedEventType) {
        List<EventArticle> cards = eventCards();

        assertFalse(cards.isEmpty(), "Assertion failed: Expected to find event cards, but the list was empty.");

        List<EventArticle> mismatchedCards = cards.stream()
                .filter(card -> {

                    return card.TypeLabels().stream()
                            .noneMatch(label -> label.getText().equalsIgnoreCase(expectedEventType.toString()));
                })
                .toList();

        assertTrue(mismatchedCards.isEmpty(),

                String.format("Assertion failed: Found events of a different type. Expected all events to be '%s'.%n" +
                        "The following events did not match: %s",
                        expectedEventType,
                        mismatchedCards.stream()
                                .map(card -> card.headingText().getText())
                                .collect(Collectors.joining(", "))));

        return this;
    }

    public EventsPage assertNoEventsMatchType(BjjEventType forbiddenEventType) {
        List<EventArticle> cards = eventCards();

        List<EventArticle> cardsWithForbiddenType = cards.stream()
                .filter(card -> {

                    return card.TypeLabels().stream()
                            .anyMatch(label -> label.getText().equalsIgnoreCase(forbiddenEventType.toString()));
                })
                .toList();

        assertTrue(cardsWithForbiddenType.isEmpty(),

                String.format("Assertion failed: Found events of a type that should have been filtered out.%n" +
                        "Expected NO events of type '%s', but the following events matched: %s",
                        forbiddenEventType,
                        cardsWithForbiddenType.stream()
                                .map(card -> card.headingText().getText())
                                .collect(Collectors.joining(", "))));

        return this;
    }

    public EventsPage assertAllEventsMatchFilter(String expectedCounty, BjjEventType expectedEventType) {
        assertAllEventsMatchCountyFilter(expectedCounty);
        assertAllEventsMatchTypeFilter(expectedEventType);
        return this;
    }

}