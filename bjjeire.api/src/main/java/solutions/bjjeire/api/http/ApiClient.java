package solutions.bjjeire.api.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import solutions.bjjeire.api.configuration.ApiSettings;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);

    /**
     * Creates a singleton, thread-safe WebClient bean.
     * This client is configured once with timeouts, logging, and codecs, then injected where needed.
     * This approach avoids redundant client instantiation and ensures consistent behavior.
     *
     * @param settings     The centralized API configuration.
     * @param objectMapper The shared Jackson ObjectMapper.
     * @return A fully configured, reusable WebClient instance.
     */
    @Bean
    public WebClient webClient(ApiSettings settings, ObjectMapper objectMapper) {
        final int bufferSize = 16 * 1024 * 1024; // 16MB buffer

        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON)
                    );
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON)
                    );
                    configurer.defaultCodecs().maxInMemorySize(bufferSize);
                })
                .build();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, settings.getConnectTimeoutMillis())
                .responseTimeout(Duration.ofMillis(settings.getResponseTimeoutMillis()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(settings.getResponseTimeoutMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(settings.getConnectTimeoutMillis(), TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(settings.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .filter(loggingFilter())
                .build();
    }

    /**
     * An ExchangeFilterFunction to log request and response details.
     * This acts as a non-intrusive interceptor, useful for debugging in a CI/CD environment.
     *
     * @return A WebClient filter for logging.
     */
    private ExchangeFilterFunction loggingFilter() {
        return (clientRequest, next) -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.debug("  -> Header: {}={}", name, value)));
            return next.exchange(clientRequest)
                    .doOnNext(response -> log.info("Response: Status {}", response.statusCode().value()))
                    .doOnError(error -> log.error("Response: Error", error));
        };
    }
}