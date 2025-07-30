package solutions.bjjeire.api.actions;

import org.springframework.stereotype.Component;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;

import java.util.Map;

/**
 * A framework-agnostic API Actions class for Authentication operations.
 */
@Component
public class AuthApiActions extends BaseApiActions {

    public String authenticateAsAdmin() {
        GenerateTokenResponse tokenResponse = runner.run(
                        given()
                                .withQueryParams(Map.of("userId", "dev-user@example.com", "role", "Admin"))
                                .get("/generate-token")
                )
                .then().hasStatusCode(200)
                .as(GenerateTokenResponse.class);

        return tokenResponse.token();
    }
}