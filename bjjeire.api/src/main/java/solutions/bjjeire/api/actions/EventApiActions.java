package solutions.bjjeire.api.actions;

import solutions.bjjeire.api.http.auth.BearerTokenAuth;
import solutions.bjjeire.api.validation.ValidatableResponse;
import solutions.bjjeire.core.data.events.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventApiActions extends BaseApiActions{

    public CreateBjjEventResponse createEvent(String authToken, CreateBjjEventCommand command) {
        return runner.run(
                        given()
                                .withAuth(new BearerTokenAuth(authToken))
                                .withBody(command)
                                .post("/api/bjjevent")
                )
                .then().hasStatusCode(201)
                .as(CreateBjjEventResponse.class);
    }

    public ValidatableResponse attemptToCreateEvent(String authToken, Object payload) { // Changed here
        return runner.run(
                given()
                        .withAuth(new BearerTokenAuth(authToken))
                        .withBody(payload) // Now accepts any object
                        .post("/api/bjjevent")
        );
    }

    public void deleteEvent(String authToken, String eventId) {
        System.out.printf("CLEANUP: Deleting event with ID: %s%n", eventId);
        runner.run(
                given()
                        .withAuth(new BearerTokenAuth(authToken))
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
                                .withAuth(new BearerTokenAuth(authToken))
                                .withQueryParams(queryParams)
                                .get("/api/bjjevent")
                )
                .then().hasStatusCode(200)
                .as(GetBjjEventPaginatedResponse.class);
    }


    public ValidatableResponse attemptToCreateEventWithInvalidData(String authToken, Object invalidPayload) {
        return runner.run(
                given()
                        .withAuth(new BearerTokenAuth(authToken))
                        .withBody(invalidPayload)
                        .post("/api/bjjevent")
        );
    }
}