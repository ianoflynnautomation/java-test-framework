package solutions.bjjeire.selenium.web.configuration;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import solutions.bjjeire.api.config.ApiSettings;

@Configuration
@ComponentScan(basePackages = {
        "solutions.bjjeire.selenium.web",
        "solutions.bjjeire.core",
        "solutions.bjjeire.cucumber",
        "solutions.bjjeire.api"
})
//@EnableConfigurationProperties({
//        WebSettings.class,
//        UrlSettings.class,
//        ApiSettings.class,
//       // TestUsersConfig.class
//})
@ConfigurationPropertiesScan("solutions.bjjeire")
public class SeleniumConfig {

}