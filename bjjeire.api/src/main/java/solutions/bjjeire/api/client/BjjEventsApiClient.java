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
    ApiRequestBuilder request =
        ApiRequestBuilder.builder()
            .post(BjjEventEndpoints.BJJ_EVENTS)
            .auth(auth)
            .body(command)
            .build();
    return requestExecutor.execute(request);
  }

  public Mono<ApiResponse> createEventWithInvalidPayload(
      Authentication auth, Object invalidPayload) {
    ApiRequestBuilder request =
        ApiRequestBuilder.builder()
            .post(BjjEventEndpoints.BJJ_EVENTS)
            .auth(auth)
            .body(invalidPayload)
            .build();
    return requestExecutor.execute(request);
  }

  public Mono<ApiResponse> deleteEvent(Authentication auth, String eventId) {
    ApiRequestBuilder request =
        ApiRequestBuilder.builder()
            .delete(BjjEventEndpoints.bjjEventById(eventId))
            .auth(auth)
            .build();
    return requestExecutor.execute(request);
  }
}
