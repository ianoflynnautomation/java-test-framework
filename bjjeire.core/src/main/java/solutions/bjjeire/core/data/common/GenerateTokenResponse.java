package solutions.bjjeire.core.data.common;


public record GenerateTokenResponse(
        String token,
        String expiresUtc,
        String userId,
        String role
) {}
