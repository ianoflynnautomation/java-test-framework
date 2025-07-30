package solutions.bjjeire.api.http;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import solutions.bjjeire.api.http.auth.Authentication;
import solutions.bjjeire.api.http.auth.NoAuth;

import java.util.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiRequest {

    private final HttpMethod method;
    private final String path;
    private final MultiValueMap<String, String> headers;
    private final MultiValueMap<String, String> queryParams;
    private final Object body;
    private final MediaType contentType;
    private final List<MediaType> acceptableMediaTypes;
    private final Authentication authentication;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HttpMethod method;
        private String path;
        private final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        private final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        private Object body;
        private MediaType contentType = MediaType.APPLICATION_JSON;
        private List<MediaType> acceptableMediaTypes = Collections.singletonList(MediaType.APPLICATION_JSON);
        private Authentication authentication = new NoAuth();

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder get(String path) {
            this.method = HttpMethod.GET;
            this.path = path;
            return this;
        }

        public Builder post(String path) {
            this.method = HttpMethod.POST;
            this.path = path;
            return this;
        }

        public Builder put(String path) {
            this.method = HttpMethod.PUT;
            this.path = path;
            return this;
        }

        public Builder delete(String path) {
            this.method = HttpMethod.DELETE;
            this.path = path;
            return this;
        }

        public Builder patch(String path) {
            this.method = HttpMethod.PATCH;
            this.path = path;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.add(name, value);
            return this;
        }

        public Builder queryParam(String name, Object value) {
            this.queryParams.add(name, String.valueOf(value));
            return this;
        }

        public Builder queryParams(Map<String, ?> params) {
            params.forEach((key, value) -> this.queryParams.add(key, String.valueOf(value)));
            return this;
        }

        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        public Builder contentType(MediaType contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder auth(Authentication auth) {
            this.authentication = auth;
            return this;
        }

        public ApiRequest build() {
            if (method == null || path == null) {
                throw new IllegalStateException("HTTP method and path must be set before building the request.");
            }
            return new ApiRequest(method, path, headers, queryParams, body, contentType, acceptableMediaTypes,
                    authentication);
        }
    }
}