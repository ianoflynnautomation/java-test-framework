package solutions.bjjeire.selenium.web.configuration;

import io.cucumber.java.ParameterType;
import org.springframework.context.annotation.*;
import solutions.bjjeire.core.configuration.IConfigurationProvider;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;
import solutions.bjjeire.selenium.web.pages.events.data.BjjEventType;


/**
 * A production-ready Spring configuration for the Selenium test framework.
 * It integrates the advanced DriverService logic and external JSON settings
 * to provide a flexible and powerful WebDriver management system.
 */
@Configuration
@ComponentScan(basePackages = {
        "solutions.bjjeire.selenium.web",
        "solutions.bjjeire.core",
})
@PropertySource("classpath:application.properties")
public class SeleniumConfig {

    @Bean
    public WebSettings webSettings(IConfigurationProvider provider) {
        return provider.getSettings(WebSettings.class);
    }

    @Bean
    public UrlSettings urlSettings(IConfigurationProvider provider) {
        return provider.getSettings(UrlSettings.class);
    }

    @Bean
    public DriverService driverService(WebSettings webSettings) {
        return new DriverService(webSettings);
    }


}