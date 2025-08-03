package solutions.bjjeire.api.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "api-settings")
@Getter
@Setter
@Validated
public class ApiSettings {

    @NotBlank(message = "Base URL must not be blank")
    private String baseUrl;

    @Positive(message = "Connect timeout must be positive")
    private int connectTimeoutMillis = 5000;

    @Positive(message = "Response timeout must be positive")
    private int responseTimeoutMillis = 10000;

    @PositiveOrZero(message = "Max retry attempts must be zero or positive")
    private int maxRetryAttempts = 2;

    @PositiveOrZero(message = "Pause between failures must be zero or positive")
    private long pauseBetweenFailuresMillis = 1000;

    @NotBlank(message = "Tracing exporter must not be blank")
    private String tracingExporter = "otlp";

    private String jaegerEndpoint = "http://jaeger:4318";

    @NotBlank(message = "OTLP log endpoint must not be blank")
    private String otlpLogEndpoint = "http://otel-collector:4317";
}