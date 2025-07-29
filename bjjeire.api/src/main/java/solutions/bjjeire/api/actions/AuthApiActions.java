package solutions.bjjeire.api.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.http.ApiClient;
import solutions.bjjeire.api.http.RequestSpecification;
import solutions.bjjeire.core.data.common.GenerateTokenResponse;

import java.util.Map;

/**
 * A framework-agnostic API Actions class for Authentication operations.
 */
@Component
public class AuthApiActions {

    @Autowired
    private ApiClient apiClient;

    public String authenticateAsAdmin() {
        GenerateTokenResponse tokenResponse = new RequestSpecification(apiClient, Map.of(), Map.of(), null, null)
                .withQueryParams(Map.of("userId", "dev-user@example.com", "role", "Admin"))
                .get("/generate-token")
                .then().hasStatusCode(200)
                .as(GenerateTokenResponse.class);
        return tokenResponse.token();
    }
}
