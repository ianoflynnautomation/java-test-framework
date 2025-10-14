package solutions.bjjeire.api.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import solutions.bjjeire.api.auth.Authentication;
import solutions.bjjeire.api.endpoints.BjjEventEndpoints;
import solutions.bjjeire.api.validation.ApiResponse;
import solutions.bjjeire.core.data.events.CreateBjjEventCommand;

@Component
@RequiredArgsConstructor
public class BjjEventsApiClient {

  private final RequestExecutor requestExecutor;

  public Mono<ApiResponse> createEvent(Authentication auth, CreateBjjEventCommand command) {
    ApiRequest request =
        ApiRequest.builder()
            .post(BjjEventEndpoints.BJJ_EVENTS)
            .authentication(auth)
            .body(command)
            .build();
    return requestExecutor.execute(request);
  }

  public Mono<ApiResponse> createEventWithInvalidPayload(
      Authentication auth, Object invalidPayload) {
    ApiRequest request =
        ApiRequest.builder()
            .post(BjjEventEndpoints.BJJ_EVENTS)
            .authentication(auth)
            .body(invalidPayload)
            .build();
    return requestExecutor.execute(request);
  }

  public Mono<ApiResponse> deleteEvent(Authentication auth, String eventId) {
    ApiRequest request =
        ApiRequest.builder()
            .delete(BjjEventEndpoints.bjjEventById(eventId))
            .authentication(auth)
            .build();
    return requestExecutor.execute(request);
  }
}
