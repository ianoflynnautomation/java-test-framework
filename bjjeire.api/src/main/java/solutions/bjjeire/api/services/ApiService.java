package solutions.bjjeire.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.auth.Authentication;
import solutions.bjjeire.api.client.ApiRequestBuilder;
import solutions.bjjeire.api.client.Client;
import solutions.bjjeire.api.validation.ApiResponse;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final Client httpClient;

    public Mono<ApiResponse> get(Authentication authentication, String path) {
        return execute(HttpMethod.GET, path, authentication, null);
    }

    public Mono<ApiResponse> post(Authentication authentication, String path, Object body) {
        return execute(HttpMethod.POST, path, authentication, body);
    }

    public Mono<ApiResponse> put(Authentication authentication, String path, Object body) {
        return execute(HttpMethod.PUT, path, authentication, body);
    }

    public Mono<ApiResponse> delete(Authentication authentication, String path) {
        return execute(HttpMethod.DELETE, path, authentication, null);
    }

    private Mono<ApiResponse> execute(HttpMethod method, String path, Authentication authentication, Object body) {
        ApiRequestBuilder.Builder requestBuilder = ApiRequestBuilder.builder()
                .method(method)
                .path(path)
                .auth(authentication);

        if (body != null) {
            requestBuilder.body(body);
        }

        return httpClient.execute(requestBuilder.build());
    }
}