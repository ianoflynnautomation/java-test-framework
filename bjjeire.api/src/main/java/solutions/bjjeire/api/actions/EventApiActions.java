package solutions.bjjeire.api.actions;

import solutions.bjjeire.api.validation.ValidatableResponse;
import solutions.bjjeire.core.data.events.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventApiActions extends BaseApiActions{

    public CreateBjjEventResponse createEvent(String authToken, BjjEvent event) {
        CreateBjjEventCommand command = new CreateBjjEventCommand(event);

        return runner.run(
                        given()
                                .withAuthToken(authToken)
                                .withBody(command)
                                .post("/api/bjjevent")
                )
                .then().hasStatusCode(201)
                .as(CreateBjjEventResponse.class);
    }
    public ValidatableResponse attemptToCreateEvent(String authToken, Object payload) { // Changed here
        return runner.run(
                given()
                        .withAuthToken(authToken)
                        .withBody(payload) // Now accepts any object
                        .post("/api/bjjevent")
        );
    }

    public void deleteEvent(String authToken, String eventId) {
        System.out.printf("CLEANUP: Deleting event with ID: %s%n", eventId);
        runner.run(
                given()
                        .withAuthToken(authToken)
                        .delete("/api/bjjevent/" + eventId)
        ).then().hasStatusCode(204);
    }


    public GetBjjEventPaginatedResponse getEvents(String authToken, GetBjjEventPaginationQuery query) {
        Map<String, Object> queryParams = new HashMap<>();
        if (query.getCounty() != null) {
            queryParams.put("county", query.getCounty().name());
        }
        if (query.getType() != null) {
            queryParams.put("type", query.getType().name());
        }

        return runner.run(
                        given()
                                .withAuthToken(authToken)
                                .withQueryParams(queryParams)
                                .get("/api/bjjevent")
                )
                .then().hasStatusCode(200)
                .as(GetBjjEventPaginatedResponse.class);
    }
}