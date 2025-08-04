package solutions.bjjeire.selenium.web.components.custom.gym;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import solutions.bjjeire.selenium.web.components.Anchor;
import solutions.bjjeire.selenium.web.components.Div;
import solutions.bjjeire.selenium.web.components.Heading;
import solutions.bjjeire.selenium.web.components.Paragraph;
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
public class GymArticle extends WebComponent {

    public GymArticle(DriverService driverService, JavaScriptService javaScriptService, BrowserService browserService,
            ComponentWaitService componentWaitService, WebSettings webSettings, ApplicationContext applicationContext,
            WaitStrategyFactory waitStrategyFactory) {
        super(driverService, javaScriptService, browserService, componentWaitService, webSettings, applicationContext,
                waitStrategyFactory);
    }

    public Heading headingText() {
        return createByDataTestId(Heading.class, "gym-card-name");
    }

    public Paragraph county() {
        return createByDataTestId(Paragraph.class, "gym-card-county");
    }

    public Div AddressText() {
        return createByDataTestId(Div.class, "gym-card-address");
    }

    public Anchor AddressLink() {
        return createByDataTestId(Anchor.class, "gym-card-address-link");
    }

    public Div Affiliation() {
        return createByDataTestId(Div.class, "gym-card-affiliation");
    }

    public Anchor AffiliationLink() {
        return createByDataTestId(Anchor.class, "gym-card-affiliation-link");
    }

    public Div timeTable() {
        return createByDataTestId(Div.class, "gym-card-timetable");
    }

    public Anchor timeTableLink() {
        return createByDataTestId(Anchor.class, "gym-card-timetable-link");
    }

    public List<Span> offeredClasses() {
        return createAllByDataTestId(Span.class, "gym-card-classes-item");
    }

    public List<Anchor> socialMediaLinks() {
        return createAllByDataTestId(Anchor.class, "social-media-link");
    }

    public Anchor visitWebsiteLink() {
        return createByDataTestId(Anchor.class, "gym-card-website-link");
    }

}
