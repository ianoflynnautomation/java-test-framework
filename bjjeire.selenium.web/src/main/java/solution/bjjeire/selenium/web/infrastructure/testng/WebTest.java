package solution.bjjeire.selenium.web.infrastructure.testng;

import solution.bjjeire.selenium.web.infrastructure.BrowserLifecyclePlugin;
import solution.bjjeire.selenium.web.services.App;
import solutions.bjjeire.core.plugins.testng.BaseTest;
import solutions.bjjeire.core.utilities.SingletonFactory;

public class WebTest extends BaseTest {

    public App app() {
        return SingletonFactory.getInstance(App.class);
    }

   @Override
    protected void configure() {
       addPlugin(BrowserLifecyclePlugin.class);
    }
}
