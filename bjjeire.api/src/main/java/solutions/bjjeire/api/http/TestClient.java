package solutions.bjjeire.api.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.retry.Retry;
import okhttp3.*;
import solutions.bjjeire.api.services.ApiClientService;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.models.MeasuredResponse;
import solutions.bjjeire.api.validation.ResponseAsserter;
import java.util.Map;
import java.util.Objects;

/**
 * A fluent, stateful client for building and executing a single API test request.
 * An instance of this class is created by PicoContainer for each scenario,
 * ensuring that request details (headers, body, etc.) are isolated.
 */
public class TestClient {

    private final ApiClientService apiClientService;
    private final ApiSettings settings;
    private final Headers.Builder headers = new Headers.Builder();
    private final HttpUrl.Builder urlBuilder;
    private Object requestBody;

    public TestClient(ApiClientService apiClientService, ApiSettings settings) {
        this.apiClientService = Objects.requireNonNull(apiClientService);
        this.settings = Objects.requireNonNull(settings);
        this.urlBuilder = Objects.requireNonNull(HttpUrl.parse(settings.getBaseUrl()), "Base URL cannot be null").newBuilder();
    }

    public TestClient withHeader(String name, String value) {
        this.headers.add(name, value);
        return this;
    }

    public TestClient withAuthToken(String token) {
        if (token != null && !token.isBlank()) {
            this.headers.add("Authorization", "Bearer " + token);
        }
        return this;
    }

    public TestClient withQueryParams(Map<String, String> paramsMap) {
        paramsMap.forEach(this.urlBuilder::addQueryParameter);
        return this;
    }

    public TestClient body(Object body) {
        this.requestBody = body;
        return this;
    }

    public <T> ResponseAsserter<T> get(String path, Class<T> responseType) {
        urlBuilder.addPathSegments(path.replaceFirst("^/", ""));
        Request request = new Request.Builder().url(urlBuilder.build()).headers(headers.build()).get().build();
        return execute(request, responseType);
    }

    public <T> ResponseAsserter<T> post(String path, Class<T> responseType) {
        return executeWithBody("POST", path, responseType);
    }

    public <T> ResponseAsserter<T> put(String path, Class<T> responseType) {
        return executeWithBody("PUT", path, responseType);
    }

    public <T> ResponseAsserter<T> delete(String path, Class<T> responseType) {
        urlBuilder.addPathSegments(path.replaceFirst("^/", ""));
        RequestBody finalBody = (requestBody != null) ? createRequestBody() : null;
        Request request = new Request.Builder().url(urlBuilder.build()).headers(headers.build()).delete(finalBody).build();
        return execute(request, responseType);
    }

    private RequestBody createRequestBody() {
        if (requestBody == null) {
            return RequestBody.create(new byte[0], null);
        }
        try {
            String jsonBody = apiClientService.getObjectMapper().writeValueAsString(requestBody);
            return RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body to JSON", e);
        }
    }

    private <T> ResponseAsserter<T> executeWithBody(String method, String path, Class<T> responseType) {
        urlBuilder.addPathSegments(path.replaceFirst("^/", ""));
        RequestBody finalBody = createRequestBody();
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .headers(headers.build())
                .method(method, finalBody)
                .build();
        return execute(request, responseType);
    }

    private <T> ResponseAsserter<T> execute(Request request, Class<T> responseType) {
        MeasuredResponse<T> response = apiClientService.execute(request, responseType);
        return new ResponseAsserter<>(response);
    }
}