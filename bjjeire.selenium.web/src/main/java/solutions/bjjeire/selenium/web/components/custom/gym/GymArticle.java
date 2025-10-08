package solutions.bjjeire.selenium.web.components.custom.gym;

import static solutions.bjjeire.selenium.web.utils.GymsPageDataTestIds.*;

import java.util.List;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
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
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GymArticle extends WebComponent {

  public GymArticle(
      DriverService driverService,
      JavaScriptService javaScriptService,
      BrowserService browserService,
      ComponentWaitService componentWaitService,
      WebSettings webSettings,
      ApplicationContext applicationContext,
      WaitStrategyFactory waitStrategyFactory) {
    super(
        driverService,
        javaScriptService,
        browserService,
        componentWaitService,
        webSettings,
        applicationContext,
        waitStrategyFactory);
  }

  public Heading headingText() {
    return createByDataTestId(Heading.class, GYM_CARD_NAME);
  }

  public Paragraph county() {
    return createByDataTestId(Paragraph.class, GYM_CARD_COUNTY);
  }

  public Div AddressText() {
    return createByDataTestId(Div.class, GYM_CARD_ADDRESS);
  }

  public Anchor AddressLink() {
    return createByDataTestId(Anchor.class, GYM_CARD_ADDRESS_LINK);
  }

  public Div Affiliation() {
    return createByDataTestId(Div.class, GYM_CARD_AFFILIATION);
  }

  public Anchor AffiliationLink() {
    return createByDataTestId(Anchor.class, GYM_CARD_AFFILIATION_LINK);
  }

  public Div timeTable() {
    return createByDataTestId(Div.class, GYM_CARD_TIMETABLE);
  }

  public Anchor timeTableLink() {
    return createByDataTestId(Anchor.class, GYM_CARD_TIMETABLE_LINK);
  }

  public List<Span> offeredClasses() {
    return createAllByDataTestId(Span.class, GYM_CARD_CLASSES_ITEM);
  }

  public List<Anchor> socialMediaLinks() {
    return createAllByDataTestId(Anchor.class, SOCIAL_MEDIA_LINK);
  }

  public Anchor visitWebsiteLink() {
    return createByDataTestId(Anchor.class, GYM_CARD_WEBSITE_LINK);
  }
}
