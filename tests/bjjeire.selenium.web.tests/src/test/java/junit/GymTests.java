package junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;
import solutions.bjjeire.selenium.web.infrastructure.Browser;
import solutions.bjjeire.selenium.web.infrastructure.ExecutionBrowser;
import solutions.bjjeire.selenium.web.infrastructure.Lifecycle;
import solutions.bjjeire.selenium.web.infrastructure.junit.JunitWebTest;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.CookiesService;

@ExecutionBrowser(browser = Browser.FIREFOX, lifecycle = Lifecycle.REUSE_IF_STARTED)
public class GymTests extends JunitWebTest {

    @Autowired
    private CookiesService cookiesService;

    @Autowired
    private BrowserService browserService;

    @Autowired
    private GymsPage gymsPage;

    @Override
    protected void afterEach() {
        browserService.clearLocalStorage();
        browserService.clearSessionStorage();
        cookiesService.deleteAllCookies();
    }

    @DisplayName("Filter gyms by county")
    @ParameterizedTest(name = "Should show gyms only for the county: {0}")
    @ValueSource(strings = {"Cork", "Dublin"})
    public void filterByCounty_shouldShowOnlyGymsForSelectedCounty(String county) {

        gymsPage.open();
        gymsPage.selectCounty(county)
                .assertAllGymsMatchCountyFilter(county);
    }

    @DisplayName("No gyms found for a given county")
    @ParameterizedTest(name = "Should show 'no gyms' message for county: {0}")
    @ValueSource(strings = {"Clare", "Wexford"})
    public void filterByCountyWithNoGyms_shouldShowNoGymsMessage(String countyWithNoGyms) {

        gymsPage.open();
        gymsPage.selectCounty(countyWithNoGyms)
                .assertNoDataInList();
    }


}