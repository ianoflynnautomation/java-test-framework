package solutions.bjjeire.api.http;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.*;
import java.util.function.Consumer;


@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestSpecification {

    private final HttpMethod method;
    private final String path;
    @With private final MultiValueMap<String, String> headers;
    private final MultiValueMap<String, String> queryParams;
    @With private final Object body;
    @With private final Consumer<HttpHeaders> authAction;
    @With private final MediaType contentType;
    @With private final List<MediaType> acceptableMediaTypes;

    /**
     * Entry point for the fluent API.
     */
    public static RequestSpecification given() {
        return new RequestSpecification(
                null, "", new LinkedMultiValueMap<>(), new LinkedMultiValueMap<>(),
                null, httpHeaders -> {}, MediaType.APPLICATION_JSON,
                Collections.singletonList(MediaType.APPLICATION_JSON)
        );
    }

    // --- HTTP Method Setters ---
    public RequestSpecification get(String path) { return setMethodAndPath(HttpMethod.GET, path); }
    public RequestSpecification post(String path) { return setMethodAndPath(HttpMethod.POST, path); }
    public RequestSpecification put(String path) { return setMethodAndPath(HttpMethod.PUT, path); }
    public RequestSpecification delete(String path) { return setMethodAndPath(HttpMethod.DELETE, path); }
    public RequestSpecification patch(String path) { return setMethodAndPath(HttpMethod.PATCH, path); }

    private RequestSpecification setMethodAndPath(HttpMethod method, String path) {
        return new RequestSpecification(method, path, headers, queryParams, body, authAction, contentType, acceptableMediaTypes);
    }

    // --- Fluent Modifiers ---
    public RequestSpecification withHeader(String name, String value) {
        MultiValueMap<String, String> newHeaders = new LinkedMultiValueMap<>(this.headers);
        newHeaders.add(name, value);
        return this.withHeaders(newHeaders);
    }

    public RequestSpecification withQueryParam(String name, Object value) {
        MultiValueMap<String, String> newParams = new LinkedMultiValueMap<>(this.queryParams);
        newParams.add(name, String.valueOf(value));
        return new RequestSpecification(method, path, headers, newParams, body, authAction, contentType, acceptableMediaTypes);
    }

    public RequestSpecification withQueryParams(Map<String, ?> params) {
        MultiValueMap<String, String> newParams = new LinkedMultiValueMap<>(this.queryParams);
        params.forEach((key, value) -> newParams.add(key, String.valueOf(value)));
        return new RequestSpecification(method, path, headers, newParams, body, authAction, contentType, acceptableMediaTypes);
    }

    public RequestSpecification withAuthToken(String token) {
        Consumer<HttpHeaders> newAuthAction = httpHeaders -> {
            if (token != null && !token.isBlank()) {
                httpHeaders.setBearerAuth(token);
            }
        };
        return this.withAuthAction(newAuthAction);
    }
}