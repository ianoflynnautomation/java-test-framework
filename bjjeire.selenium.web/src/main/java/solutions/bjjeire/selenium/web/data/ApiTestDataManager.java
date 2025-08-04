package solutions.bjjeire.selenium.web.data;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;

/**
 * A stateless, thread-safe service for managing test data via non-blocking API
 * calls using Spring WebClient.
 * This is the primary implementation for development and testing environments.
 */
@Service
@Primary
@Profile("development")
public class ApiTestDataManager implements TestDataManager {

    private static final Logger log = LoggerFactory.getLogger(ApiTestDataManager.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${test.api.admin.user:dev-user@example.com}")
    private String adminUser;
    @Value("${test.api.admin.role:Admin}")
    private String adminRole;

    public ApiTestDataManager(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        log.info("Initialized STATLESS ApiTestDataManager with WebClient for DEVELOPMENT environment.");
    }

    @Override
    public String authenticate() {
        log.info("Authenticating as admin user '{}' to get API token...", adminUser);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/generate-token")
                        .queryParam("userId", adminUser)
                        .queryParam("role", adminRole)
                        .build())
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("HTTP Error during authentication: {} - {}", response.statusCode(), body);
                            return Mono.error(new IllegalStateException(
                                    "Could not authenticate due to HTTP error: " + response.statusCode()));
                        }))
                .bodyToMono(GenerateTokenResponse.class)
                .map(response -> {
                    if (response != null && response.token() != null) {
                        log.info("Successfully acquired auth token.");
                        return response.token();
                    } else {
                        log.error("Failed to authenticate. Response body or token was null.");
                        throw new IllegalStateException(
                                "Authentication failed. Response was OK but body or token was null.");
                    }
                })
                .block(TIMEOUT); // Block to return a synchronous result
    }

    @Override
    public List<String> seedEvents(List<BjjEvent> events, String authToken) {
        validateAuthToken(authToken);
        log.info("Seeding {} events...", events.size());

        return Flux.fromIterable(events)
                .flatMap(event -> {
                    CreateBjjEventCommand command = new CreateBjjEventCommand(event);
                    logJsonRequest("/api/bjjevent", command);

                    return webClient.post()
                            .uri("/api/bjjevent")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(command)
                            .retrieve()
                            .onStatus(status -> status.value() != HttpStatus.CREATED.value(), response -> {
                                log.warn("Failed to create event '{}'. Status: {}", event.name(),
                                        response.statusCode());
                                return Mono.empty(); // Continue with other events
                            })
                            .bodyToMono(CreateBjjEventResponse.class)
                            .map(response -> response.data().id())
                            .doOnSuccess(
                                    id -> log.debug("Successfully created event '{}' with ID: {}", event.name(), id))
                            .onErrorResume(e -> {
                                log.error("An unexpected error occurred while creating event '{}':", event.name(), e);
                                return Mono.empty();
                            });
                })
                .filter(Objects::nonNull)
                .collectList()
                .block(TIMEOUT); // Block to return a synchronous result
    }

    @Override
    public List<String> seedGyms(List<Gym> gyms, String authToken) {
        validateAuthToken(authToken);
        log.info("Seeding {} gyms...", gyms.size());

        return Flux.fromIterable(gyms)
                .flatMap(gym -> {
                    CreateGymCommand command = new CreateGymCommand(gym);
                    logJsonRequest("/api/gym", command);

                    return webClient.post()
                            .uri("/api/gym")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(command)
                            .retrieve()
                            .onStatus(status -> status.value() != HttpStatus.CREATED.value(), response -> {
                                log.warn("Failed to create gym '{}'. Status: {}", gym.name(), response.statusCode());
                                return Mono.empty();
                            })
                            .bodyToMono(CreateGymResponse.class)
                            .map(response -> response.data().id())
                            .doOnSuccess(id -> log.debug("Successfully created gym '{}' with ID: {}", gym.name(), id))
                            .onErrorResume(e -> {
                                log.error("An unexpected error occurred while creating gym '{}':", gym.name(), e);
                                return Mono.empty();
                            });
                })
                .filter(Objects::nonNull)
                .collectList()
                .block(TIMEOUT);
    }

    @Override
    public void teardownEvents(List<String> createdEventIds, String authToken) {
        cleanupEntities("event", createdEventIds, authToken, "/api/bjjevent/");
    }

    @Override
    public void teardownGyms(List<String> createdGymIds, String authToken) {
        cleanupEntities("gym", createdGymIds, authToken, "/api/gym/");
    }

    private void cleanupEntities(String entityType, List<String> entityIds, String authToken, String apiPath) {
        if (authToken == null || authToken.isBlank() || entityIds == null || entityIds.isEmpty()) {
            log.info("No {}s to clean up or auth token not provided, skipping teardown.", entityType);
            return;
        }

        log.info("Cleaning up {} created {}s...", entityIds.size(), entityType);

        Flux.fromIterable(entityIds)
                .flatMap(entityId -> webClient.delete()
                        .uri(apiPath + entityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                        .retrieve()
                        .toBodilessEntity()
                        .doOnSuccess(response -> log.debug("Successfully deleted {} with ID: {}", entityType, entityId))
                        .onErrorResume(e -> {
                            log.error("Error deleting {} with ID '{}':", entityType, entityId, e);
                            return Mono.empty(); // Continue cleanup even if one fails
                        }))
                .then() // Wait for all deletions to complete
                .block(TIMEOUT);

        log.info("Scenario data cleanup complete for {}s.", entityType);
    }

    private void validateAuthToken(String authToken) {
        if (authToken == null || authToken.isBlank()) {
            throw new IllegalArgumentException("Authentication token must be provided.");
        }
    }

    private void logJsonRequest(String url, Object command) {
        try {
            String jsonBody = objectMapper.writeValueAsString(command);
            log.info("Attempting to POST to {}. Request Body:\n{}", url, jsonBody);
        } catch (JsonProcessingException e) {
            log.error("Could not serialize request body to JSON for logging.", e);
        }
    }
}
