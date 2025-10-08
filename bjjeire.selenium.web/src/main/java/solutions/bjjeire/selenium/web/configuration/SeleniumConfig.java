package solutions.bjjeire.selenium.web.configuration;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
    basePackages = {
      "solutions.bjjeire.selenium.web",
      "solutions.bjjeire.core",
      "solutions.bjjeire.cucumber",
      "solutions.bjjeire.api"
    })
// @EnableConfigurationProperties({
//        WebSettings.class,
//        UrlSettings.class,
//        ApiSettings.class,
//       // TestUsersConfig.class
// })
@ConfigurationPropertiesScan("solutions.bjjeire")
public class SeleniumConfig {}
