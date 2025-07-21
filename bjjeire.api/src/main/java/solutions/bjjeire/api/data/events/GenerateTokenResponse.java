package solutions.bjjeire.api.data.events;


public record GenerateTokenResponse(
        String token,
        String expiresUtc,
        String userId,
        String role
) {}
