package solutions.bjjeire.api.client;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import solutions.bjjeire.api.auth.Authentication;
import solutions.bjjeire.api.auth.NoAuth;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiRequestBuilder {
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

        public Builder options(String path) {
            this.method = HttpMethod.OPTIONS;
            this.path = path;
            return this;
        }

        public Builder head(String path) {
            this.method = HttpMethod.HEAD;
            this.path = path;
            return this;
        }

        public Builder trace(String path) {
            this.method = HttpMethod.TRACE;
            this.path = path;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.add(name, value);
            return this;
        }

        public Builder headers(MultiValueMap<String, String> headers) {
            this.headers.clear();
            this.headers.putAll(headers);
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

        public Builder acceptableMediaTypes(List<MediaType> mediaTypes) {
            this.acceptableMediaTypes = mediaTypes != null ? mediaTypes
                    : Collections.singletonList(MediaType.APPLICATION_JSON);
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

        public Builder multipartBody(MultiValueMap<String, Object> multipartBody) {
            this.body = multipartBody;
            this.contentType = MediaType.MULTIPART_FORM_DATA;
            return this;
        }

        public Builder formBody(MultiValueMap<String, String> formBody) {
            this.body = formBody;
            this.contentType = MediaType.APPLICATION_FORM_URLENCODED;
            return this;
        }

        public Builder auth(Authentication auth) {
            this.authentication = auth;
            return this;
        }

        public ApiRequestBuilder build() {
            if (method == null || path == null) {
                throw new IllegalStateException("HTTP method and path must be set before building the request.");
            }
            return new ApiRequestBuilder(method, path, headers, queryParams, body, contentType, acceptableMediaTypes,
                    authentication);
        }
    }
}