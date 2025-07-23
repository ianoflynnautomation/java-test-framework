package solutions.bjjeire.api.data.common;


public record GenerateTokenResponse(
        String token,
        String expiresUtc,
        String userId,
        String role
) {}
