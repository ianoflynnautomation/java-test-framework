package solutions.bjjeire.selenium.web.data;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;
import solutions.bjjeire.selenium.web.configuration.ApiSettings;
import solutions.bjjeire.selenium.web.data.strategy.EntityApiStrategy;

@Service
@Slf4j
@Primary
@Profile("development")
public class ApiTestDataManager implements TestDataManager {

    private final ApiSettings apiSettings;
    private final Duration apiTimeout;
    private final GenericApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final Map<Class<?>, EntityApiStrategy<?, ?, ?>> strategies;

    public ApiTestDataManager(
            ApiSettings apiSettings,
            GenericApiClient apiClient,
            ObjectMapper objectMapper,
            WebClient webClient,
            List<EntityApiStrategy<?, ?, ?>> strategyList) {
        this.apiSettings = apiSettings;
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
        this.webClient = webClient;
        this.apiTimeout = Duration.ofSeconds(apiSettings.getTimeoutSeconds());
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(EntityApiStrategy::getEntityType, Function.identity()));
        log.info("ApiTestDataManager initialized",
                StructuredArguments.keyValue("eventName", "apiTestDataManagerInit"),
                StructuredArguments.keyValue("strategyCount", strategies.size()));
    }

    @Override
    public String authenticate() {
        var adminUser = apiSettings.getAdmin();
        log.info("Authenticating user for API access",
                StructuredArguments.keyValue("eventName", "apiAuthenticationStart"),
                StructuredArguments.keyValue("user", adminUser.getUser()));

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/generate-token")
                        .queryParam("userId", adminUser.getUser())
                        .queryParam("role", adminUser.getRole())
                        .build())
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("API Authentication failed",
                                    StructuredArguments.keyValue("eventName", "apiAuthenticationFailed"),
                                    StructuredArguments.keyValue("httpStatus", response.statusCode().value()),
                                    StructuredArguments.keyValue("responseBody", body));
                            return Mono.error(new IllegalStateException(
                                    "Could not authenticate due to HTTP error: " + response.statusCode()));
                        }))
                .bodyToMono(GenerateTokenResponse.class)
                .map(response -> {
                    if (response != null && response.token() != null) {
                        log.debug("Successfully acquired auth token",
                                StructuredArguments.keyValue("eventName", "apiAuthenticationSuccess"));
                        return response.token();
                    } else {
                        log.error("API Authentication failed, response body or token was null",
                                StructuredArguments.keyValue("eventName", "apiAuthenticationFailed"),
                                StructuredArguments.keyValue("reason", "Null response or token"));
                        throw new IllegalStateException("Authentication failed. Response body or token was null.");
                    }
                })
                .block(apiTimeout);
    }

    @Override
    public <T> List<String> seed(List<T> entities, String authToken) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        validateAuthToken(authToken);
        Class<?> entityType = entities.get(0).getClass();
        log.info("Seeding entities via API",
                StructuredArguments.keyValue("eventName", "apiSeedStart"),
                StructuredArguments.keyValue("entityType", entityType.getSimpleName()),
                StructuredArguments.keyValue("count", entities.size()));

        return Flux.fromIterable(entities)
                .flatMap(entity -> seedSingleEntity(entity, authToken))
                .collectList()
                .block(apiTimeout);
    }

    @Override
    public <T> void teardown(Class<T> entityType, List<String> ids, String authToken) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        EntityApiStrategy<?, ?, ?> strategy = getStrategyForType(entityType);
        log.info("Cleaning up entities via API",
                StructuredArguments.keyValue("eventName", "apiTeardownStart"),
                StructuredArguments.keyValue("entityType", entityType.getSimpleName()),
                StructuredArguments.keyValue("count", ids.size()));

        Flux.fromIterable(ids)
                .flatMap(id -> apiClient.delete(strategy.getApiPath() + "/", id, authToken))
                .then()
                .block(apiTimeout);

        log.info("API data cleanup complete",
                StructuredArguments.keyValue("eventName", "apiTeardownSuccess"),
                StructuredArguments.keyValue("entityType", entityType.getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<String> seedSingleEntity(T entity, String authToken) {
        EntityApiStrategy<T, ?, ?> strategy = (EntityApiStrategy<T, ?, ?>) getStrategyForType(entity.getClass());
        return doSeed(entity, authToken, strategy);
    }

    private <T_ENTITY, T_COMMAND, T_RESPONSE> Mono<String> doSeed(
            T_ENTITY entity,
            String authToken,
            EntityApiStrategy<T_ENTITY, T_COMMAND, T_RESPONSE> strategy) {
        T_COMMAND command = strategy.createCommand(entity);
        logJsonRequest(strategy.getApiPath(), command);

        return apiClient.post(
                strategy.getApiPath(),
                command,
                authToken,
                strategy.getResponseClass())
                .map(strategy::getIdFromResponse)
                .doOnSuccess(id -> log.debug("Successfully created entity",
                        StructuredArguments.keyValue("eventName", "apiEntityCreated"),
                        StructuredArguments.keyValue("entityType", strategy.getEntityType().getSimpleName()),
                        StructuredArguments.keyValue("entityName", strategy.getEntityName(entity)),
                        StructuredArguments.keyValue("entityId", id)));
    }

    private EntityApiStrategy<?, ?, ?> getStrategyForType(Class<?> entityType) {
        EntityApiStrategy<?, ?, ?> strategy = strategies.get(entityType);
        if (strategy == null) {
            log.error("No data management strategy found for entity type",
                    StructuredArguments.keyValue("eventName", "apiStrategyNotFound"),
                    StructuredArguments.keyValue("entityType", entityType.getName()));
            throw new IllegalArgumentException("No data management strategy found for type: " + entityType.getName());
        }
        return strategy;
    }

    private void validateAuthToken(String authToken) {
        if (authToken == null || authToken.isBlank()) {
            log.error("Authentication token was null or blank for an API operation",
                    StructuredArguments.keyValue("eventName", "apiAuthTokenInvalid"));
            throw new IllegalArgumentException("Authentication token must be provided for API operations.");
        }
    }

    private void logJsonRequest(String url, Object command) {
        if (log.isDebugEnabled()) {
            try {
                String jsonBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(command);
                log.debug("Attempting API POST request",
                        StructuredArguments.keyValue("eventName", "apiPostAttempt"),
                        StructuredArguments.keyValue("url", url),
                        StructuredArguments.keyValue("requestBody", jsonBody));
            } catch (JsonProcessingException e) {
                log.warn("Could not serialize request body to JSON for logging",
                        StructuredArguments.keyValue("eventName", "jsonSerializationFailed"),
                        e);
            }
        }
    }
}