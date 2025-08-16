package solutions.bjjeire.selenium.web.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {
        "solutions.bjjeire.selenium.web",
        "solutions.bjjeire.core",
        "solutions.bjjeire.cucumber"
})
@EnableConfigurationProperties({ WebSettings.class, UrlSettings.class, ApiSettings.class })
@Import(WebClientConfig.class)
public class SeleniumConfig {



}
    