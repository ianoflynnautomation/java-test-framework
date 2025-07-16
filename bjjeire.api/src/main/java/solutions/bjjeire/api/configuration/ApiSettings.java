package solutions.bjjeire.api.configuration;

import lombok.Data;

@Data
public class ApiSettings {

    private String baseUrl;

    private int clientTimeoutSeconds = 30;

    private int maxRetryAttempts = 3;

    private long pauseBetweenFailuresMillis = 1000;

    ApiSettings() {
    }
}