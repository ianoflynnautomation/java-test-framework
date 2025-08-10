package solutions.bjjeire.selenium.web.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Configuration
public class WebClientConfig {

        @Bean
        public ObjectMapper webClientObjectMapper() {
                return JsonMapper.builder()
                                .enable(SerializationFeature.INDENT_OUTPUT)
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                // .enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
                                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                                .addModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                                // .serializationInclusion(JsonInclude.Include.NON_NULL)
                                .build();
        }

        @Bean
        public WebClient.Builder webClientBuilder() {
                return WebClient.builder();
        }

        @Bean
        public WebClient webClient(WebClient.Builder builder,
                        ObjectMapper objectMapper,
                        @Value("${api.backend-url}") String baseUrl,
                        @Value("${api.buffer-size-mb:16}") int bufferSizeMb) {
                final int bufferSize = bufferSizeMb * 1024 * 1024; // 16MB

                ExchangeStrategies strategies = ExchangeStrategies.builder()
                                .codecs(clientDefaultCodecsConfigurer -> {
                                        clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(
                                                        new Jackson2JsonEncoder(objectMapper));
                                        clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(
                                                        new Jackson2JsonDecoder(objectMapper));
                                        clientDefaultCodecsConfigurer.defaultCodecs().maxInMemorySize(bufferSize);
                                })
                                .build();

                return builder
                                .baseUrl(baseUrl)
                                .exchangeStrategies(strategies)
                                .build();
        }
}