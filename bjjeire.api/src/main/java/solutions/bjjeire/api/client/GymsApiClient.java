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
    ApiRequestBuilder request =
        ApiRequestBuilder.builder().post(GymEndpoints.GYMS).auth(auth).body(command).build();
    return requestExecutor.execute(request);
  }

  public Mono<ApiResponse> deleteGym(Authentication auth, String gymId) {
    ApiRequestBuilder request =
        ApiRequestBuilder.builder().delete(GymEndpoints.gymById(gymId)).auth(auth).build();
    return requestExecutor.execute(request);
  }
}
