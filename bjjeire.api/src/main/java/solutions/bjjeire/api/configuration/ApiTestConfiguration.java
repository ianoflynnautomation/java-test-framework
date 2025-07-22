package solutions.bjjeire.api.configuration;

import com.github.javafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import solutions.bjjeire.core.configuration.IConfigurationProvider;

/**
 * Spring configuration specifically for plain Java/JUnit API tests.
 * This class is kept separate from the Cucumber configuration to ensure
 * that JUnit tests do not depend on any BDD-specific setup or scopes.
 */
@Configuration
@ComponentScan(basePackages = "solutions.bjjeire")
@PropertySource("classpath:application.properties")
public class ApiTestConfiguration {

    /*
     * FIX: The IConfigurationProvider is now a @Component and will be picked up
     * automatically by the @ComponentScan. We no longer need to define it as a @Bean here.
     */

    @Bean
    public ApiSettings apiSettings(IConfigurationProvider provider) {
        ApiSettings settings = provider.getSettings(ApiSettings.class);
        if (settings == null || settings.getBaseUrl() == null || settings.getBaseUrl().isBlank()) {
            throw new IllegalStateException(
                    "baseUrl is not configured or is empty. Check your testSettings.<env>.json."
            );
        }
        return settings;
    }

    @Bean
    public Faker faker() {
        return new Faker();
    }
}