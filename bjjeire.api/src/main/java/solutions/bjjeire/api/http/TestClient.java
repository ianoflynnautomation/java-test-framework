package solutions.bjjeire.api.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.validation.ResponseAsserter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A fluent, stateful client for building and executing a single API test request.
 * This is a Spring-managed component, scoped to "cucumber-glue" to ensure
 * each scenario gets a fresh instance, providing test isolation.
 */
@Component
@Scope("prototype")
public class TestClient {

    private final ApiClient apiClient;
    private final Headers.Builder headers = new Headers.Builder();
    private final Map<String, String> queryParams = new HashMap<>();
    private Object requestBody = null;

    @Autowired
    public TestClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "ApiClient cannot be null.");
        // Default header for all requests
        this.headers.add("Accept", "application/json");
    }

    /**
     * Adds a header to the request.
     *
     * @param name  The header name.
     * @param value The header value.
     * @return The current TestClient instance for chaining.
     */
    public TestClient withHeader(String name, String value) {
        this.headers.add(name, value);
        return this;
    }

    /**
     * Adds an Authorization bearer token header to the request.
     *
     * @param token The bearer token.
     * @return The current TestClient instance for chaining.
     */
    public TestClient withAuthToken(String token) {
        if (token != null && !token.isBlank()) {
            this.headers.set("Authorization", "Bearer " + token);
        }
        return this;
    }

    /**
     * Adds query parameters to the request URL.
     *
     * @param paramsMap A map of query parameter keys and values.
     * @return The current TestClient instance for chaining.
     */
    public TestClient withQueryParams(Map<String, String> paramsMap) {
        this.queryParams.putAll(paramsMap);
        return this;
    }

    /**
     * Sets the request body. The object will be serialized to JSON.
     *
     * @param body The request body object.
     * @return The current TestClient instance for chaining.
     */
    public TestClient body(Object body) {
        this.requestBody = body;
        return this;
    }

    /**
     * Executes a GET request to the specified path.
     *
     * @param path The request path (e.g., "/users/1").
     * @return A ResponseAsserter for validating the response.
     */
    public ResponseAsserter get(String path) {
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .headers(headers.build())
                .get()
                .build();
        return apiClient.execute(request);
    }

    /**
     * Executes a POST request to the specified path.
     *
     * @param path The request path.
     * @return A ResponseAsserter for validating the response.
     */
    public ResponseAsserter post(String path) {
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .headers(headers.build())
                .post(createRequestBody())
                .build();
        return apiClient.execute(request);
    }

    /**
     * Executes a PUT request to the specified path.
     *
     * @param path The request path.
     * @return A ResponseAsserter for validating the response.
     */
    public ResponseAsserter put(String path) {
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .headers(headers.build())
                .put(createRequestBody())
                .build();
        return apiClient.execute(request);
    }

    /**
     * Executes a DELETE request to the specified path.
     *
     * @param path The request path.
     * @return A ResponseAsserter for validating the response.
     */
    public ResponseAsserter delete(String path) {
        // OkHttp requires a body for DELETE, even if it's empty.
        RequestBody body = (this.requestBody != null) ? createRequestBody() : RequestBody.create(new byte[0]);
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .headers(headers.build())
                .delete(body)
                .build();
        return apiClient.execute(request);
    }

    private HttpUrl buildUrl(String path) {
        String baseUrl = apiClient.getSettings().getBaseUrl();
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(baseUrl), "Base URL is invalid.")
                .newBuilder();
        urlBuilder.addPathSegments(path.replaceAll("^/+", ""));
        this.queryParams.forEach(urlBuilder::addQueryParameter);
        return urlBuilder.build();
    }

    private RequestBody createRequestBody() {
        if (requestBody == null) {
            // Return empty body for POST/PUT requests without an explicit body
            return RequestBody.create(new byte[0], null);
        }
        try {
            String jsonBody = apiClient.getObjectMapper().writeValueAsString(requestBody);
            return RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize request body to JSON", e);
        }
    }
}
