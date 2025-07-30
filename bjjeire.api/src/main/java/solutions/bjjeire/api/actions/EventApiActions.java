package solutions.bjjeire.api.actions;

import solutions.bjjeire.api.http.auth.BearerTokenAuth;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.events.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventApiActions extends BaseApiActions {

    public ApiResponse createEvent(String authToken, CreateBjjEventCommand command) {
        return runner.run(
                request()
                        .auth(new BearerTokenAuth(authToken))
                        .body(command)
                        .post("/api/bjjevent")
                        .build()
        );
    }

    public void deleteEvent(String authToken, String eventId) {
        System.out.printf("CLEANUP: Deleting event with ID: %s%n", eventId);
        runner.run(
                request()
                        .auth(new BearerTokenAuth(authToken))
                        .delete("/api/bjjevent/" + eventId)
                        .build()
        ).then().statusCode(204);
    }

    public ApiResponse getEvents(String authToken, GetBjjEventPaginationQuery query) {
        Map<String, Object> queryParams = new HashMap<>();
        if (query.getCounty() != null) {
            queryParams.put("county", query.getCounty().name());
        }
        if (query.getType() != null) {
            queryParams.put("type", query.getType().name());
        }

        return runner.run(
                request()
                        .auth(new BearerTokenAuth(authToken))
                        .queryParams(queryParams)
                        .get("/api/bjjevent")
                        .build()
        );
    }

    public ApiResponse attemptToCreateEventWithInvalidData(String authToken, Object invalidPayload) {
        return runner.run(
                request()
                        .auth(new BearerTokenAuth(authToken))
                        .body(invalidPayload)
                        .post("/api/bjjevent")
                        .build()
        );
    }
}
