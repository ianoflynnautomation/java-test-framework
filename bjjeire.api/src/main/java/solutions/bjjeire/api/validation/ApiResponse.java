package solutions.bjjeire.api.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import solutions.bjjeire.api.exceptions.ApiAssertionException;
import solutions.bjjeire.api.exceptions.ApiResponseDeserializationException;

@Getter
@RequiredArgsConstructor
public class ApiResponse {

  private final Map<Class<?>, Object> deserializedBodyCache = new ConcurrentHashMap<>();

  private final ResponseEntity<String> responseEntity;
  private final Duration executionTime;
  private final ObjectMapper objectMapper;
  private final String requestPath;

  public int getStatusCode() {
    return responseEntity.getStatusCode().value();
  }

  public String getBodyAsString() {
    return responseEntity.getBody();
  }

  public HttpHeaders getHeaders() {
    return responseEntity.getHeaders();
  }

  public String header(String name) {
    return responseEntity.getHeaders().getFirst(name);
  }

  @SuppressWarnings("unchecked")
  public <T> T as(Class<T> type) {

    return (T)
        deserializedBodyCache.computeIfAbsent(
            type,
            key -> {
              String body = getBodyAsString();
              if (body == null || body.isBlank()) {
                throw new ApiAssertionException(
                    "Cannot deserialize response body because it is empty.", requestPath, body);
              }
              try {
                return objectMapper.readValue(body, key);
              } catch (IOException e) {
                throw new ApiResponseDeserializationException(
                    e.getMessage(), body, type.getSimpleName(), e);
              }
            });
  }

  public JsonNode asJsonNode() {
    return as(JsonNode.class);
  }

  public ResponseValidator shouldBe() {
    return new ResponseValidator(this);
  }

  public ResponseValidator should() {
    return new ResponseValidator(this);
  }
}
