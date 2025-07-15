package junit;

import org.junit.jupiter.api.Test;
import solution.bjjeire.selenium.web.infrastructure.Browser;
import solution.bjjeire.selenium.web.infrastructure.ExecutionBrowser;
import solution.bjjeire.selenium.web.infrastructure.Lifecycle;
import solution.bjjeire.selenium.web.infrastructure.junit.JunitWebTest;

@ExecutionBrowser(browser = Browser.FIREFOX, lifecycle = Lifecycle.RESTART_EVERY_TIME)
public class GymTests extends JunitWebTest {

    @Override
    protected void afterEach() {
        app().cookies().deleteAllCookies();
    }


    @Test
    public void JunitSearchGymsUserJourney()
    {
        app().navigate().to("https://localhost:60743/gyms");
        app().navigate().waitForPartialUrl("/gyms");
    }
}
