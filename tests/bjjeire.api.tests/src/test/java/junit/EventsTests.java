package junit;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
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
import solutions.bjjeire.api.infrastructure.junit.ApiTestBase;
import solutions.bjjeire.api.services.AuthService;
import solutions.bjjeire.api.services.EventService;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;

@RequiredArgsConstructor
@Execution(ExecutionMode.CONCURRENT)
@DisplayName("BJJ Events API")
public class EventsTests extends ApiTestBase {

    @Autowired
    private EventService eventService;
    @Autowired
    private AuthService authService;

    private String authToken;

    @BeforeEach
    void setup() {
        this.authToken = authService.authenticateAsAdmin();
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
            ApiResponse apiResponse = eventService.createEvent(authToken, command).block();

            // Assert
            assertAll("Verify successful event creation",
                    () -> apiResponse.should().statusCode(201),
                    () -> apiResponse.should().bodySatisfies(CreateBjjEventResponse.class, responseBody -> {
                        assertNotNull(responseBody.data().id(), "Event ID should not be null");

                        org.assertj.core.api.Assertions.assertThat(responseBody.data())
                                .usingRecursiveComparison()
                                .ignoringFields("id", "createdOnUtc", "updatedOnUtc")
                                .isEqualTo(eventToCreate);

                        registerForCleanup(() -> eventService.deleteEvent(authToken, responseBody.data().id()));
                    }));
        }
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
        ApiResponse apiResponse = eventService.attemptToCreateEvent(authToken, invalidPayload).block();

        // Assert
        apiResponse.should()
                .statusCode(400)
                .containsErrorForField(field, errorMessage);
    }
}
