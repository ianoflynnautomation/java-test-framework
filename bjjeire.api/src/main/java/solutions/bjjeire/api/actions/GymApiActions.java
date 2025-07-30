package solutions.bjjeire.api.actions;

import org.springframework.stereotype.Component;
import solutions.bjjeire.api.validation.ValidatableResponse;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;
import solutions.bjjeire.core.data.gyms.*;

import java.util.HashMap;
import java.util.Map;


@Component
public class GymApiActions extends BaseApiActions {

    public CreateGymResponse createGym(String authToken, Gym gym) {
        CreateGymCommand command = new CreateGymCommand(gym);
        return runner.run(
                        given()
                                .withAuthToken(authToken)
                                .withBody(command)
                                .post("/api/gym")
                )
                .then().hasStatusCode(201)
                .as(CreateGymResponse.class);
    }

    public ValidatableResponse attemptToCreateGym(String authToken, CreateGymCommand command) {
        return runner.run(
                given()
                        .withAuthToken(authToken)
                        .withBody(command)
                        .post("/api/gym")
        );
    }

    public void deleteGym(String authToken, String gymId) {
        System.out.printf("CLEANUP: Deleting gym with ID: %s%n", gymId);
        runner.run(
                given()
                        .withAuthToken(authToken)
                        .delete("/api/gym/" + gymId)
        ).then().hasStatusCode(204);
    }

    public GetGymPaginatedResponse getGyms(String authToken, GetGymPaginationQuery query) {
        Map<String, Object> queryParams = new HashMap<>();
        if (query.getCounty() != null) {
            queryParams.put("county", query.getCounty().name());
        }

        return runner.run(
                        given()
                                .withAuthToken(authToken)
                                .withQueryParams(queryParams)
                                .get("/api/gym")
                )
                .then().hasStatusCode(200)
                .as(GetGymPaginatedResponse.class);
    }
}