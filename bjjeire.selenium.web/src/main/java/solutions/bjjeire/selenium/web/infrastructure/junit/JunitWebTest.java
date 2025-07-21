package solutions.bjjeire.selenium.web.infrastructure.junit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import solutions.bjjeire.selenium.web.configuration.SeleniumConfig;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;
import solutions.bjjeire.selenium.web.pages.WebPage;
import solutions.bjjeire.core.plugins.junit.JunitBaseTest;

/**
 * A Spring-enabled base class for all Selenium web tests.
 * It automatically loads the Spring context, injects the WebDriver,
 * and handles browser teardown after each test.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SeleniumConfig.class)
public abstract class JunitWebTest extends JunitBaseTest {

    @Autowired
    protected DriverService driverService;

    @Autowired
    protected ApplicationContext applicationContext;

    protected WebDriver getDriver() {
        return driverService.getWrappedDriver();
    }


    protected <T extends WebPage> T goTo(Class<T> pageClass) {
        T page = applicationContext.getBean(pageClass);
        page.open();
        return page;
    }

}
