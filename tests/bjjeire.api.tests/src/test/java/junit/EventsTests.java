package junit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;
import solutions.bjjeire.api.infrastructure.junit.ApiTestBase;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.BjjEventFactory;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test class for the BJJ Events API.
 * This class demonstrates parallel execution, a fluent API, and test isolation.
 * The @Execution(ExecutionMode.CONCURRENT) annotation enables parallel test runs.
 */
@Execution(ExecutionMode.CONCURRENT)
public class EventsTests extends ApiTestBase {

    @Test
    @DisplayName("Should create a BJJ event successfully with a valid auth token")
    public void createBjjEvent_withValidData_shouldReturn201() {
        // Arrange: Get an auth token first
        GenerateTokenResponse tokenResponse = testClient()
                .withQueryParams(Map.of(
                        "userId", "dev-user@example.com",
                        "role", "Admin"
                ))
                .get("/generate-token")
                .then()
                .hasStatusCode(200)
                .as(GenerateTokenResponse.class);

        String token = tokenResponse.token();
        CreateBjjEventCommand command = BjjEventFactory.getValidBjjEventCommand();

        // Act & Assert: Use the token to create an event and validate the response
        testClient()
                .withAuthToken(token)
                .body(command)
                .post("/api/bjjevent")
                .then()
                .hasStatusCode(201)
                .and().bodySatisfies(CreateBjjEventResponse.class, response -> {
                    assertNotNull(response, "Response should not be null");
                    assertNotNull(response.data(), "Response data should not be null");

                    BjjEvent responseData = response.data();
                    assertNotNull(responseData.id(), "Event ID should not be null");
                    assertEquals(command.data().name(), responseData.name());
                    assertEquals(command.data().description(), responseData.description());
                    assertEquals(command.data().type(), responseData.type());
                    assertEquals(command.data().organiser(), responseData.organiser());
                    assertEquals(command.data().location(), responseData.location());
                    assertEquals(command.data().pricing(), responseData.pricing());
                });
    }
}