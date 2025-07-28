package solutions.bjjeire.selenium.web.data;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.gyms.Gym;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A stateless, thread-safe service for managing test data using a Testcontainers MongoDB instance.
 * This implementation uses WebClient for HTTP communication with the backend's test endpoints.
 */
@Service
@Profile("staging")
public class TestcontainersTestDataManager implements TestDataManager {

    private static final Logger log = LoggerFactory.getLogger(TestcontainersTestDataManager.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final WebClient webClient;
    private final String testApiBaseUrl;
    private MongoDBContainer mongoDBContainer;

    public TestcontainersTestDataManager(WebClient.Builder webClientBuilder,
                                         @Value("${web-settings.backendApiUrl}") String baseUrl) {
        this.testApiBaseUrl = baseUrl + "/_test"; // Assuming a dedicated endpoint for testing
        this.webClient = webClientBuilder.baseUrl(this.testApiBaseUrl).build();
        log.info("Initialized TestcontainersTestDataManager for STAGING environment.");
    }

    @PostConstruct
    public void startContainer() {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));
        mongoDBContainer.start();
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        log.info("MongoDB Testcontainer started at: {}", mongoDBContainer.getReplicaSetUrl());
    }

    @PreDestroy
    public void stopContainer() {
        if (mongoDBContainer != null) {
            mongoDBContainer.stop();
            log.info("MongoDB Testcontainer stopped.");
        }
    }

    @Override
    public String authenticate() {
        log.info("Wiping Testcontainer database clean for new scenario.");
        webClient.delete()
                .uri("/data/all")
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("Database wipe complete."))
                .doOnError(error -> log.error("Failed to wipe database.", error))
                .block(TIMEOUT);

        return "testcontainers-dummy-token";
    }

    @Override
    public List<String> seedEvents(List<BjjEvent> events, String authToken) {
        log.info("Seeding {} events into Testcontainer DB via endpoint: /seed/events", events.size());

        List<BjjEvent> createdEvents = webClient.post()
                .uri("/seed/events")
                .bodyValue(events)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BjjEvent>>() {})
                .doOnSuccess(response -> log.info("Successfully seeded {} events.", response.size()))
                .onErrorResume(e -> {
                    log.error("Failed to seed events.", e);
                    return Mono.just(List.of());
                })
                .block(TIMEOUT);

        return createdEvents.stream().map(BjjEvent::id).collect(Collectors.toList());
    }

    @Override
    public List<String> seedGyms(List<Gym> gyms, String authToken) {
        log.info("Seeding {} gyms into Testcontainer DB via endpoint: /seed/gyms", gyms.size());

        List<Gym> createdGyms = webClient.post()
                .uri("/seed/gyms")
                .bodyValue(gyms)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Gym>>() {})
                .doOnSuccess(response -> log.info("Successfully seeded {} gyms.", response.size()))
                .onErrorResume(e -> {
                    log.error("Failed to seed gyms.", e);
                    return Mono.just(List.of());
                })
                .block(TIMEOUT);

        return createdGyms.stream().map(Gym::id).collect(Collectors.toList());
    }

    @Override
    public void teardownEvents(List<String> createdEventIds, String authToken) {
        log.info("Testcontainers teardown for events is a no-op. Cleanup is handled before the next scenario runs.");
    }

    @Override
    public void teardownGyms(List<String> createdGymIds, String authToken) {
        log.info("Testcontainers teardown for gyms is a no-op. Cleanup is handled before the next scenario runs.");
    }
}
