package Data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the JSON response from the token generation endpoint.
 * e.g., { "token": "your-jwt-string" }
 */
public record GenerateTokenResponse(String token) {
    @JsonCreator
    public GenerateTokenResponse(@JsonProperty("token") String token) {
        this.token = token;
    }
}
