package solutions.bjjeire.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.client.ApiRequest;
import solutions.bjjeire.api.client.Client;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.http.auth.BearerTokenAuth;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.events.GetBjjEventPaginationQuery;

import java.util.HashMap;
import java.util.Map;

@Service("bjjEventService")
@RequiredArgsConstructor
public class EventService {

    private final Client httpClient;
    private final ApiSettings settings;

    public Mono<ApiResponse> createEvent(String authToken, CreateBjjEventCommand command) {
        ApiRequest request = ApiRequest.builder().post("/api/bjjevent")
                .auth(new BearerTokenAuth(authToken))
                .body(command)
                .build();
        return httpClient.execute(request);
    }

    public Mono<ApiResponse> deleteEvent(String authToken, String eventId) {
        ApiRequest request = ApiRequest.builder().delete("/api/bjjevent" + "/" + eventId)
                .auth(new BearerTokenAuth(authToken))
                .build();
        return httpClient.execute(request);
    }

    public Mono<ApiResponse> getEvents(String authToken, GetBjjEventPaginationQuery query) {
        Map<String, Object> queryParams = new HashMap<>();
        if (query.getCounty() != null) queryParams.put("county", query.getCounty().name());
        if (query.getType() != null) queryParams.put("type", query.getType().name());

        ApiRequest request = ApiRequest.builder().get("/api/bjjevent")
                .auth(new BearerTokenAuth(authToken))
                .queryParams(queryParams)
                .build();
        return httpClient.execute(request);
    }

    public Mono<ApiResponse> attemptToCreateEvent(String authToken, Object invalidPayload) {
        ApiRequest request = ApiRequest.builder().post("/api/bjjevent")
                .auth(new BearerTokenAuth(authToken))
                .body(invalidPayload)
                .build();
        return httpClient.execute(request);
    }
}