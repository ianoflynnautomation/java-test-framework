package solutions.bjjeire.selenium.web.configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@ConfigurationProperties(prefix = "api")
@Data
@Validated
public class ApiSettings {

    @NotBlank(message = "API backend URL cannot be blank.")
    private String backendUrl;

    @Positive(message = "Buffer size must be a positive number.")
    private int bufferSizeMb = 16;

    @Positive(message = "Timeout must be a positive number.")
    private int timeoutSeconds = 30;

    @NotNull
    private Admin admin;

}
