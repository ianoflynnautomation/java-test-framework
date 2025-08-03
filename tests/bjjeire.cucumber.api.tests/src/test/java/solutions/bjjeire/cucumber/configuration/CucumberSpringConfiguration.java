package solutions.bjjeire.cucumber.configuration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import solutions.bjjeire.api.configuration.TestConfiguration;
import solutions.bjjeire.api.utils.TestLifecycleLogger;

@CucumberContextConfiguration
@SpringBootTest(classes = TestConfiguration.class)
@ExtendWith(TestLifecycleLogger.class)
public class CucumberSpringConfiguration {
  
}
