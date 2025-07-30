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
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;

import static org.junit.jupiter.api.Assertions.*;

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
        CreateBjjEventResponse response = eventApi.createEvent(this.authToken, command);

        registerForCleanup(() -> eventApi.deleteEvent(this.authToken, response.data().id()));

        // Assert
        assertNotNull(response.data(), "Response should not be null");
        assertNotNull(response.data().id(), "Event ID should not be null");
        assertEquals(eventToCreate.name(), response.data().name());
        assertEquals(eventToCreate.description(), response.data().description());
        assertEquals(eventToCreate.type(), response.data().type());
    }
}