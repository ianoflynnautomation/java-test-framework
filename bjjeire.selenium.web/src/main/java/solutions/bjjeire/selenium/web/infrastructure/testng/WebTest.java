package solutions.bjjeire.selenium.web.infrastructure.testng;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import solutions.bjjeire.core.plugins.testng.BaseTest;
import solutions.bjjeire.selenium.web.configuration.SeleniumConfig;

@ContextConfiguration(classes = SeleniumConfig.class)
@TestPropertySource("classpath:application.properties")
public abstract class WebTest extends BaseTest {

  @Override
  protected void configure() {
    super.configure();
  }
}
