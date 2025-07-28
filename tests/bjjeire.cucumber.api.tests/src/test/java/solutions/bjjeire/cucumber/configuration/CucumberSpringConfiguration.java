package solutions.bjjeire.cucumber.configuration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import solutions.bjjeire.api.configuration.TestConfiguration;

/**
 * This class provides the central Spring configuration for all Cucumber tests.
 * Cucumber will automatically discover this class because of the @CucumberContextConfiguration
 * annotation and use it to bootstrap the Spring application context.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = TestConfiguration.class)
public class CucumberSpringConfiguration {
    // This class remains empty. Its purpose is to hold the configuration annotations.
}
