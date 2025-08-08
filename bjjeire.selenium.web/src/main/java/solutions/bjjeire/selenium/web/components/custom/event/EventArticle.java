package solutions.bjjeire.selenium.web.components.custom.event;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import solutions.bjjeire.selenium.web.components.Anchor;
import solutions.bjjeire.selenium.web.components.Div;
import solutions.bjjeire.selenium.web.components.Heading;
import solutions.bjjeire.selenium.web.components.Span;
import solutions.bjjeire.selenium.web.components.WebComponent;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;
import static solutions.bjjeire.selenium.web.utils.EventsPageDataTestIds.*;

@Component
@Scope("prototype")
public class EventArticle extends WebComponent {

    public EventArticle(DriverService driverService, JavaScriptService javaScriptService, BrowserService browserService,
                        ComponentWaitService componentWaitService, WebSettings webSettings, ApplicationContext applicationContext,
                        WaitStrategyFactory waitStrategyFactory) {
        super(driverService, javaScriptService, browserService, componentWaitService, webSettings, applicationContext,
                waitStrategyFactory);
    }

    public Heading headingText() {
        return createByDataTestId(Heading.class, EVENT_CARD_NAME);
    }

    public Span county() {
        return createByDataTestId(Span.class, EVENT_CARD_COUNTY);
    }

    public List<Span> TypeLabels() {
        return createAllByDataTestId(Span.class, EVENT_CARD_TYPE);
    }

    public Div AddressText() {
        return createByDataTestId(Div.class, EVENT_CARD_ADDRESS);
    }

    public Anchor AddressLink() {
        return createByDataTestId(Anchor.class, EVENT_CARD_ADDRESS_LINK);
    }

    public Div OrganiserText() {
        return createByDataTestId(Div.class, EVENT_CARD_ORGANISER);
    }

    public Anchor OrganiserLink() {
        return createByDataTestId(Anchor.class, EVENT_CARD_ORGANISER_LINK);
    }

    public Div price() {
        return createByDataTestId(Div.class, EVENT_CARD_PRICING);
    }

    public List<Anchor> socialMediaLinks() {
        return createAllByDataTestId(Anchor.class, SOCIAL_MEDIA_LINK);
    }

    // TODO: Add event schedule once refactored

    public Anchor moreInformationLink() {
        return createByDataTestId(Anchor.class, EVENT_CARD_BUTTON);
    }

}