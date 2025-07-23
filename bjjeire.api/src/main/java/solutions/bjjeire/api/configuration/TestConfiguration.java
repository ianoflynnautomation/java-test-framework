package solutions.bjjeire.api.configuration;

import com.github.javafaker.Faker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Unified Spring configuration for all tests (JUnit, Cucumber, etc.).
 * This class enables component scanning to find all Spring beans, including the
 * @ConfigurationProperties-annotated ApiSettings class.
 */
@Configuration
@ComponentScan(basePackages = "solutions.bjjeire")
@EnableConfigurationProperties(ApiSettings.class)
public class TestConfiguration {

    /**
     * Provides a Faker instance for generating realistic test data.
     * This is defined as a bean so it can be injected across the test suite.
     * @return A singleton Faker instance.
     */
    @Bean
    public Faker faker() {
        return new Faker();
    }
}