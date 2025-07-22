package solutions.bjjeire.selenium.web.infrastructure.testng;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import solutions.bjjeire.core.plugins.testng.BaseTest;
import solutions.bjjeire.selenium.web.configuration.SeleniumConfig;
import solutions.bjjeire.selenium.web.services.App;

/**
 * A specific base class for web tests using TestNG.
 * It extends the generic BaseTest and provides the Selenium-specific context.
 */
@ContextConfiguration(classes = SeleniumConfig.class)
public abstract class WebTest extends BaseTest {

    @Autowired
    protected App app;

    @Override
    protected void configure() {
    }
}