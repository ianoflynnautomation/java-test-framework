package solutions.bjjeire.api.actions;

import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;

import java.util.Map;

/**
 * A framework-agnostic API Actions class for Authentication operations.
 */
public class AuthApiActions {

    /**
     * Authenticates as an admin.
     * @param client A fresh TestClient instance.
     * @return The auth token.
     */
    public String authenticateAsAdmin(TestClient client) {
        GenerateTokenResponse tokenResponse = client
                .withQueryParams(Map.of("userId", "dev-user@example.com", "role", "Admin"))
                .get("/generate-token")
                .then().hasStatusCode(200)
                .as(GenerateTokenResponse.class);
        return tokenResponse.token();
    }
}
