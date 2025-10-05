package solutions.bjjeire.selenium.web.configuration;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@ConfigurationProperties(prefix = "url-settings")
@Data
@Validated
public class UrlSettings {

    @NotBlank(message = "Event URL cannot be blank.")
    @URL(message = "Event URL must be a valid URL.")
    private String eventUrl;

    @NotBlank(message = "Gym URL cannot be blank.")
    @URL(message = "Gym URL must be a valid URL.")
    private String gymUrl;
}