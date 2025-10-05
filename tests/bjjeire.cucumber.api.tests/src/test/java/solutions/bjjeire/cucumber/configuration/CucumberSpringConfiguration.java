package solutions.bjjeire.cucumber.configuration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import solutions.bjjeire.api.config.TestConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = TestConfiguration.class)
@TestPropertySource("classpath:application.properties")
public class CucumberSpringConfiguration {
  
}
