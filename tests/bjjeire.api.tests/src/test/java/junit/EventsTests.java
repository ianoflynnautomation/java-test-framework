package junit;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.RequiredArgsConstructor;
import solutions.bjjeire.api.auth.BearerTokenAuth;
import solutions.bjjeire.api.client.BjjEventsApiClient;
import solutions.bjjeire.api.infrastructure.junit.ApiTestBase;
import solutions.bjjeire.api.services.AuthService;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;

@RequiredArgsConstructor
@Execution(ExecutionMode.CONCURRENT)
@DisplayName("BJJ Events API")
public class EventsTests extends ApiTestBase {

    @Autowired private BjjEventsApiClient eventsApiClient;
    @Autowired private AuthService authService;

    private String authToken;

    @BeforeEach
    void setup() {
        this.authToken = authService.getTokenFor("admin").block();
    }

    @Nested
    @DisplayName("Create Event (POST /api/bjjevent)")
    class CreateEventScenarios {

        @Test
        @DisplayName("Should return 201 Created for a valid event")
        void createBjjEvent_withValidData_shouldReturn201() {
            // Arrange
            BjjEvent eventToCreate = BjjEventFactory.getValidBjjEvent();
            CreateBjjEventCommand command = new CreateBjjEventCommand(eventToCreate);

            // Act
            ApiResponse apiResponse = eventsApiClient.createEvent(new BearerTokenAuth(authToken), command).block();

            // Assert
            apiResponse.should().isCreated()
                    .and().hasHeader("Content-Type", "application/json")
                    .and().hasExecutionTimeUnder(Duration.ofSeconds(1))
                    .and().bodyMatchesSchema("schemas/CreateBjjEventResponseSchema.json")
                    .and().bodySatisfies(CreateBjjEventResponse.class, responseBody -> {
                        assertNotNull(responseBody.data().id(), "Event ID should not be null");

                        org.assertj.core.api.Assertions.assertThat(responseBody.data())
                                .usingRecursiveComparison()
                                .ignoringFields("id", "createdOnUtc", "updatedOnUtc")
                                .isEqualTo(eventToCreate);

                        registerForCleanup(() -> eventsApiClient.deleteEvent(new BearerTokenAuth(authToken), responseBody.data().id()).block());
                    });
        }

        @ParameterizedTest(name = "Run #{index}: Field=''{0}'', Value=''{1}''")
        @CsvSource({
                "Data.Name,           '', 'Event Name is required.'",
                "Data.Pricing.Amount, -10.00, 'Amount must be greater than 0.'"
        })
        @DisplayName("Should return 400 Bad Request for various invalid data points")
        void createBjjEvent_withInvalidData_shouldReturn400(String field, String invalidValue, String errorMessage) {
            // Arrange
            Object invalidPayload = BjjEventFactory.createPayloadWithInvalidDetails(Map.of(field, invalidValue));

            // Act
            ApiResponse apiResponse = eventsApiClient.createEventWithInvalidPayload(new BearerTokenAuth(authToken), invalidPayload).block();

            // Assert
            apiResponse.should().isBadRequest()
                    .and().bodyContainsErrorForField("Data.Name", "Event Name is required.");
        }
    }
}
