package solutions.bjjeire.api.actions;

import solutions.bjjeire.api.http.TestClient;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;
import solutions.bjjeire.core.data.gyms.CreateGymResponse;
import solutions.bjjeire.core.data.gyms.Gym;

import java.util.function.Consumer;

/**
 * A framework-agnostic API Actions class for the Gym resource.
 * This class is completely independent of any other actions class.
 */
public class GymApiActions {

    /**
     * A record local to this class to hold the result of a creation action.
     */
    public record CreationResult<T>(T resource, Consumer<TestClient> cleanupAction) {}

    /**
     * Creates a BJJ gym and returns the created gym along with its cleanup action.
     * @param client A fresh TestClient instance.
     * @param authToken The auth token to use for the request.
     * @param gym The Gym object to create.
     * @return A CreationResult containing the new gym and its cleanup logic.
     */
    public CreationResult<Gym> createGym(TestClient client, String authToken, Gym gym) {
        CreateGymCommand command = new CreateGymCommand(gym);

        CreateGymResponse response = client
                .withAuthToken(authToken)
                .body(command)
                .post("/api/gym") // Use the /api/gym endpoint
                .then().hasStatusCode(201)
                .as(CreateGymResponse.class);

        Gym createdGym = response.data();
        final String gymId = createdGym.id();

        // Define the cleanup logic for the gym.
        Consumer<TestClient> cleanupAction = c -> {
            System.out.printf("CLEANUP (from Action): Deleting gym with ID: %s%n", gymId);
            c.withAuthToken(authToken)
                    .delete("/api/gym/" + gymId) // Use the correct delete endpoint
                    .then().hasStatusCode(204);
        };

        // Return a new instance of this class's own CreationResult.
        return new CreationResult<>(createdGym, cleanupAction);
    }
}
