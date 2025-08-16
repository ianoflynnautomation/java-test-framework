package solutions.bjjeire.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.client.ApiRequest;
import solutions.bjjeire.api.client.Client;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.http.auth.BearerTokenAuth;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.GetGymPaginationQuery;

import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class GymService {
    private final Client httpClient;
    private final ApiSettings settings;


    public Mono<ApiResponse> createGym(String authToken, CreateGymCommand command) {
        ApiRequest request = ApiRequest.builder().post("/api/gym")
                .auth(new BearerTokenAuth(authToken))
                .body(command)
                .build();
        return httpClient.execute(request);
    }

    public Mono<ApiResponse> deleteGym(String authToken, String eventId) {
        ApiRequest request = ApiRequest.builder().delete("/api/gym" + "/" + eventId)
                .auth(new BearerTokenAuth(authToken))
                .build();
        return httpClient.execute(request);
    }

    public Mono<ApiResponse> getGyms(String authToken, GetGymPaginationQuery query) {
        Map<String, Object> queryParams = new HashMap<>();
        if (query.getCounty() != null) queryParams.put("county", query.getCounty().name());

        ApiRequest request = ApiRequest.builder().get("/api/gym")
                .auth(new BearerTokenAuth(authToken))
                .queryParams(queryParams)
                .build();
        return httpClient.execute(request);
    }

    public Mono<ApiResponse> attemptToCreateGym(String authToken, Object invalidPayload) {
        ApiRequest request = ApiRequest.builder().post("/api/gym")
                .auth(new BearerTokenAuth(authToken))
                .body(invalidPayload)
                .build();
        return httpClient.execute(request);
    }
}