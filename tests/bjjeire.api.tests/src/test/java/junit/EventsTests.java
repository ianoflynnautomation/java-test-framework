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

        // Act
        CreateBjjEventResponse response = eventApi.createEvent(this.authToken, eventToCreate);
        BjjEvent createdEvent = response.data();

        registerForCleanup(() -> eventApi.deleteEvent(this.authToken, createdEvent.id()));

        // Assert
        assertNotNull(createdEvent, "Response should not be null");
        assertNotNull(createdEvent.id(), "Event ID should not be null");
        assertEquals(eventToCreate.name(), createdEvent.name());
        assertEquals(eventToCreate.description(), createdEvent.description());
        assertEquals(eventToCreate.type(), createdEvent.type());
    }
}