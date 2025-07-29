package solutions.bjjeire.api.http;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import solutions.bjjeire.api.configuration.ApiSettings;
import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Component
public class ApiClient {

    private final WebClient webClient;
    private final ApiSettings settings;
    private final ObjectMapper objectMapper;

    public ApiClient(ApiSettings settings) {
        this.settings = settings;

        this.objectMapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .build();

        final int size = 16 * 1024 * 1024;

        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(this.objectMapper, MediaType.APPLICATION_JSON)
                    );
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(this.objectMapper, MediaType.APPLICATION_JSON)
                    );
                    configurer.defaultCodecs().maxInMemorySize(size);
                })
                .build();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, settings.getConnectTimeoutMillis())
                .responseTimeout(Duration.ofMillis(settings.getResponseTimeoutMillis()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(settings.getResponseTimeoutMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(settings.getResponseTimeoutMillis(), TimeUnit.MILLISECONDS)));

        this.webClient = WebClient.builder()
                .baseUrl(settings.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }

    public WebClient getWebClient() {
        return webClient;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public ApiSettings getSettings() {
        return settings;
    }
}