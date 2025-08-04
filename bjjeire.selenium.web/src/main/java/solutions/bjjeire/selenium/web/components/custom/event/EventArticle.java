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
        return createByDataTestId(Heading.class, "event-card-name");
    }

    public Span county() {
        return createByDataTestId(Span.class, "event-card-county");
    }

    public List<Span> TypeLabels() {
        return createAllByDataTestId(Span.class, "event-card-type");
    }

    public Div AddressText() {
        return createByDataTestId(Div.class, "event-card-address");
    }

    public Anchor AddressLink() {
        return createByDataTestId(Anchor.class, "event-card-address-link");
    }

    public Div OrganiserText() {
        return createByDataTestId(Div.class, "event-card-organiser");
    }

    public Anchor OrganiserLink() {
        return createByDataTestId(Anchor.class, "event-card-organiser-link");
    }

    public Div price() {
        return createByDataTestId(Div.class, "event-card-pricing");
    }

    public List<Anchor> socialMediaLinks() {
        return createAllByDataTestId(Anchor.class, "social-media-link");
    }

    // TODO: Add event schedule once refactored

    public Anchor moreInformationLink() {
        return createByDataTestId(Anchor.class, "event-card-button");
    }

}
