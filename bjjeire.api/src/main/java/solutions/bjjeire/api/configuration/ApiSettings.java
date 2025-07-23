package solutions.bjjeire.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "api-settings")
public class ApiSettings {

    private String baseUrl;

    private int clientTimeoutSeconds = 30;

    private int maxRetryAttempts = 3;

    private long pauseBetweenFailuresMillis = 1000;

    ApiSettings() {
    }
}