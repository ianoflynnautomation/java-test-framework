package solutions.bjjeire.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "api-settings")
public class ApiSettings {
    private String baseUrl;
    private int connectTimeoutMillis = 5000;
    private int responseTimeoutMillis = 10000;
    private int maxRetryAttempts = 2;
    private long pauseBetweenFailuresMillis = 1000;
}