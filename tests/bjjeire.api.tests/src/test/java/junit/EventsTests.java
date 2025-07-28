package junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import solutions.bjjeire.api.actions.AuthApiActions;
import solutions.bjjeire.api.actions.EventApiActions;
import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.api.infrastructure.junit.ApiTestBase;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test class for the BJJ Events API.
 * This class demonstrates parallel execution, a fluent API, and test isolation.
 * The @Execution(ExecutionMode.CONCURRENT) annotation enables parallel test runs.
 */
@Execution(ExecutionMode.CONCURRENT)
public class EventsTests extends ApiTestBase {

    private final EventApiActions eventApi = new EventApiActions();

    private final AuthApiActions authApi = new AuthApiActions();

    private final List<Consumer<TestClient>> cleanupActions = new ArrayList<>();

    private String authToken;

    @BeforeEach
    void setup() {
        this.authToken = authApi.authenticateAsAdmin(testClient());
    }

    @AfterEach
    void teardown() {
        if (!cleanupActions.isEmpty()) {
            System.out.println("--- JUnit Teardown: Executing cleanup actions ---");
            cleanupActions.forEach(action -> action.accept(testClient()));
        }
    }

    @Test
    @DisplayName("Should create a BJJ event successfully with a valid auth token")
    void createBjjEvent_withValidData_shouldReturn201() {
        // Arrange
        BjjEvent eventToCreate = BjjEventFactory.getValidBjjEvent();

        // Act
        var result = eventApi.createEvent(testClient(), this.authToken, eventToCreate);
        BjjEvent createdEvent = result.resource();

        cleanupActions.add(result.cleanupAction());

        // Assert
        assertNotNull(createdEvent, "Response should not be null");
        assertNotNull(createdEvent.id(), "Event ID should not be null");
        assertEquals(eventToCreate.name(), createdEvent.name());
        assertEquals(eventToCreate.description(), createdEvent.description());
        assertEquals(eventToCreate.type(), createdEvent.type());
    }
}