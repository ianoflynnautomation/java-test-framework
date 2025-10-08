package solutions.bjjeire.selenium.web.infrastructure.junit;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import solutions.bjjeire.core.plugins.junit.JunitBaseTest;
import solutions.bjjeire.selenium.web.configuration.SeleniumConfig;
import solutions.bjjeire.selenium.web.plugins.BrowserLifecyclePlugin;

@ActiveProfiles("development")
@ContextConfiguration(classes = SeleniumConfig.class)
@TestPropertySource("classpath:application.properties")
public abstract class JunitWebTest extends JunitBaseTest {

  @Override
  protected void configure() {
    super.configure();
    addPlugin(BrowserLifecyclePlugin.class);
  }
}
