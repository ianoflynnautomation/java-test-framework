package solutions.bjjeire.selenium.web.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
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
                .enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .build();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(this.objectMapper);
        this.restTemplate.getMessageConverters().add(0, converter);

        log.info("Initialized STATLESS ApiTestDataManager for DEVELOPMENT environment with explicit message converter.");
    }

    /**
     * Authenticates with the backend API and returns a JWT token.
     * This method is stateless; it does not store the token as an instance variable.
     *
     * @return A String containing the authentication token.
     * @throws IllegalStateException if authentication fails.
     */
    @Override
    public String authenticate() {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/generate-token")
                .queryParam("userId", adminUser)
                .queryParam("role", adminRole)
                .toUriString();

        log.info("Authenticating as admin user '{}' to get API token...", adminUser);
        try {
            ResponseEntity<GenerateTokenResponse> response = restTemplate.getForEntity(url, GenerateTokenResponse.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().token() != null) {
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

    /**
     * Creates a list of BJJ events via API calls.
     * This method is stateless and requires the auth token to be passed in.
     *
     * @param events    A list of BjjEvent objects to create.
     * @param authToken The authentication token to use for the API requests.
     * @return A list of String IDs for the events that were successfully created.
     */
    @Override
    public List<String> seedEvents(List<BjjEvent> events, String authToken) {
        if (authToken == null || authToken.isBlank()) {
            throw new IllegalArgumentException("Authentication token must be provided to seed events.");
        }

        List<String> createdEventIds = new ArrayList<>();
        String url = baseUrl + "/api/bjjevent";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.info("Seeding {} events...", events.size());

        for (BjjEvent event : events) {
            CreateBjjEventCommand command = new CreateBjjEventCommand(event);
            HttpEntity<CreateBjjEventCommand> requestEntity = new HttpEntity<>(command, headers);

            try {
                String jsonBody = objectMapper.writeValueAsString(command); // Log the command object
                log.info("Attempting to POST to {}. Request Body:\n{}", url, jsonBody);

                ResponseEntity<CreateBjjEventResponse> response = restTemplate.postForEntity(url, requestEntity, CreateBjjEventResponse.class);
                if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null && response.getBody().data() != null) {
                    String newEventId = response.getBody().data().id();
                    if (newEventId != null && !newEventId.isBlank()) {
                        createdEventIds.add(newEventId);
                        log.debug("Successfully created event '{}' with ID: {}", event.name(), newEventId);
                    } else {
                        log.warn("Created event '{}' but did not receive a valid ID in the response.", event.name());
                    }
                } else {
                    log.warn("Failed to create event '{}'. Status: {}", event.name(), response.getStatusCode());
                }
            } catch (HttpClientErrorException e) {
                log.error("HTTP Client Error creating event '{}': {} - {}", event.name(), e.getStatusCode(), e.getResponseBodyAsString());
            } catch (JsonProcessingException e) {
                log.error("Could not serialize request body to JSON for logging.", e);
            } catch (Exception e) {
                log.error("An unexpected error occurred while creating event '{}':", event.name(), e);
            }
        }
        return createdEventIds;
    }

    /**
     * Deletes a list of BJJ events via API calls.
     * This method is stateless and requires both the list of IDs and the auth token to be passed in.
     *
     * @param createdEventIds A list of event IDs to be deleted.
     * @param authToken       The authentication token to use for the API requests.
     */
    @Override
    public void teardown(List<String> createdEventIds, String authToken) {
        if (authToken == null || authToken.isBlank() || createdEventIds == null || createdEventIds.isEmpty()) {
            log.info("No events to clean up or authentication token not provided, skipping teardown.");
            return;
        }

        log.info("Cleaning up {} created events...", createdEventIds.size());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        for (String eventId : createdEventIds) {
            String url = baseUrl + "/api/bjjevent/" + eventId;
            try {
                restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
                log.debug("Successfully deleted event with ID: {}", eventId);
            } catch (HttpClientErrorException e) {
                log.error("HTTP Error deleting event with ID '{}': {} - {}", eventId, e.getStatusCode(), e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("An unexpected error occurred while deleting event with ID '{}':", eventId, e);
            }
        }
        log.info("Scenario data cleanup complete.");
    }
}