package solutions.bjjeire.api.actions;

import org.springframework.stereotype.Component;
import solutions.bjjeire.api.http.auth.BearerTokenAuth;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.gyms.*;

import java.util.HashMap;
import java.util.Map;

@Component
public class GymApiActions extends BaseApiActions {

    public ApiResponse createGym(String authToken, CreateGymCommand command) {
        return runner.run(
                request()
                        .auth(new BearerTokenAuth(authToken))
                        .body(command)
                        .post("/api/gym")
                        .build());
    }

    public void deleteGym(String authToken, String gymId) {
        System.out.printf("CLEANUP: Deleting gym with ID: %s%n", gymId);
        runner.run(
                request()
                        .auth(new BearerTokenAuth(authToken))
                        .delete("/api/gym/" + gymId)
                        .build())
                .then().statusCode(204);
    }

    public ApiResponse getGyms(String authToken, GetGymPaginationQuery query) {
        Map<String, Object> queryParams = new HashMap<>();
        if (query.getCounty() != null) {
            queryParams.put("county", query.getCounty().name());
        }

        return runner.run(
                request()
                        .auth(new BearerTokenAuth(authToken))
                        .queryParams(queryParams)
                        .get("/api/gym")
                        .build());
    }

    public ApiResponse attemptToCreateGymWithInvalidData(String authToken, Object invalidPayload) {
        return runner.run(
                request()
                        .auth(new BearerTokenAuth(authToken))
                        .body(invalidPayload)
                        .post("/api/gym")
                        .build());
    }
}