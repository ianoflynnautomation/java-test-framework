package solutions.bjjeire.selenium.web.pages.gyms;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import solutions.bjjeire.selenium.web.components.Heading;
import solutions.bjjeire.selenium.web.components.Label;
import solutions.bjjeire.selenium.web.components.Paragraph;
import solutions.bjjeire.selenium.web.components.Select;
import solutions.bjjeire.selenium.web.components.custom.gym.GymArticle;
import solutions.bjjeire.selenium.web.configuration.UrlSettings;
import solutions.bjjeire.selenium.web.pages.ListPageBase;
import solutions.bjjeire.selenium.web.pages.gyms.data.GymCardDetails;
import solutions.bjjeire.selenium.web.services.ComponentCreateService;
import solutions.bjjeire.selenium.web.services.NavigationService;
import static solutions.bjjeire.selenium.web.utils.CommonDataTestIds.SELECT_FILTER;
import static solutions.bjjeire.selenium.web.utils.GymsPageDataTestIds.GYMS_LIST_ITEM;
import static solutions.bjjeire.selenium.web.utils.GymsPageDataTestIds.GYMS_PAGE_HEADER_TITLE;
import static solutions.bjjeire.selenium.web.utils.GymsPageDataTestIds.GYMS_PAGE_HEADER_TOTAL;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GymsPage extends ListPageBase {

    private final UrlSettings urlSettings;

    public GymsPage(NavigationService navigationService,
            ComponentCreateService componentCreateService,
            UrlSettings urlSettings) {
        super(navigationService, componentCreateService);
        this.urlSettings = urlSettings;
    }

    @Override
    protected String getUrl() {
        return urlSettings.getGymUrl();
    }

    public Heading titleText() {
        return create().byDataTestId(Heading.class, GYMS_PAGE_HEADER_TITLE);
    }

    public Label gymsListTotalText() {
        return create().byDataTestId(Label.class, GYMS_PAGE_HEADER_TOTAL);
    }

    private Select countyDropdown() {
        return create().byDataTestId(Select.class, SELECT_FILTER);
    }

    private List<GymArticle> gymCards() {
        return create().allByDataTestId(GymArticle.class, GYMS_LIST_ITEM);
    }

    public GymsPage selectCounty(String county) {
        countyDropdown().validateNotDisabled();
        countyDropdown().selectByText(county);
        return this;
    }

    public GymsPage assertAllGymsMatchCountyFilter(String expectedCounty) {
        List<GymArticle> cards = gymCards();
        assertFalse(cards.isEmpty(), "Expected to find gyms cards after filtering, but none were found.");

        List<String> mismatchedCounties = cards.stream()
                .map(GymArticle::county)
                .map(Paragraph::getText)
                .filter(actualCounty -> !actualCounty.toLowerCase().contains(expectedCounty.toLowerCase()))
                .toList();

        assertTrue(mismatchedCounties.isEmpty(),
                String.format("The following counties did not match the filter '%s': %s", expectedCounty,
                        mismatchedCounties));
        return this;
    }

    public GymsPage assertTotalGymsFoundInList(Integer expectedGymsTotal) {
        if (null == expectedGymsTotal) {
            gymsListTotalText().validateTextIs(String.format("Found %d gyms.", expectedGymsTotal));
        } else
            switch (expectedGymsTotal) {
                case 0 -> gymsListTotalText().validateTextIs("");
                case 1 -> gymsListTotalText().validateTextIs(String.format("Found %d gym.", expectedGymsTotal));
                default -> gymsListTotalText().validateTextIs(String.format("Found %d gyms.", expectedGymsTotal));
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