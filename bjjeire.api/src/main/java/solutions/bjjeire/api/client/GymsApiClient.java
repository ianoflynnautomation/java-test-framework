package solutions.bjjeire.api.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.auth.Authentication;
import solutions.bjjeire.api.endpoints.GymEndpoints;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.gyms.CreateGymCommand;

@Component
@RequiredArgsConstructor
public class GymsApiClient {

  private final RequestExecutor requestExecutor;

  public Mono<ApiResponse> createGym(Authentication auth, CreateGymCommand command) {
    ApiRequest request =
        ApiRequest.builder().post(GymEndpoints.GYMS).authentication(auth).body(command).build();
    return requestExecutor.execute(request);
  }

  public Mono<ApiResponse> deleteGym(Authentication auth, String gymId) {
    ApiRequest request =
        ApiRequest.builder().delete(GymEndpoints.gymById(gymId)).authentication(auth).build();
    return requestExecutor.execute(request);
  }
}
