package junit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.selenium.web.pages.gyms.GymsPage;
import solutions.bjjeire.selenium.web.infrastructure.Browser;
import solutions.bjjeire.selenium.web.infrastructure.ExecutionBrowser;
import solutions.bjjeire.selenium.web.infrastructure.Lifecycle;
import solutions.bjjeire.selenium.web.infrastructure.junit.JunitWebTest;
import solutions.bjjeire.selenium.web.pages.gyms.data.GymCardDetails;
import solutions.bjjeire.selenium.web.services.CookiesService;

@ExecutionBrowser(browser = Browser.FIREFOX, lifecycle = Lifecycle.RESTART_EVERY_TIME)
public class GymTests extends JunitWebTest {

    @Autowired
    private CookiesService cookiesService;

    @Autowired
    private GymsPage gymPage;

    @Override
    protected void afterEach() {
        cookiesService.deleteAllCookies();
    }

    @Test
    public void junitSearchGymsUserJourney() {

        GymCardDetails TEST_GYM = new GymCardDetails(
                "test",
                "Dublin",
                "Unit 5, Blanchardstown Corporate Park, Dublin 15 (Blanchardstown Training Centre)",
                "Affiliated with Gracie Barra",
                "View Timetable"
        );

        gymPage.open();
        gymPage.selectCounty("Dublin")
                .assertTotalGymsFoundInList(1)
                .assertGymIsInList(TEST_GYM);
    }
}