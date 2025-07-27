package solutions.bjjeire.selenium.web.pages.gyms;

import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import solutions.bjjeire.selenium.web.components.Heading;
import solutions.bjjeire.selenium.web.components.Label;
import solutions.bjjeire.selenium.web.components.Select;
import solutions.bjjeire.selenium.web.components.custom.gym.GymArticle;
import solutions.bjjeire.selenium.web.configuration.UrlSettings;
import solutions.bjjeire.selenium.web.configuration.WebSettings;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.pages.ListPageBase;
import solutions.bjjeire.selenium.web.pages.gyms.data.GymCardDetails;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.ComponentCreateService;
import solutions.bjjeire.selenium.web.services.ComponentWaitService;
import solutions.bjjeire.selenium.web.services.JavaScriptService;
import solutions.bjjeire.selenium.web.services.NavigationService;
import solutions.bjjeire.selenium.web.waitstrategies.WaitStrategyFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
@Scope("prototype")
public class GymsPage extends ListPageBase {

    private final UrlSettings urlSettings;

    @Autowired
    public GymsPage(DriverService driverService, JavaScriptService javaScriptService, BrowserService browserService,
            ComponentWaitService componentWaitService, WebSettings webSettings, ApplicationContext applicationContext,
            WaitStrategyFactory waitStrategyFactory, NavigationService navigationService,
            ComponentCreateService componentCreateService, UrlSettings urlSettings) {
        super(driverService, javaScriptService, browserService, componentWaitService, webSettings, applicationContext,
                waitStrategyFactory, navigationService, componentCreateService);
        this.urlSettings = urlSettings;
    }

    @Override
    protected String getUrl() {
        return urlSettings.getGymUrl();
    }

    public Heading titleText() {
        return create().byDataTestId(Heading.class, "gyms-page-header-title");
    }

    public Label gymsListTotalText() {
        return create().byDataTestId(Label.class, "gyms-page-header-total");
    }

    private Select countyDropdown() {
        return create().byDataTestId(Select.class, "select-filter-select");
    }

    private List<GymArticle> gymCards() {
        return create().allByDataTestId(GymArticle.class, "gyms-list-item");
    }

    public GymsPage selectCounty(String county) {
        countyDropdown().selectByText(county);
        return this;
    }

    public GymsPage assertAllGymsMatchCountyFilter(String expectedCounty) {
        List<GymArticle> cards = gymCards();
        assertFalse(cards.isEmpty(), "Expected to find gyms cards after filtering, but none were found.");

        for (GymArticle card : cards) {
            String actualCounty = card.county().getText();
            assertTrue(actualCounty.toLowerCase().contains(expectedCounty.toLowerCase()),
                    String.format("Gym card '%s' should have county '%s' but was '%s'", card.headingText().getText(),
                            expectedCounty, actualCounty));
        }
        return this;
    }

    public GymsPage assertTotalGymsFoundInList(Integer expectedGymsTotal) {
        switch (expectedGymsTotal) {
            case 0:
                break;
            case 1:
                gymsListTotalText().validateTextIs(String.format("Found %d gym.", expectedGymsTotal));
                break;
            default:
                gymsListTotalText().validateTextIs(String.format("Found %d gyms.", expectedGymsTotal));
        }
        return this;
    }

    public GymsPage assertGymIsInList(GymCardDetails gymCard) {
        GymArticle cardToAssert = gymCards().stream()
                .filter(card -> card.headingText().getText().trim().equals(gymCard.name()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Could not find an gym card with the name: " + gymCard.name()));

        cardToAssert.headingText().validateTextIs(gymCard.name());

        return this;
    }

}
