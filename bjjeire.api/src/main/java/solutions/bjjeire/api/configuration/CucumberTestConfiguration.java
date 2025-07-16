package solutions.bjjeire.api.configuration;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import solutions.bjjeire.core.configuration.EnvironmentConfigurationProvider;
import solutions.bjjeire.core.configuration.IConfigurationProvider;

/**
 * Main Spring configuration class specifically for the BDD/Cucumber test suite.
 * It enables component scanning and defines beans that require explicit creation.
 * This configuration is used by the Cucumber Spring integration.
 */
@Configuration
@ComponentScan(basePackages = "solutions.bjjeire")
@PropertySource("classpath:application.properties")
public class CucumberTestConfiguration {

    /**
     * Creates a singleton bean for the configuration provider.
     * @param environment The active environment (e.g., "development"), injected from application.properties.
     * @return An instance of IConfigurationProvider.
     */
    @Bean
    public IConfigurationProvider configurationProvider(@Value("${environment:development}") String environment) {
        return new EnvironmentConfigurationProvider(environment);
    }

    /**
     * Creates the ApiSettings bean using the configuration provider.
     * @param provider The configuration provider bean.
     * @return A configured ApiSettings instance.
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

    /**
     * Provides a singleton Faker instance for generating test data.
     * @return A Faker instance.
     */
    @Bean
    public Faker faker() {
        return new Faker();
    }
}
