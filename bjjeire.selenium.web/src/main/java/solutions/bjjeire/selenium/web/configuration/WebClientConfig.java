package solutions.bjjeire.selenium.web.configuration;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public ObjectMapper webClientObjectMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .build();
    }

    /**
     * Creates a WebClient.Builder bean that can be injected elsewhere.
     * Spring Boot's auto-configuration normally provides this, but in a test context,
     * it's safer to define it explicitly.
     * @return A new instance of WebClient.Builder.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder, ObjectMapper objectMapper, @Value("${web-settings.backendApiUrl}") String baseUrl) {
        // Increase buffer size for potentially large API responses
        final int bufferSize = 16 * 1024 * 1024; // 16MB

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper)
                    );
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper)
                    );
                    clientDefaultCodecsConfigurer.defaultCodecs().maxInMemorySize(bufferSize);
                })
                .build();

        return builder
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .build();
    }
}