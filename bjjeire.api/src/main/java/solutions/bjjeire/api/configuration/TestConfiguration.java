package solutions.bjjeire.api.configuration;

import com.github.javafaker.Faker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "solutions.bjjeire")
@EnableConfigurationProperties(ApiSettings.class)
public class TestConfiguration {

    @Bean
    public Faker faker() {
        return new Faker();
    }
}