package solutions.bjjeire.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.client.ApiRequestBuilder;
import solutions.bjjeire.api.client.Client;
import solutions.bjjeire.api.auth.BearerTokenAuth;
import solutions.bjjeire.api.validation.ApiResponse;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final Client httpClient;

    public Mono<ApiResponse> get(String authToken, String path) {
        return execute(HttpMethod.GET, path, authToken, null);
    }

    public Mono<ApiResponse> post(String authToken, String path, Object body) {
        return execute(HttpMethod.POST, path, authToken, body);
    }

    public Mono<ApiResponse> put(String authToken, String path, Object body) {
        return execute(HttpMethod.PUT, path, authToken, body);
    }

    public Mono<ApiResponse> delete(String authToken, String path) {
        return execute(HttpMethod.DELETE, path, authToken, null);
    }

    private Mono<ApiResponse> execute(HttpMethod method, String path, String authToken, Object body) {
        ApiRequestBuilder.Builder requestBuilder = ApiRequestBuilder.builder()
                .method(method)
                .path(path)
                .auth(new BearerTokenAuth(authToken));

        if (body != null) {
            requestBuilder.body(body);
        }

        return httpClient.execute(requestBuilder.build());
    }
}