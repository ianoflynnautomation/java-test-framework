package junit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.actions.AuthApiActions;
import solutions.bjjeire.api.actions.EventApiActions;
import solutions.bjjeire.api.infrastructure.junit.ApiTestBase;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test class for the BJJ Events API.
 * This class demonstrates parallel execution, a fluent API, and test isolation.
 * The @Execution(ExecutionMode.CONCURRENT) annotation enables parallel test runs.
 */
@Execution(ExecutionMode.CONCURRENT)
public class EventsTests extends ApiTestBase {

    @Autowired private EventApiActions eventApi;
    @Autowired private AuthApiActions authApi;
        private String authToken;

    @BeforeEach
    void setup() {
        this.authToken = authApi.authenticateAsAdmin();
    }

    @Test
    @DisplayName("Should create a BJJ event successfully with a valid auth token")
    void createBjjEvent_withValidData_shouldReturn201() {
        // Arrange
        BjjEvent eventToCreate = BjjEventFactory.getValidBjjEvent();
        CreateBjjEventCommand command = new CreateBjjEventCommand(eventToCreate);

        // Act
        ApiResponse apiResponse = eventApi.createEvent(this.authToken, command);
        assertThat(apiResponse.getStatusCode()).isEqualTo(201);

        CreateBjjEventResponse responseBody = apiResponse.as(CreateBjjEventResponse.class);

        registerForCleanup(() -> eventApi.deleteEvent(this.authToken, responseBody.data().id()));

        // Assert
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.data()).isNotNull();
        assertThat(responseBody.data().id()).isNotNull().isNotBlank();
        assertThat(responseBody.data())
                .usingRecursiveComparison()
                .ignoringFields("id", "createdOnUtc", "updatedOnUtc")
                .isEqualTo(eventToCreate);
    }
}