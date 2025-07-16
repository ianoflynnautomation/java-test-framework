package Data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


public record GenerateTokenResponse(
        String token,
        String expiresUtc,
        String userId,
        String role
) {}
