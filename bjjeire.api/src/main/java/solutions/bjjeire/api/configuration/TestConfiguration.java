package solutions.bjjeire.api.configuration;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import solutions.bjjeire.api.client.RequestBodyHandler;
import solutions.bjjeire.api.client.RequestExecutor;
import solutions.bjjeire.api.client.WebClientAdapter;
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
    public WebClient webClient(WebClientConfig webClientConfig) {
        return webClientConfig.buildWebClient(WebClient.builder());
    }


    @Bean
    public WebClientConfig webClientConfig(ApiSettings apiSettings, ObjectMapper objectMapper) {
        return new WebClientConfig(apiSettings, objectMapper);
    }

    @Bean
    public RetryPolicy retryPolicy(ApiSettings apiSettings) {
        return new RetryPolicy(apiSettings);
    }


    @Bean
    public RequestBodyHandler requestBodyHandler(ObjectMapper objectMapper) {
        return new RequestBodyHandler(objectMapper);
    }

    @Bean
    public RequestExecutor requestExecutor(WebClient webClient, RetryPolicy retryPolicy,
            RequestBodyHandler bodyHandler) {
        return new RequestExecutor(webClient, retryPolicy, bodyHandler);
    }

    @Bean
    public WebClientAdapter webClientAdapter(ApiSettings apiSettings, WebClientConfig webClientConfig,
            RequestExecutor requestExecutor) {
        return new WebClientAdapter(apiSettings, webClientConfig, requestExecutor);
    }
}