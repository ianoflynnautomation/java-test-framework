package solutions.bjjeire.api.configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;

@Component
@RequiredArgsConstructor
public class WebClientConfig {

        private final ApiSettings settings;
        private final ObjectMapper objectMapper;
        private final int BUFFER_SIZE = 16 * 1024 * 1024;

        public WebClient buildWebClient(WebClient.Builder webClientBuilder) {

                ExchangeStrategies strategies = ExchangeStrategies.builder()
                                .codecs(configurer -> {
                                        configurer.defaultCodecs().jackson2JsonEncoder(
                                                        new Jackson2JsonEncoder(objectMapper,
                                                                        MediaType.APPLICATION_JSON));
                                        configurer.defaultCodecs().jackson2JsonDecoder(
                                                        new Jackson2JsonDecoder(objectMapper,
                                                                        MediaType.APPLICATION_JSON));
                                        configurer.defaultCodecs().maxInMemorySize(BUFFER_SIZE);
                                })
                                .build();

                HttpClient httpClient = HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, settings.getConnectTimeoutMillis())
                                .responseTimeout(Duration.ofMillis(settings.getResponseTimeoutMillis()))
                                .doOnConnected(conn -> conn
                                                .addHandlerLast(new ReadTimeoutHandler(
                                                                settings.getResponseTimeoutMillis(),
                                                                TimeUnit.MILLISECONDS))
                                                .addHandlerLast(new WriteTimeoutHandler(
                                                                settings.getConnectTimeoutMillis(),
                                                                TimeUnit.MILLISECONDS)));

                return webClientBuilder
                                .baseUrl(settings.getBaseUrl())
                                .clientConnector(new ReactorClientHttpConnector(httpClient))
                                .exchangeStrategies(strategies)
                                .build();
        }
}