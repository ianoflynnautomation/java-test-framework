package solutions.bjjeire.api.configuration;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import solutions.bjjeire.api.client.RequestBodyHandler;
import solutions.bjjeire.api.client.RequestExecutor;
import solutions.bjjeire.api.client.WebClientAdapter;
import solutions.bjjeire.api.telemetry.MetricsCollector;
import solutions.bjjeire.api.telemetry.TracingManager;
import solutions.bjjeire.api.utils.CorrelationIdFilter;
import solutions.bjjeire.api.utils.RetryPolicy;

@Configuration
@ComponentScan(basePackages = "solutions.bjjeire")
@EnableConfigurationProperties(ApiSettings.class)
public class TestConfiguration {

    @Bean
    public Faker faker() {
        return new Faker();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(new JavaTimeModule())
                .build();
    }

    @Bean
    public OpenTelemetry openTelemetry(ApiSettings apiSettings) {
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        ResourceAttributes.SERVICE_NAME, apiSettings.getServiceName(),
                        ResourceAttributes.DEPLOYMENT_ENVIRONMENT, apiSettings.getEnvironment(),
                        ResourceAttributes.SERVICE_VERSION, "1.0-SNAPSHOT"
                )));

        // Create a tracer provider with the resource
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .build();

        // Build OpenTelemetry with the tracer provider
        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry(); // Or use Micrometer's Prometheus registry if needed
    }


    @Bean
    public MetricsCollector metricsCollector(MeterRegistry meterRegistry, ApiSettings apiSettings) {
        return new MetricsCollector(meterRegistry, apiSettings.getServiceName(), apiSettings.getEnvironment());
    }

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    public WebClient webClient(WebClientConfig webClientConfig) {
        return webClientConfig.buildWebClient(WebClient.builder());
    }


    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(TestConfiguration.class.getName());
    }
    @Bean
    public WebClientConfig webClientConfig(ApiSettings apiSettings, ObjectMapper objectMapper) {
        return new WebClientConfig(apiSettings, objectMapper);
    }

    @Bean
    public RetryPolicy retryPolicy(ApiSettings apiSettings, MeterRegistry meterRegistry) {
        return new RetryPolicy(apiSettings, meterRegistry);
    }

    @Bean
    public TracingManager tracingManager() {
        return new TracingManager();
    }

    @Bean
    public RequestBodyHandler requestBodyHandler(ObjectMapper objectMapper) {
        return new RequestBodyHandler(objectMapper);
    }

    @Bean
    public RequestExecutor requestExecutor(WebClient webClient, RetryPolicy retryPolicy,
                                           MetricsCollector metricsCollector, RequestBodyHandler bodyHandler) {
        return new RequestExecutor(webClient, retryPolicy, metricsCollector, bodyHandler);
    }

    @Bean
    public WebClientAdapter webClientAdapter(ApiSettings apiSettings, WebClientConfig webClientConfig,
                                             RequestExecutor requestExecutor) {
        return new WebClientAdapter(apiSettings, webClientConfig, requestExecutor);
    }
}