package solutions.bjjeire.cucumber.configuration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import solutions.bjjeire.selenium.web.configuration.SeleniumConfig;

@CucumberContextConfiguration
@ContextConfiguration(classes = SeleniumConfig.class)
@TestPropertySource("classpath:application.properties")
public class CucumberSpringConfiguration {
}
