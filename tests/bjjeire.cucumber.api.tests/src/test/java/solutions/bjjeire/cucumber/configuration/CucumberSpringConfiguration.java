package solutions.bjjeire.cucumber.configuration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import solutions.bjjeire.api.config.TestConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = TestConfiguration.class)
public class CucumberSpringConfiguration {
  
}
