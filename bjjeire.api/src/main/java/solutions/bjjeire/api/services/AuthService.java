package solutions.bjjeire.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.client.ApiRequest;
import solutions.bjjeire.api.client.Client;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final Client httpClient;
    private final ApiSettings settings;


    public String authenticateAsAdmin() {
        ApiRequest request = ApiRequest.builder().get("/generate-token")
                .queryParams(Map.of("userId", "dev-user@example.com", "role", "Admin"))
                .build();

        ApiResponse response = httpClient.execute(request).block();

        if (response == null) {
            throw new IllegalStateException("Authentication response was null. Check API connectivity or response.");
        }

        if (response.getStatusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Authentication failed with status code: %d. Response body: %s",
                            response.getStatusCode(), response.getBodyAsString()));
        }

        return response.as(GenerateTokenResponse.class).token();
    }

    public Mono<ApiResponse> authenticateWithCredentials(String userId, String role) {
        ApiRequest request = ApiRequest.builder().get("/generate-token")
                .queryParams(Map.of("userId", userId, "role", role))
                .build();
        return httpClient.execute(request);
    }
}