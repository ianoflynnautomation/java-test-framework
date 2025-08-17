package solutions.bjjeire.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.client.ApiRequestBuilder;
import solutions.bjjeire.api.client.Client;
import solutions.bjjeire.api.config.TestUsersConfig;
import solutions.bjjeire.api.endpoints.AuthEndpoints;
import solutions.bjjeire.api.models.AuthenticationFailedException;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final Client httpClient;
    private final TestUsersConfig testUsersConfig;

    private final Map<String, Mono<String>> cachedUserTokens = new ConcurrentHashMap<>();


    public Mono<String> getTokenFor(String userKey) {
        return cachedUserTokens.computeIfAbsent(userKey, this::authenticate);
    }

    private Mono<String> authenticate(String userKey) {
        TestUsersConfig.User user = testUsersConfig.getUser(userKey);

        return authenticateWithCredentials(user.getUserId(), user.getRole())
                .flatMap(response -> {
                    if (response.getStatusCode() != 200) {
                        String errorMessage = String.format(
                                "Authentication for user '%s' failed with status code: %d. Response: %s",
                                userKey, response.getStatusCode(), response.getBodyAsString());
                        return Mono.error(new AuthenticationFailedException(errorMessage));
                    }
                    return Mono.just(response.as(GenerateTokenResponse.class).token());
                });
    }


    public Mono<ApiResponse> authenticateWithCredentials(String userId, String role) {
        ApiRequestBuilder request = ApiRequestBuilder.builder().get(AuthEndpoints.GENERATE_TOKEN)
                .queryParams(Map.of("userId", userId, "role", role))
                .build();
        return httpClient.execute(request);
    }
}