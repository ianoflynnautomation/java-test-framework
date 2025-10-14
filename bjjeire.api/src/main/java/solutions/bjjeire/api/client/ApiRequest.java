package solutions.bjjeire.api.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import solutions.bjjeire.api.auth.Authentication;
import solutions.bjjeire.api.auth.NoAuth;

@Getter
@Builder(toBuilder = true)
public class ApiRequest {

  @NonNull private final HttpMethod method;

  @NonNull private final String path;

  @Builder.Default
  private final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

  @Builder.Default
  private final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

  private final Object body;

  @Builder.Default private final MediaType contentType = MediaType.APPLICATION_JSON;

  @Builder.Default
  private final List<MediaType> acceptableMediaTypes =
      Collections.singletonList(MediaType.APPLICATION_JSON);

  @Builder.Default private final Authentication authentication = new NoAuth();

  public static class ApiRequestBuilder {
    private HttpMethod method;
    private String path;
    private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    private final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    private Object body;
    private MediaType contentType = MediaType.APPLICATION_JSON;
    private List<MediaType> acceptableMediaTypes =
        Collections.singletonList(MediaType.APPLICATION_JSON);
    private Authentication authentication = new NoAuth();

    public ApiRequestBuilder get(String path) {
      return this.method(HttpMethod.GET).path(path);
    }

    public ApiRequestBuilder post(String path) {
      return this.method(HttpMethod.POST).path(path);
    }

    public ApiRequestBuilder put(String path) {
      return this.method(HttpMethod.PUT).path(path);
    }

    public ApiRequestBuilder delete(String path) {
      return this.method(HttpMethod.DELETE).path(path);
    }

    public ApiRequestBuilder patch(String path) {
      return this.method(HttpMethod.PATCH).path(path);
    }

    public ApiRequestBuilder multipartBody(MultiValueMap<String, Object> multipartBody) {
      this.body = multipartBody;
      this.contentType = MediaType.MULTIPART_FORM_DATA;
      return this;
    }

    public ApiRequestBuilder formBody(MultiValueMap<String, String> formBody) {
      this.body = formBody;
      this.contentType = MediaType.APPLICATION_FORM_URLENCODED;
      return this;
    }

    public ApiRequestBuilder queryParams(Map<String, ?> params) {
      params.forEach((key, value) -> this.queryParams.add(key, String.valueOf(value)));
      return this;
    }
  }
}
