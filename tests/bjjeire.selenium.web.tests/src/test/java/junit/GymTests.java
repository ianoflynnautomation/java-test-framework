package junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.gyms.GymFactory;
import solutions.bjjeire.core.plugins.Browser;
import solutions.bjjeire.core.plugins.Lifecycle;
import solutions.bjjeire.selenium.web.data.TestDataManager;
import solutions.bjjeire.selenium.web.infrastructure.ExecutionBrowser;
import solutions.bjjeire.selenium.web.infrastructure.junit.JunitWebTest;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;
import solutions.bjjeire.selenium.web.services.BrowserService;
import solutions.bjjeire.selenium.web.services.CookiesService;

@ExecutionBrowser(browser = Browser.FIREFOX, lifecycle = Lifecycle.REUSE_IF_STARTED)
public class GymTests extends JunitWebTest {

    private final CookiesService cookiesService;
    private final BrowserService browserService;
    private final GymsPage gymsPage;
    private final TestDataManager testDataManager;
    private String authToken;
    private final List<String> createdGymIds = new ArrayList<>();

    public GymTests(CookiesService cookiesService, BrowserService browserService, GymsPage gymsPage,
            TestDataManager testDataManager) {
        this.cookiesService = cookiesService;
        this.browserService = browserService;
        this.gymsPage = gymsPage;
        this.testDataManager = testDataManager;
    }

    @BeforeEach
    public void setup() {
        this.authToken = testDataManager.authenticate();
        this.createdGymIds.clear();
    }

    @Override
    protected void afterEach() {
        testDataManager.teardownGyms(this.createdGymIds, this.authToken);
        browserService.clearLocalStorage();
        browserService.clearSessionStorage();
        cookiesService.deleteAllCookies();
    }

    @DisplayName("Filter gyms by county")
    @ParameterizedTest(name = "Should show gyms only for the county: {0}")
    @ValueSource(strings = { "Cork", "Dublin" })
    public void filterByCounty_shouldShowOnlyGymsForSelectedCounty(String countyStr) {
        // Arrange
        County county = County.valueOf(countyStr);
        createdGymIds.addAll(testDataManager.seedGyms(List.of(
                GymFactory.createGym(b -> b.county(county).name(countyStr + " Gym 1")),
                GymFactory.createGym(b -> b.county(county).name(countyStr + " Gym 2")),
                GymFactory.createGym(b -> b.county(County.Kildare).name("Kildare Gym"))), authToken));

        // Act & Assert
        gymsPage.open();
        gymsPage.selectCounty(countyStr)
                .assertAllGymsMatchCountyFilter(countyStr);
    }

    @DisplayName("No gyms found for a given county")
    @ParameterizedTest(name = "Should show 'no gyms' message for county: {0}")
    @ValueSource(strings = { "Clare", "Wexford" })
    public void filterByCountyWithNoGyms_shouldShowNoGymsMessage(String countyWithNoGyms) {
        // Arrange
        createdGymIds.addAll(testDataManager.seedGyms(List.of(
                GymFactory.createGym(b -> b.county(County.Dublin).name("Dublin Gym"))), authToken));

        // Act & Assert
        gymsPage.open();
        gymsPage.selectCounty(countyWithNoGyms)
                .assertNoDataInList();
    }

    @Test
    @DisplayName("Should show empty list when no gyms exist in the system")
    public void filterWhenNoGymsExist_shouldShowEmptyList() {

        // Act & Assert
        gymsPage.open();
        gymsPage.selectCounty("Wexford")
                .assertNoDataInList();
    }
}