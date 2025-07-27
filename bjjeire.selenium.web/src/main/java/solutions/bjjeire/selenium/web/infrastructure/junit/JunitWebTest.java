package solutions.bjjeire.selenium.web.infrastructure.junit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import solutions.bjjeire.core.plugins.junit.JunitBaseTest;
import solutions.bjjeire.selenium.web.configuration.SeleniumConfig;
import solutions.bjjeire.selenium.web.plugins.BrowserLifecyclePlugin;
import solutions.bjjeire.selenium.web.services.DriverService;
import solutions.bjjeire.selenium.web.pages.WebPage;

/**
 * A Spring-enabled base class for all Selenium web tests using JUnit 5.
 * It extends the generic JunitBaseTest and provides the Selenium-specific
 * context.
 */
@ActiveProfiles("development")
@SpringBootTest(classes = SeleniumConfig.class)
public abstract class JunitWebTest extends JunitBaseTest {

    @Autowired
    protected DriverService driverService;

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    protected void configure() {
        super.configure();
        addPlugin(BrowserLifecyclePlugin.class);
    }

    protected <T extends WebPage> T goTo(Class<T> pageClass) {
        T page = applicationContext.getBean(pageClass);
        page.open();
        return page;
    }
}