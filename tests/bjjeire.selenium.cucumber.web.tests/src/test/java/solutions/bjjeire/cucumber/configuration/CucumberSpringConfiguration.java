package solutions.bjjeire.cucumber.configuration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import solutions.bjjeire.selenium.web.configuration.SeleniumConfig;

@CucumberContextConfiguration
@SpringBootTest(classes = SeleniumConfig.class)
public class CucumberSpringConfiguration {
}
