package solutions.bjjeire.api.actions;

import org.springframework.stereotype.Component;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;

import java.util.Map;

/**
 * A framework-agnostic API Actions class for Authentication operations.
 */
@Component
public class AuthApiActions extends BaseApiActions {

    /**
     * Authenticates as an admin user by calling a token generation endpoint.
     * @return A JWT bearer token.
     */
    public String authenticateAsAdmin() {
        GenerateTokenResponse tokenResponse = runner.run(
                        request()
                                .queryParams(Map.of("userId", "dev-user@example.com", "role", "Admin"))
                                .get("/generate-token") // Assuming a mock/dev token endpoint
                                .build()
                )
                .then().statusCode(200)
                .andReturn().as(GenerateTokenResponse.class);

        return tokenResponse.token();
    }
}