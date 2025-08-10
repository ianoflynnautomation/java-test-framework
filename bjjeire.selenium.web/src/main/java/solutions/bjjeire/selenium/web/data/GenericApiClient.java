package solutions.bjjeire.selenium.web.data;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenericApiClient {

        private final WebClient webClient;

        public <T_COMMAND, T_RESPONSE> Mono<T_RESPONSE> post(
                        String apiPath,
                        T_COMMAND command,
                        String authToken,
                        Class<T_RESPONSE> responseClass) {
                return webClient.post()
                                .uri(apiPath)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(command)
                                .retrieve()
                                .onStatus(status -> !status.is2xxSuccessful(),
                                                response -> response.bodyToMono(String.class)
                                                                .defaultIfEmpty("[Empty Response Body]")
                                                                .flatMap(body -> {
                                                                        log.error("API POST Error for path '{}'. Status: {}, Body: {}",
                                                                                        apiPath, response.statusCode(),
                                                                                        body);
                                                                        return Mono.error(new IllegalStateException(
                                                                                        "API call failed for " + apiPath
                                                                                                        + " with status "
                                                                                                        + response.statusCode()
                                                                                                        + ": " + body));
                                                                }))
                                .bodyToMono(responseClass);
        }

        public Mono<Void> delete(String apiPath, String entityId, String authToken) {
                return webClient.delete()
                                .uri(apiPath + entityId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                                .retrieve()
                                .bodyToMono(Void.class)
                                .doOnSuccess(v -> log.debug("Successfully deleted entity with ID: {}", entityId))
                                .onErrorResume(e -> {
                                        log.warn("Failed to delete entity with ID '{}'. Error: {}", entityId,
                                                        e.getMessage());
                                        return Mono.empty();
                                });
        }
}