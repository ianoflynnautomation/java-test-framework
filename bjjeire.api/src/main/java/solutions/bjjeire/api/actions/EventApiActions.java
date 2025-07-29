package solutions.bjjeire.api.actions;

import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.http.ApiClient;
import solutions.bjjeire.api.http.RequestSpecification;
import solutions.bjjeire.api.validation.ValidatableResponse;
import solutions.bjjeire.core.data.events.BjjEvent;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.CreateBjjEventResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * An API Actions class that encapsulates all business-level operations
 * for the BJJ Event resource. It hides the underlying TestClient implementation details.
 */
@Component
public class EventApiActions {

    @Autowired
    private ApiClient apiClient;

    public record CreationResult<T>(T resource, Runnable cleanupAction) {}

    public CreationResult<BjjEvent> createEvent(String authToken, BjjEvent event) {
        CreateBjjEventCommand command = new CreateBjjEventCommand(event);

        CreateBjjEventResponse response = new RequestSpecification(apiClient, Map.of(), Map.of(), null, null)
                .withAuthToken(authToken)
                .withBody(command)
                .post("/api/bjjevent")
                .then().hasStatusCode(201)
                .as(CreateBjjEventResponse.class);

        BjjEvent createdEvent = response.data();
        final String eventId = createdEvent.id();

        Runnable cleanupAction = () -> {
            System.out.printf("CLEANUP: Deleting event with ID: %s%n", eventId);
            new RequestSpecification(apiClient, Map.of(), Map.of(), null, null)
                    .withAuthToken(authToken)
                    .delete("/api/bjjevent/" + eventId)
                    .then().hasStatusCode(204);
        };

        return new CreationResult<>(createdEvent, cleanupAction);
    }

    public ValidatableResponse getEvents(String authToken, String county, String name) {
        return new RequestSpecification(apiClient, Map.of(), Map.of(), null, null)
                .withAuthToken(authToken)
                .withQueryParams(Map.of(
                        "County", county.replace(" ", ""),
                        "Name", name
                ))
                .get("/api/bjjevent");
    }
}