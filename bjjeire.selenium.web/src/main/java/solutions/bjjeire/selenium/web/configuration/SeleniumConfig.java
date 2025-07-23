package solutions.bjjeire.selenium.web.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Simplified Spring configuration.
 * It no longer needs to enable properties explicitly, as @Component on the
 * properties classes combined with @ComponentScan is sufficient.
 * Spring Boot will automatically find and register them.
 */
@Configuration
@ComponentScan(basePackages = {
        "solutions.bjjeire.selenium.web",
        "solutions.bjjeire.core",
        "solutions.bjjeire.cucumber"
})
@EnableConfigurationProperties({WebSettings.class, UrlSettings.class})
public class SeleniumConfig {

}
