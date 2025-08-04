package solutions.bjjeire.selenium.web.infrastructure.junit;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import solutions.bjjeire.core.plugins.junit.JunitBaseTest;
import solutions.bjjeire.selenium.web.configuration.SeleniumConfig;
import solutions.bjjeire.selenium.web.plugins.BrowserLifecyclePlugin;

/**
 * A Spring-enabled base class for all Selenium web tests using JUnit 5.
 * It extends the generic JunitBaseTest and provides the Selenium-specific
 * context.
 */
@ActiveProfiles("development")
@SpringBootTest(classes = SeleniumConfig.class)
public abstract class JunitWebTest extends JunitBaseTest {

    @Override
    protected void configure() {
        super.configure();
        addPlugin(BrowserLifecyclePlugin.class);
    }
}