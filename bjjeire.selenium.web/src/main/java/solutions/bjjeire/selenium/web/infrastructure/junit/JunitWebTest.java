package solutions.bjjeire.selenium.web.infrastructure.junit;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import solutions.bjjeire.core.plugins.junit.JunitBaseTest;
import solutions.bjjeire.selenium.web.configuration.SeleniumConfig;
import solutions.bjjeire.selenium.web.plugins.BrowserLifecyclePlugin;

@ActiveProfiles("development")
@SpringBootTest(classes = SeleniumConfig.class)
public abstract class JunitWebTest extends JunitBaseTest {

    @Override
    protected void configure() {
        super.configure();
        addPlugin(BrowserLifecyclePlugin.class);
    }
}