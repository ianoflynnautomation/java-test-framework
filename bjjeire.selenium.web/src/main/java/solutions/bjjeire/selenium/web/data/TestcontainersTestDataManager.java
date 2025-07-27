package solutions.bjjeire.selenium.web.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.gyms.Gym;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A stateless, thread-safe service for managing test data using a
 * Testcontainers MongoDB instance.
 * This implementation aligns with the TestDataManager interface for use in
 * parallel execution environments.
 * The core strategy for isolation is to wipe the database clean before each
 * scenario runs.
 */
@Service
@Profile("staging")
public class TestcontainersTestDataManager implements TestDataManager {

    private static final Logger log = LoggerFactory.getLogger(TestcontainersTestDataManager.class);
    private final RestTemplate restTemplate;
    private final String testApiBaseUrl;
    private MongoDBContainer mongoDBContainer;

    public TestcontainersTestDataManager(RestTemplate restTemplate,
            @Value("${web-settings.backendApiUrl}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.testApiBaseUrl = baseUrl + "/_test"; // Assuming a dedicated endpoint for testing
        log.info("Initialized TestcontainersTestDataManager for STAGING environment.");
    }

    @PostConstruct
    public void startContainer() {
        // The container starts once when the Spring context is created.
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));
        mongoDBContainer.start();

        // This sets the connection string for the backend application to use this
        // container.
        // This requires the backend to be configured to read this system property.
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        log.info("MongoDB Testcontainer started at: {}", mongoDBContainer.getReplicaSetUrl());
    }

    @PreDestroy
    public void stopContainer() {
        // The container is stopped once when the Spring context is destroyed.
        if (mongoDBContainer != null) {
            mongoDBContainer.stop();
            log.info("MongoDB Testcontainer stopped.");
        }
    }

    /**
     * Prepares the environment for a new scenario. For Testcontainers, this means
     * wiping the database to ensure a clean slate. This method also fulfills the
     * `authenticate` contract by returning a dummy token, which is not needed for
     * this implementation.
     *
     * @return A non-null, non-empty string as a placeholder token.
     */
    @Override
    public String authenticate() {
        log.info("Wiping Testcontainer database clean for new scenario.");
        // We assume a test-only endpoint exists on the backend to clear all data.
        restTemplate.delete(testApiBaseUrl + "/data/all");
        log.info("Database wipe complete.");
        // This implementation doesn't require a real token, but we return a placeholder
        // to satisfy the interface contract and avoid NullPointerExceptions.
        return "testcontainers-dummy-token";
    }

    /**
     * Seeds the database with a specific list of events. The auth token is ignored
     * as the test endpoint is assumed to be open.
     *
     * @param events    A list of Event POJOs to create.
     * @param authToken This is ignored in the Testcontainers implementation.
     * @return A list of IDs of the created events.
     */
    @Override
    public List<String> seedEvents(List<BjjEvent> events, String authToken) {
        String url = testApiBaseUrl + "/seed/events";
        log.info("Seeding {} events into Testcontainer DB via endpoint: {}", events.size(), url);

        ResponseEntity<List<BjjEvent>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(events),
                new ParameterizedTypeReference<>() {
                });

        if (response.getBody() != null) {
            List<String> createdIds = response.getBody().stream()
                    .map(BjjEvent::id)
                    .collect(Collectors.toList());
            log.info("Successfully seeded {} events.", createdIds.size());
            return createdIds;
        }

        return List.of();
    }

    /**
     * Seeds the database with a specific list of gyms. The auth token is ignored
     * as the test endpoint is assumed to be open.
     *
     * @param gyms      A list of Gym POJOs to create.
     * @param authToken This is ignored in the Testcontainers implementation.
     * @return A list of IDs of the created gyms.
     */
    @Override
    public List<String> seedGyms(List<Gym> gyms, String authToken) {
        String url = testApiBaseUrl + "/seed/gyms";
        log.info("Seeding {} gyms into Testcontainer DB via endpoint: {}", gyms.size(), url);

        ResponseEntity<List<Gym>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(gyms),
                new ParameterizedTypeReference<>() {
                });

        if (response.getBody() != null) {
            List<String> createdIds = response.getBody().stream()
                    .map(Gym::id)
                    .collect(Collectors.toList());
            log.info("Successfully seeded {} gyms.", createdIds.size());
            return createdIds;
        }

        return List.of();
    }

    /**
     * This is a no-op for the Testcontainers strategy.
     * Cleanup is handled by the `authenticate()` method, which wipes the database
     * completely at the beginning of the *next* scenario.
     *
     * @param createdEventIds This is ignored.
     * @param authToken       This is ignored.
     */
    @Override
    public void teardownEvents(List<String> createdEventIds, String authToken) {
        log.info("Testcontainers teardown for events is a no-op. Cleanup is handled before the next scenario runs.");
    }

    /**
     * This is a no-op for the Testcontainers strategy.
     * Cleanup is handled by the `authenticate()` method, which wipes the database
     * completely at the beginning of the *next* scenario.
     *
     * @param createdGymIds This is ignored.
     * @param authToken     This is ignored.
     */
    @Override
    public void teardownGyms(List<String> createdGymIds, String authToken) {
        log.info("Testcontainers teardown for gyms is a no-op. Cleanup is handled before the next scenario runs.");
    }
}