package solutions.bjjeire.api.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Getter
public class MeasuredResponse<T> {
    // ... (This class is identical to the one you provided)
    private final Response rawResponse;
    private final Duration executionTime;
    private final T data;
    private final String responseBodyAsString;
    private final int statusCode;
    private final boolean isSuccessful;

    public MeasuredResponse(Response rawResponse, Duration executionTime, ObjectMapper objectMapper, Class<T> responseType) {
        this.rawResponse = rawResponse;
        this.executionTime = executionTime;
        this.statusCode = rawResponse.code();
        this.isSuccessful = rawResponse.isSuccessful();

        T deserializedData = null;
        String bodyString = null;
        try (ResponseBody body = rawResponse.body()) {
            if (body != null) {
                bodyString = body.string();
                if (responseType != null && responseType != Void.class && !bodyString.isEmpty() && isSuccessful) {
                    deserializedData = objectMapper.readValue(bodyString, responseType);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read or deserialize response body: " + e.getMessage(), e);
        }
        this.data = deserializedData;
        this.responseBodyAsString = bodyString;
    }

    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    public Optional<String> getHeader(String name) {
        return Optional.ofNullable(rawResponse.header(name));
    }
}