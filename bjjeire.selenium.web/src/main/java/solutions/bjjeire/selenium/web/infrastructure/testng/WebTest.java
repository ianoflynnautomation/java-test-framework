package solutions.bjjeire.selenium.web.infrastructure.testng;

import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.selenium.web.infrastructure.BrowserLifecyclePlugin;
import solutions.bjjeire.selenium.web.services.App;
import solutions.bjjeire.core.plugins.testng.BaseTest;

public class WebTest extends BaseTest {

    @Autowired
    protected App app;

   @Override
    protected void configure() {
       addPlugin(BrowserLifecyclePlugin.class);
    }
}
