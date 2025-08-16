package solutions.bjjeire.api.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import solutions.bjjeire.api.models.ApiAssertionException;

import java.io.IOException;
import java.time.Duration;

@Getter
@RequiredArgsConstructor
public class ApiResponse {

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

    public <T> T as(Class<T> type) {
        String body = getBodyAsString();
        if (body == null || body.isBlank()) {
            throw new ApiAssertionException("Cannot deserialize response body because it is empty.", requestPath, "");
        }
        try {
            return objectMapper.readValue(body, type);
        } catch (IOException e) {
            throw new ApiAssertionException(
                    String.format("Failed to deserialize response body to type '%s'. Error: %s", type.getSimpleName(), e.getMessage()),
                    requestPath, body, e);
        }
    }

    public JsonNode asJsonNode() {
        return as(JsonNode.class);
    }

    public ResponseValidator should() {
        return new ResponseValidator(this);
    }
}