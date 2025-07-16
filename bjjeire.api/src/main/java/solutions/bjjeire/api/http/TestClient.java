package solutions.bjjeire.api.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.configuration.ApiSettings;
import solutions.bjjeire.api.models.MeasuredResponse;
import solutions.bjjeire.api.services.IApiClientService;
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
    private static final Logger logger = LoggerFactory.getLogger(TestClient.class);

    private final IApiClientService apiClientService;

    @Getter
    private final ApiSettings settings;

    private Headers.Builder headers;
    private Map<String, String> queryParams;
    private Object requestBody;

    /**
     * Constructor for Spring dependency injection.
     * @param apiClientService The singleton API client service.
     * @param settings The singleton API settings configuration.
     */
    @Autowired
    public TestClient(IApiClientService apiClientService, ApiSettings settings) {
        this.apiClientService = Objects.requireNonNull(apiClientService, "IApiClientService cannot be null.");
        this.settings = Objects.requireNonNull(settings, "ApiSettings cannot be null. Check DI configuration.");
        resetState();
    }

    private void resetState() {
        this.headers = new Headers.Builder();
        this.queryParams = new HashMap<>();
        this.requestBody = null;
        logger.trace("TestClient state has been reset for a new request.");
    }

    public TestClient withHeader(String name, String value) {
        this.headers.add(name, value);
        return this;
    }

    public TestClient withAuthToken(String token) {
        if (token != null && !token.isBlank()) {
            this.headers.set("Authorization", "Bearer " + token);
            logger.debug("Authorization header set.");
        }
        return this;
    }

    public TestClient withQueryParams(Map<String, String> paramsMap) {
        this.queryParams.putAll(paramsMap);
        return this;
    }

    public TestClient body(Object body) {
        this.requestBody = body;
        return this;
    }

    private HttpUrl buildUrl(String path) {
        String baseUrl = settings.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("API baseUrl is null or empty. Check your testSettings.json and DI setup.");
        }
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(baseUrl)).newBuilder();
        urlBuilder.addPathSegments(path.replaceFirst("^/", ""));
        this.queryParams.forEach(urlBuilder::addQueryParameter);
        return urlBuilder.build();
    }

    private RequestBody createRequestBody() {
        if (requestBody == null) {
            return RequestBody.create(new byte[0], null); // Empty body for requests that need it
        }
        try {
            String jsonBody = apiClientService.getObjectMapper().writeValueAsString(requestBody);
            logger.trace("Serialized request body: {}", jsonBody);
            return RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body to JSON", e);
        }
    }

    public <T> ResponseAsserter<T> get(String path, Class<T> responseType) {
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .headers(headers.build())
                .get()
                .build();
        return execute(request, responseType);
    }

    public <T> ResponseAsserter<T> post(String path, Class<T> responseType) {
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .headers(headers.build())
                .post(createRequestBody())
                .build();
        return execute(request, responseType);
    }

    public <T> ResponseAsserter<T> put(String path, Class<T> responseType) {
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .headers(headers.build())
                .put(createRequestBody())
                .build();
        return execute(request, responseType);
    }

    public <T> ResponseAsserter<T> delete(String path, Class<T> responseType) {
        RequestBody body = (this.requestBody != null) ? createRequestBody() : null;
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .headers(headers.build())
                .delete(body)
                .build();
        return execute(request, responseType);
    }

    private <T> ResponseAsserter<T> execute(Request request, Class<T> responseType) {
        try {
            return new ResponseAsserter<>(apiClientService.execute(request, responseType));
        } finally {
            // Reset state after the request is executed to ensure the client is clean
            // in case it's reused within the same scenario.
            resetState();
        }
    }
}
