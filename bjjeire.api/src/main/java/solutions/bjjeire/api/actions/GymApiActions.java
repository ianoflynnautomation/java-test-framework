package solutions.bjjeire.api.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import solutions.bjjeire.api.http.ApiClient;
import solutions.bjjeire.api.http.RequestSpecification;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;

import java.util.Map;

/**
 * A framework-agnostic API Actions class for the Gym resource.
 * This class is completely independent of any other actions class.
 */
@Component
public class GymApiActions {

    @Autowired
    private ApiClient apiClient;

    public record CreationResult<T>(T resource, Runnable cleanupAction) {}

    public CreationResult<Gym> createGym(String authToken, Gym gym) {
        CreateGymCommand command = new CreateGymCommand(gym);

        CreateGymResponse response = new RequestSpecification(apiClient, Map.of(), Map.of(), null, null)
                .withAuthToken(authToken)
                .withBody(command)
                .post("/api/gym")
                .then().hasStatusCode(201)
                .as(CreateGymResponse.class);

        Gym createdGym = response.data();
        final String gymId = createdGym.id();

        Runnable cleanupAction = () -> {
            System.out.printf("CLEANUP: Deleting gym with ID: %s%n", gymId);
            new RequestSpecification(apiClient, Map.of(), Map.of(), null, null)
                    .withAuthToken(authToken)
                    .delete("/api/gym/" + gymId)
                    .then().hasStatusCode(204);
        };

        return new CreationResult<>(createdGym, cleanupAction);
    }
}