package solutions.bjjeire.api.client;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.auth.Authentication;
import solutions.bjjeire.api.endpoints.GymEndpoints;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;

@Component
public class GymsApiClient extends BaseApiClient {

  protected GymsApiClient(RequestExecutor requestExecutor) {
    super(requestExecutor);
  }

  public Mono<ApiResponse> createGym(Authentication auth, CreateGymCommand command) {
    return execute(
        ApiRequest.builder().post(GymEndpoints.GYMS).authentication(auth).body(command).build());
  }

  public Mono<ApiResponse> deleteGym(Authentication auth, String gymId) {
    return execute(
        ApiRequest.builder().delete(GymEndpoints.gymById(gymId)).authentication(auth).build());
  }
}
