package solutions.bjjeire.api.actions;

import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.api.validation.ResponseAsserter;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

/**
 * An API Actions class that encapsulates all business-level operations
 * for the BJJ Event resource. It hides the underlying TestClient implementation details.
 */
@Component
public class EventApiActions {

    /**
     * A record to hold the result of a creation action, bundling the created
     * resource with its specific cleanup logic.
     */
    public record CreationResult<T>(T resource, Consumer<TestClient> cleanupAction) {}

    /**
     * Creates a BJJ event and returns the created event along with its cleanup action.
     * @param client A fresh TestClient instance.
     * @param authToken The auth token to use for the request.
     * @param event The BjjEvent object to create.
     * @return A CreationResult containing the new event and its cleanup logic.
     */
    public CreationResult<BjjEvent> createEvent(TestClient client, String authToken, BjjEvent event) {
        CreateBjjEventCommand command = new CreateBjjEventCommand(event);

        CreateBjjEventResponse response = client
                .withAuthToken(authToken)
                .body(command)
                .post("/api/bjjevent")
                .then().hasStatusCode(201)
                .as(CreateBjjEventResponse.class);

        BjjEvent createdEvent = response.data();
        final String eventId = createdEvent.id();

        // Define the cleanup logic as a lambda.
        Consumer<TestClient> cleanupAction = c -> {
            System.out.printf("CLEANUP (from Action): Deleting event with ID: %s%n", eventId);
            c.withAuthToken(authToken)
                    .delete("/api/bjjevent/" + eventId)
                    .then().hasStatusCode(204);
        };

        // Return the created resource and its cleanup logic together.
        return new CreationResult<>(createdEvent, cleanupAction);
    }

    /**
     * Retrieves events by county and name.
     * @param client A fresh TestClient instance.
     * @param authToken The auth token to use for the request.
     * @param county The county to filter by.
     * @param name The name to filter by.
     * @return A ResponseAsserter for the API response.
     */
    public ResponseAsserter getEvents(TestClient client, String authToken, String county, String name) {
        return client
                .withAuthToken(authToken)
                .withQueryParams(Map.of(
                        "County", county.replace(" ", ""),
                        "Name", name
                ))
                .get("/api/bjjevent");
    }
}
