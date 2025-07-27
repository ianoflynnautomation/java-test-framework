package solutions.bjjeire.selenium.web.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;

import java.util.ArrayList;
import java.util.List;

/**
 * A stateless, thread-safe service for managing test data via API calls.
 */
@Service
@Profile("development")
public class ApiTestDataManager implements TestDataManager {

    private static final Logger log = LoggerFactory.getLogger(ApiTestDataManager.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    @Value("${test.api.admin.user:dev-user@example.com}")
    private String adminUser;
    @Value("${test.api.admin.role:Admin}")
    private String adminRole;

    public ApiTestDataManager(RestTemplate restTemplate, @Value("${web-settings.backendApiUrl}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.objectMapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .build();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(this.objectMapper);
        this.restTemplate.getMessageConverters().add(0, converter);

        log.info("Initialized STATLESS ApiTestDataManager for DEVELOPMENT environment.");
    }

    @Override
    public String authenticate() {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/generate-token")
                .queryParam("userId", adminUser)
                .queryParam("role", adminRole)
                .toUriString();

        log.info("Authenticating as admin user '{}' to get API token...", adminUser);
        try {
            ResponseEntity<GenerateTokenResponse> response = restTemplate.getForEntity(url,
                    GenerateTokenResponse.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                    && response.getBody().token() != null) {
                log.info("Successfully acquired auth token.");
                return response.getBody().token();
            } else {
                log.error("Failed to authenticate. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new IllegalStateException("Authentication failed. Response was not OK or body was null.");
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP Error during authentication: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new IllegalStateException("Could not authenticate due to HTTP error.", e);
        }
    }

    @Override
    public List<String> seedEvents(List<BjjEvent> events, String authToken) {
        if (authToken == null || authToken.isBlank()) {
            throw new IllegalArgumentException("Authentication token must be provided to seed events.");
        }

        List<String> createdEventIds = new ArrayList<>();
        String url = baseUrl + "/api/bjjevent";
        HttpHeaders headers = createAuthHeaders(authToken);

        log.info("Seeding {} events...", events.size());

        for (BjjEvent event : events) {
            CreateBjjEventCommand command = new CreateBjjEventCommand(event);
            HttpEntity<CreateBjjEventCommand> requestEntity = new HttpEntity<>(command, headers);

            try {
                logJsonRequest(url, command);
                ResponseEntity<CreateBjjEventResponse> response = restTemplate.postForEntity(url, requestEntity,
                        CreateBjjEventResponse.class);
                if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null
                        && response.getBody().data() != null) {
                    String newEventId = response.getBody().data().id();
                    createdEventIds.add(newEventId);
                    log.debug("Successfully created event '{}' with ID: {}", event.name(), newEventId);
                } else {
                    log.warn("Failed to create event '{}'. Status: {}", event.name(), response.getStatusCode());
                }
            } catch (HttpClientErrorException e) {
                log.error("HTTP Client Error creating event '{}': {} - {}", event.name(), e.getStatusCode(),
                        e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("An unexpected error occurred while creating event '{}':", event.name(), e);
            }
        }
        return createdEventIds;
    }

    @Override
    public List<String> seedGyms(List<Gym> gyms, String authToken) {
        if (authToken == null || authToken.isBlank()) {
            throw new IllegalArgumentException("Authentication token must be provided to seed gyms.");
        }

        List<String> createdGymIds = new ArrayList<>();
        String url = baseUrl + "/api/gym";
        HttpHeaders headers = createAuthHeaders(authToken);

        log.info("Seeding {} gyms...", gyms.size());

        for (Gym gym : gyms) {
            CreateGymCommand command = new CreateGymCommand(gym);
            HttpEntity<CreateGymCommand> requestEntity = new HttpEntity<>(command, headers);

            try {
                logJsonRequest(url, command);
                ResponseEntity<CreateGymResponse> response = restTemplate.postForEntity(url, requestEntity,
                        CreateGymResponse.class);
                if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null
                        && response.getBody().data() != null) {
                    String newGymId = response.getBody().data().id();
                    createdGymIds.add(newGymId);
                    log.debug("Successfully created gym '{}' with ID: {}", gym.name(), newGymId);
                } else {
                    log.warn("Failed to create gym '{}'. Status: {}", gym.name(), response.getStatusCode());
                }
            } catch (HttpClientErrorException e) {
                log.error("HTTP Client Error creating gym '{}': {} - {}", gym.name(), e.getStatusCode(),
                        e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("An unexpected error occurred while creating gym '{}':", gym.name(), e);
            }
        }
        return createdGymIds;
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
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        for (String entityId : entityIds) {
            String url = baseUrl + apiPath + entityId;
            try {
                restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
                log.debug("Successfully deleted {} with ID: {}", entityType, entityId);
            } catch (HttpClientErrorException e) {
                log.error("HTTP Error deleting {} with ID '{}': {} - {}", entityType, entityId, e.getStatusCode(),
                        e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("An unexpected error occurred while deleting {} with ID '{}':", entityType, entityId, e);
            }
        }
        log.info("Scenario data cleanup complete for {}s.", entityType);
    }

    private HttpHeaders createAuthHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
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
